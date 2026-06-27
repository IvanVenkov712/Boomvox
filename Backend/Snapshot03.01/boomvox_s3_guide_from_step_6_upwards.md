# BoomVox — AWS S3 Integration Guide (FileStorageService onwards)

> **Stack:** Java 21 + Spring Boot · Angular SPA · PostgreSQL · AWS S3
> **Starting point:** Steps 1–5 (bucket creation, IAM user, CORS, Spring Boot AWS config) are
> already covered in the original guide. This document picks up from Step 6 and covers the
> **complete** upload and streaming implementation — first without `audio_variants`, then the
> full upgrade path that adds multi-quality variant processing.

---

## Table of Contents

**Phase 1 — Single-file upload & streaming (build this first)**

1. [Step 6 — FileStorageService](#step-6--filestorageservice)
2. [Step 7 — Upload Flow (End-to-End)](#step-7--upload-flow-end-to-end)
3. [Step 8 — Streaming Flow (End-to-End)](#step-8--streaming-flow-end-to-end)
4. [Step 9 — StreamingController & Signed URL Endpoint](#step-9--streamingcontroller--signed-url-endpoint)
5. [Step 10 — Domain & Enum Classes](#step-10--domain--enum-classes)
6. [Step 11 — PlaybackService](#step-11--playbackservice)
7. [Step 12 — Session & Event REST Endpoints](#step-12--session--event-rest-endpoints)
8. [Step 13 — Angular Audio Player Integration](#step-13--angular-audio-player-integration)

**Phase 2 — audio_variants: multi-quality processing (add after Phase 1 works)**

9.  [Step 14 — What audio_variants Adds](#step-14--what-audio_variants-adds)
10. [Step 15 — Liquibase Migration](#step-15--liquibase-migration)
11. [Step 16 — AudioVariant Domain & Enums](#step-16--audiovariant-domain--enums)
12. [Step 17 — Song Domain Changes](#step-17--song-domain-changes)
13. [Step 18 — FileStorageService Additions](#step-18--filestorageservice-additions)
14. [Step 19 — AudioProcessingJob (FFmpeg pipeline)](#step-19--audioprocessingjob-ffmpeg-pipeline)
15. [Step 20 — SongService Upload Changes](#step-20--songservice-upload-changes)
16. [Step 21 — StreamingController Changes](#step-21--streamingcontroller-changes)
17. [Step 22 — StreamingSession variant_id FK](#step-22--streamingsession-variant_id-fk)
18. [Step 23 — Angular Player Changes](#step-23--angular-player-changes)

**Common**

19. [Security Checklist](#security-checklist)
20. [Cost Optimization](#cost-optimization)

---

---

# PHASE 1 — Single-file upload & streaming

> Build and test this entirely before touching Phase 2.
> All Phase 2 changes are additive — nothing from Phase 1 gets deleted, only extended.

---

## Step 6 — FileStorageService

This is the **single point of contact** between your application and S3. No controller or
service ever calls S3 directly — everything goes through here.

> **Note on `@Value` fields:** Spring resolves `@Value` fields after construction, so they
> cannot be constructor parameters. Inject only the SDK beans via the constructor and declare
> the config values as `@Value` fields.

```java
// service/FileStorageService.java
@Service
@Slf4j
public class FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.signedUrlExpiryMinutes:15}")
    private int signedUrlExpiryMinutes;

    public FileStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client    = s3Client;
        this.s3Presigner = s3Presigner;
    }

    // ─────────────────────────────────────────────
    // UPLOAD
    // ─────────────────────────────────────────────

    /**
     * Upload any file to S3.
     *
     * @param key           S3 object key, e.g. "songs/song_abc123.mp3"
     * @param inputStream   file content
     * @param contentLength byte length (required by SDK)
     * @param contentType   MIME type, e.g. "audio/mpeg"
     * @return the S3 key — store this in the database
     */
    public String uploadFile(String key, InputStream inputStream,
                             long contentLength, String contentType) {
        try {
            log.info("Uploading to S3: bucket={} key={}", bucket, key);
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build(),
                RequestBody.fromInputStream(inputStream, contentLength)
            );
            log.info("Upload complete: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}: {}", key, e.awsErrorDetails().errorMessage());
            throw new StorageException("Failed to upload file to S3", e);
        }
    }

    /**
     * Convenience overload — wraps a Spring MultipartFile.
     */
    public String uploadMultipartFile(String key, MultipartFile file) {
        try {
            return uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        }
    }

    // ─────────────────────────────────────────────
    // SIGNED URL GENERATION
    // ─────────────────────────────────────────────

    /**
     * Generate a pre-signed GET URL valid for signedUrlExpiryMinutes (default 15).
     *
     * Phase 1: pass song.storageKey
     * Phase 2: pass audioVariant.streamingKey
     *
     * @param s3Key the key stored in the database
     * @return a temporary HTTPS URL the browser uses to stream directly from S3
     */
    public String generateSignedUrl(String s3Key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(signedUrlExpiryMinutes))
                .getObjectRequest(req -> req
                    .bucket(bucket)
                    .key(s3Key)
                    .responseContentType("audio/mpeg")
                )
                .build();

            return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
        } catch (S3Exception e) {
            log.error("Failed to generate signed URL for key {}: {}", s3Key,
                e.awsErrorDetails().errorMessage());
            throw new StorageException("Failed to generate signed URL", e);
        }
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    /**
     * Delete a single S3 object.
     * Logs a warning on failure instead of throwing — a failed delete during cleanup
     * is non-critical because the Liquibase schema still holds the key for retry.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
            log.info("Deleted S3 object: {}", key);
        } catch (S3Exception e) {
            log.warn("Failed to delete S3 object {}: {}", key, e.awsErrorDetails().errorMessage());
        }
    }

    // ─────────────────────────────────────────────
    // KEY BUILDERS — call these everywhere; never hand-craft keys inline
    // ─────────────────────────────────────────────

    /** Phase 1 key for the uploaded song file. Example: "songs/song_a3f1bc.mp3" */
    public static String songKey(String uuid, String extension) {
        return "songs/song_" + uuid + "." + extension.toLowerCase();
    }

    /** Phase 2 key for a raw upload staging area. Example: "raw/uploads/song_a3f1bc_name.mp3" */
    public static String rawUploadKey(String uuid, String originalFilename) {
        return "raw/uploads/song_" + uuid + "_" + sanitize(originalFilename);
    }

    /** Phase 2 key for a processed quality variant. Example: "variants/song_42_128k.mp3" */
    public static String variantKey(long songId, int bitrateKbps, String format) {
        return "variants/song_" + songId + "_" + bitrateKbps + "k." + format.toLowerCase();
    }

    /** Cover art key. Example: "covers/song_42_cover.jpg" */
    public static String coverKey(long songId, String extension) {
        return "covers/song_" + songId + "_cover." + extension.toLowerCase();
    }

    /** User avatar key. Example: "avatars/user_7_avatar.png" */
    public static String avatarKey(long userId, String extension) {
        return "avatars/user_" + userId + "_avatar." + extension.toLowerCase();
    }

    private static String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }
}
```

---

## Step 7 — Upload Flow (End-to-End)

### Flow Diagram

```
Artist (Angular)
    │
    │  POST /api/songs
    │  Content-Type: multipart/form-data
    │  Body: audio file + metadata (name, albumId, format, duration)
    │
    ▼
SongController.uploadSong()
    │
    ├── Validate MIME type (audio/mpeg or audio/wav only)
    ├── Validate file size (≤ 100 MB)
    │
    ├── Generate UUID → build S3 key: songs/song_{UUID}.mp3
    ├── Upload audio file → S3
    │
    ├── Build Song entity:
    │     album       = look up by albumId
    │     name        = from metadata
    │     uploadedAt  = LocalDateTime.now()
    │     format      = SongFormat from metadata
    │     duration    = from metadata
    │     fileSize    = file.getSize()
    │     storageKey  = the S3 key just uploaded  ← never client-supplied
    │     stats       = new SongStats(0,0,0,0,0)  ← constructor handles this
    │
    └── Save Song → HTTP 201 with SongResponse
```

### StorageException

```java
// exception/StorageException.java
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### SongUploadRequest DTO

```java
// dto/SongUploadRequest.java
public record SongUploadRequest(
    @NotNull  long      albumId,
    @NotBlank String    name,
    @NotNull  SongFormat format,
    long duration   // seconds
) {}
```

### SongController — Upload Endpoint

```java
// controller/SongController.java
@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<SongResponse> uploadSong(
            @RequestPart("audio")    MultipartFile      audioFile,
            @RequestPart("metadata") @Valid SongUploadRequest metadata) {

        validateAudioMimeType(audioFile);

        if (audioFile.getSize() > 100L * 1024 * 1024) {
            throw new ValidationException("Audio file must not exceed 100 MB");
        }

        SongResponse response = songService.uploadSong(audioFile, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validateAudioMimeType(MultipartFile file) {
        String ct = file.getContentType();
        if (!"audio/mpeg".equals(ct) && !"audio/wav".equals(ct)) {
            throw new ValidationException("Only MP3 and WAV files are accepted");
        }
    }
}
```

### SongService — uploadSong & deleteSong

Replace your current `uploadSong(SongRequest)` and `deleteSong` with these:

```java
// service/SongService.java  (relevant methods — rest of the class is unchanged)

@Transactional
public SongResponse uploadSong(MultipartFile audioFile, SongUploadRequest request) {
    Album album = albumRepository.findById(request.albumId())
        .orElseThrow(() -> new NotFoundException("Album", request.albumId()));

    // Key is always generated server-side — never trust the client to supply a storage path
    String uuid      = UUID.randomUUID().toString();
    String extension = resolveExtension(request.format());
    String s3Key     = FileStorageService.songKey(uuid, extension);

    fileStorageService.uploadMultipartFile(s3Key, audioFile);

    Song song = new Song(
        album,
        request.name().trim(),
        LocalDateTime.now(),
        request.format(),
        request.duration(),
        audioFile.getSize(),  // derive from the actual file, not from the request body
        s3Key
    );

    return SongResponse.from(songRepository.save(song));
}

public void deleteSong(long id) {
    Song song = songRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Song", id));

    // Delete S3 object before the DB row.
    // If S3 fails, the song row survives and the key is still known — retryable.
    // If DB fails after S3 succeeds, the orphan file is gone but no harm done.
    fileStorageService.deleteFile(song.getStorageKey());
    songRepository.deleteById(id);
}

private String resolveExtension(SongFormat format) {
    return switch (format) {
        case MP3  -> "mp3";
        case WAV  -> "wav";
        case FLAC -> "flac";
        // extend as SongFormat grows
    };
}
```

> **Important:** Add `FileStorageService` to the `SongService` constructor injection alongside
> the existing repositories.

---

## Step 8 — Streaming Flow (End-to-End)

### Flow Diagram

```
User clicks Play (Angular)
    │
    │  GET /api/songs/{songId}/stream
    │  Headers: Authorization: Bearer <JWT>
    │
    ▼
StreamingController.getStreamUrl()
    │
    ├── Spring Security: validate JWT → extract userId
    ├── Load Song from DB
    │
    ├── fileStorageService.generateSignedUrl(song.storageKey)
    │     → S3Presigner signs key with 15-minute expiry
    │     → Returns HTTPS URL with embedded AWS signature
    │
    ├── PlaybackService.startSession(songId, userId)
    │     → INSERT streaming_session (status = ACTIVE)
    │     → INSERT streaming_event   (type = START, positionSec = 0)
    │
    └── Return JSON: { streamUrl, expiresAt, sessionId, durationSec }

Browser receives streamUrl
    │
    ├── Sets <audio src="...signed-s3-url...">
    │
    ├── Initial request → S3: GET (no Range header)
    │     S3 response: HTTP 200, audio begins streaming
    │
    ├── User seeks to 2:30
    │     Browser → S3: GET Range: bytes=7500000-
    │     S3 response: HTTP 206 Partial Content
    │     (Spring Boot is NOT in this data path)
    │
    └── Progress updates → Spring Boot every ~10 s:
          POST /api/streaming/events
          Body: { sessionId, type, positionSec }
```

---

## Step 9 — StreamingController & Signed URL Endpoint

### StreamUrlResponse DTO

```java
// dto/StreamUrlResponse.java
public record StreamUrlResponse(
    String  streamUrl,   // signed S3 URL — expires in 15 min, never log or store this
    Instant expiresAt,   // Angular uses this to know when to refresh
    long    sessionId,   // opened StreamingSession id — Angular needs this for event calls
    long    durationSec  // from song.duration
) {}
```

### StreamingController

```java
// controller/StreamingController.java
@RestController
@RequestMapping("/api/songs")
public class StreamingController {

    private final SongRepository      songRepository;
    private final FileStorageService  fileStorageService;
    private final PlaybackService     playbackService;

    public StreamingController(
        SongRepository     songRepository,
        FileStorageService fileStorageService,
        PlaybackService    playbackService
    ) {
        this.songRepository     = songRepository;
        this.fileStorageService = fileStorageService;
        this.playbackService    = playbackService;
    }

    /**
     * GET /api/songs/{songId}/stream
     *
     * Returns a signed S3 URL. The browser streams audio directly from S3 using this URL.
     * Spring Security has already validated the JWT before this method runs.
     */
    @GetMapping("/{songId}/stream")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(
            @PathVariable long songId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Song song = songRepository.findById(songId)
            .orElseThrow(() -> new NotFoundException("Song", songId));

        // Phase 1: sign song.storageKey directly — no variant selection yet
        String signedUrl = fileStorageService.generateSignedUrl(song.getStorageKey());

        StreamingSession session = playbackService.startSession(songId, currentUser.getId());

        return ResponseEntity.ok(new StreamUrlResponse(
            signedUrl,
            Instant.now().plus(15, ChronoUnit.MINUTES),
            session.getId(),
            song.getDuration()
        ));
    }
}
```

---

## Step 10 — Domain & Enum Classes

### SessionStatus

```java
// enums/SessionStatus.java
public enum SessionStatus {
    ACTIVE,      // in progress
    COMPLETED,   // user listened to > 90 %
    SKIPPED,     // user left before 20 %
    ABANDONED    // tab closed / app backgrounded without natural end
}
```

### StreamingEventType

```java
// enums/StreamingEventType.java
public enum StreamingEventType {
    START,
    PAUSE,
    RESUME,
    SEEK,
    REPLAY,
    END
}
```

### StreamingSession entity

> **Schema note:** your `streaming_session.id` is `INTEGER` and `streaming_event.session_id`
> references it as `INTEGER`. Use `int` / `Integer` for the id so JPA types match exactly.
> Migrating both to `BIGINT` later is one Liquibase changeset — worth doing for consistency
> with the rest of your schema.

```java
// domain/StreamingSession.java
@Entity
@Table(name = "streaming_session")
@NoArgsConstructor
public class StreamingSession {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Getter
    @Column(name = "song_id", nullable = false)
    private long songId;

    @Getter
    @Column(name = "playlist_id")
    private Long playlistId;

    @Getter
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Getter
    @Setter
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Version
    private long version;

    public StreamingSession(long userId, long songId, Long playlistId, LocalDateTime startedAt) {
        this.userId     = userId;
        this.songId     = songId;
        this.playlistId = playlistId;
        this.startedAt  = startedAt;
        this.status     = SessionStatus.ACTIVE;
    }
}
```

### StreamingEvent entity

```java
// domain/StreamingEvent.java
@Entity
@Table(name = "streaming_event")
@NoArgsConstructor
public class StreamingEvent {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private StreamingSession session;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StreamingEventType type;

    @Getter
    @Column(name = "position_sec", nullable = false)
    private long positionSec;

    @Getter
    @Column(name = "seek_from_sec")
    private Long seekFromSec;

    @Getter
    @Column(name = "seek_to_sec")
    private Long seekToSec;

    @Getter
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    private long version;

    public StreamingEvent(StreamingSession session, StreamingEventType type,
                          long positionSec, Long seekFromSec, Long seekToSec) {
        this.session     = session;
        this.type        = type;
        this.positionSec = positionSec;
        this.seekFromSec = seekFromSec;
        this.seekToSec   = seekToSec;
        this.createdAt   = LocalDateTime.now();
    }
}
```

### Repositories

```java
// repository/StreamingSessionRepository.java
public interface StreamingSessionRepository extends JpaRepository<StreamingSession, Integer> {}

// repository/StreamingEventRepository.java
public interface StreamingEventRepository extends JpaRepository<StreamingEvent, Long> {}
```

---

## Step 11 — PlaybackService

Owns all session and event logic. `StreamingController` and `StreamingSessionController` both
delegate here — neither one touches the repositories directly for write operations.

```java
// service/PlaybackService.java
@Service
@Transactional
public class PlaybackService {

    private final StreamingSessionRepository sessionRepository;
    private final StreamingEventRepository   eventRepository;

    public PlaybackService(
        StreamingSessionRepository sessionRepository,
        StreamingEventRepository   eventRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository   = eventRepository;
    }

    /**
     * Opens a new ACTIVE session and logs a START event.
     * Called by StreamingController when a signed URL is issued.
     */
    public StreamingSession startSession(long songId, long userId) {
        StreamingSession session = new StreamingSession(userId, songId, null, LocalDateTime.now());
        session = sessionRepository.save(session);
        logEvent(session, StreamingEventType.START, 0L, null, null);
        return session;
    }

    /**
     * Logs any mid-session event (PAUSE, RESUME, SEEK, REPLAY).
     * Called from StreamingSessionController on POST /api/streaming/events.
     */
    public void logEvent(StreamingSession session, StreamingEventType type,
                         long positionSec, Long seekFromSec, Long seekToSec) {
        eventRepository.save(
            new StreamingEvent(session, type, positionSec, seekFromSec, seekToSec)
        );
    }

    /**
     * Closes a session with its final status.
     * Called from StreamingSessionController on PATCH /api/streaming/sessions/{id}.
     */
    public void endSession(long sessionId, SessionStatus finalStatus) {
        StreamingSession session = sessionRepository.findById((int) sessionId)
            .orElseThrow(() -> new NotFoundException("StreamingSession", sessionId));

        session.setStatus(finalStatus);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}
```

---

## Step 12 — Session & Event REST Endpoints

Angular calls these during playback to update session state and log events.

```java
// controller/StreamingSessionController.java
@RestController
@RequestMapping("/api/streaming")
public class StreamingSessionController {

    private final PlaybackService            playbackService;
    private final StreamingSessionRepository sessionRepository;

    public StreamingSessionController(
        PlaybackService            playbackService,
        StreamingSessionRepository sessionRepository
    ) {
        this.playbackService    = playbackService;
        this.sessionRepository  = sessionRepository;
    }

    /**
     * PATCH /api/streaming/sessions/{id}
     * Angular calls this when playback ends (COMPLETED, SKIPPED, or ABANDONED).
     */
    @PatchMapping("/sessions/{id}")
    public ResponseEntity<Void> updateSession(
            @PathVariable long id,
            @RequestBody  UpdateSessionRequest request) {

        playbackService.endSession(id, request.status());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/streaming/events
     * Angular calls this on PAUSE, RESUME, SEEK, REPLAY — fire-and-forget from the client.
     */
    @PostMapping("/events")
    public ResponseEntity<Void> logEvent(@RequestBody LogEventRequest request) {
        StreamingSession session = sessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new NotFoundException("StreamingSession", request.sessionId()));

        playbackService.logEvent(
            session,
            request.type(),
            request.positionSec(),
            request.seekFromSec(),
            request.seekToSec()
        );
        return ResponseEntity.noContent().build();
    }
}
```

```java
// dto/UpdateSessionRequest.java
public record UpdateSessionRequest(SessionStatus status) {}

// dto/LogEventRequest.java
public record LogEventRequest(
    int                sessionId,
    StreamingEventType type,
    long               positionSec,
    Long               seekFromSec,
    Long               seekToSec
) {}
```

---

## Step 13 — Angular Audio Player Integration

### StreamingService

```typescript
// services/streaming.service.ts
@Injectable({ providedIn: 'root' })
export class StreamingService {
  constructor(private http: HttpClient) {}

  getStreamUrl(songId: number): Observable<StreamUrlResponse> {
    return this.http.get<StreamUrlResponse>(`/api/songs/${songId}/stream`);
  }
}

export interface StreamUrlResponse {
  streamUrl:   string;
  expiresAt:   string;   // ISO-8601 instant
  sessionId:   number;
  durationSec: number;
}
```

### StreamingSessionService

```typescript
// services/streaming-session.service.ts
@Injectable({ providedIn: 'root' })
export class StreamingSessionService {
  constructor(private http: HttpClient) {}

  endSession(sessionId: number, status: 'COMPLETED' | 'SKIPPED' | 'ABANDONED'): void {
    this.http.patch(`/api/streaming/sessions/${sessionId}`, { status })
      .subscribe();
  }

  logEvent(sessionId: number, type: string, positionSec: number,
           seekFromSec?: number, seekToSec?: number): void {
    this.http.post('/api/streaming/events',
      { sessionId, type, positionSec, seekFromSec, seekToSec }
    ).subscribe();
  }
}
```

### Audio Player Component

```typescript
// components/audio-player/audio-player.component.ts
@Component({
  selector: 'app-audio-player',
  template: `
    <audio #audioEl
      [src]="currentStreamUrl"
      (timeupdate)="onTimeUpdate($event)"
      (ended)="onEnded()"
      (error)="onError()"
      preload="metadata">
    </audio>
    <div class="player-controls">
      <button (click)="togglePlay()">{{ isPlaying ? 'Pause' : 'Play' }}</button>
      <input type="range" [max]="duration" [value]="currentTime" (input)="seek($event)" />
      <span>{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
    </div>
  `
})
export class AudioPlayerComponent implements OnDestroy {
  @ViewChild('audioEl') audioEl!: ElementRef<HTMLAudioElement>;

  currentStreamUrl: string | null = null;
  isPlaying   = false;
  currentTime = 0;
  duration    = 0;

  private songId:       number | null = null;
  private sessionId:    number | null = null;
  private urlExpiresAt: Date   | null = null;
  private progressInterval?: ReturnType<typeof setInterval>;

  constructor(
    private streamingService: StreamingService,
    private sessionService:   StreamingSessionService
  ) {}

  loadSong(songId: number): void {
    this.songId = songId;
    this.streamingService.getStreamUrl(songId).subscribe(response => {
      this.currentStreamUrl = response.streamUrl;
      this.urlExpiresAt     = new Date(response.expiresAt);
      this.duration         = response.durationSec;
      this.sessionId        = response.sessionId;

      // Heartbeat — log RESUME every 10 s so the backend knows the user is still listening
      this.progressInterval = setInterval(() => {
        if (this.isPlaying && this.sessionId) {
          this.sessionService.logEvent(
            this.sessionId, 'RESUME', Math.floor(this.currentTime)
          );
        }
      }, 10_000);
    });
  }

  togglePlay(): void {
    const audio = this.audioEl.nativeElement;
    if (this.isPlaying) {
      audio.pause();
      this.isPlaying = false;
      this.sessionService.logEvent(this.sessionId!, 'PAUSE', Math.floor(this.currentTime));
    } else {
      if (this.isUrlExpiringSoon()) {
        this.refreshStreamUrl().then(() => audio.play());
      } else {
        audio.play();
      }
      this.isPlaying = true;
    }
  }

  seek(event: Event): void {
    const targetSec = Number((event.target as HTMLInputElement).value);
    const fromSec   = Math.floor(this.currentTime);
    this.audioEl.nativeElement.currentTime = targetSec;
    this.sessionService.logEvent(
      this.sessionId!, 'SEEK', fromSec, fromSec, Math.floor(targetSec)
    );
  }

  onTimeUpdate(event: Event): void {
    this.currentTime = (event.target as HTMLAudioElement).currentTime;
  }

  onEnded(): void {
    this.isPlaying = false;
    const pct    = this.duration > 0 ? this.currentTime / this.duration : 0;
    const status = pct >= 0.9 ? 'COMPLETED' : 'SKIPPED';
    if (this.sessionId) this.sessionService.endSession(this.sessionId, status);
  }

  onError(): void {
    if (this.isUrlExpiringSoon()) this.refreshStreamUrl();
  }

  private isUrlExpiringSoon(): boolean {
    if (!this.urlExpiresAt) return false;
    return (this.urlExpiresAt.getTime() - Date.now()) < 2 * 60 * 1000;
  }

  private async refreshStreamUrl(): Promise<void> {
    // Re-fetches a signed URL without starting a new session.
    // The backend just signs the key again — session continuity is maintained by sessionId.
    return new Promise(resolve => {
      this.streamingService.getStreamUrl(this.songId!).subscribe(response => {
        this.currentStreamUrl = response.streamUrl;
        this.urlExpiresAt     = new Date(response.expiresAt);
        resolve();
      });
    });
  }

  formatTime(sec: number): string {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  ngOnDestroy(): void {
    clearInterval(this.progressInterval);
    if (this.sessionId) this.sessionService.endSession(this.sessionId, 'ABANDONED');
  }
}
```

---

---

# PHASE 2 — audio_variants: multi-quality processing

> Add this after Phase 1 is working end-to-end. Every change below is additive.
> The session/event system, Angular player structure, and `FileStorageService` core are unchanged.

---

## Step 14 — What audio_variants Adds

When a song is uploaded, instead of storing the raw file as the final deliverable, an async
FFmpeg job transcodes it into three quality variants and stores each in S3:

| Variant | Key pattern | Who gets it |
|---|---|---|
| 128 kbps MP3 | `variants/song_{id}_128k.mp3` | Guests, free users |
| 320 kbps MP3 | `variants/song_{id}_320k.mp3` | Registered users, artists, admins |
| 256 kbps AAC | `variants/song_{id}_256k.aac` | Premium (future) |

The `Song` entity gains a `processingStatus` field. The song is invisible to listeners until
processing completes and `processingStatus` flips to `ACTIVE`.

---

## Step 15 — Liquibase Migration

### 015 — add processing_status to song

```sql
--liquibase formatted sql
--changeset boomvox:015-add-processing-status-to-song

ALTER TABLE song ADD COLUMN processing_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE';

--rollback ALTER TABLE song DROP COLUMN processing_status;
```

> Default `ACTIVE` means all existing songs (uploaded before this feature) continue to work.
> New uploads start as `PROCESSING` and flip to `ACTIVE` once FFmpeg finishes.

### 016 — create audio_variant table

```sql
--liquibase formatted sql
--changeset boomvox:016-create-audio-variant-table

CREATE TABLE audio_variant (
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    song_id          BIGINT        NOT NULL,
    format           VARCHAR(255)  NOT NULL,
    bitrate_kbps     INTEGER       NOT NULL,
    duration_sec     INTEGER       NOT NULL,
    file_size_bytes  BIGINT        NOT NULL,
    storage_url      VARCHAR(1024) NOT NULL,
    streaming_key    VARCHAR(1024) NOT NULL,
    is_default       BOOLEAN       NOT NULL DEFAULT FALSE,
    status           VARCHAR(255)  NOT NULL,
    version          BIGINT        NOT NULL,
    CONSTRAINT pk_audio_variant PRIMARY KEY (id),
    CONSTRAINT fk_audio_variant_song FOREIGN KEY (song_id) REFERENCES song (id)
);

--rollback DROP TABLE IF EXISTS audio_variant;
```

### 017 — add variant_id to streaming_session

```sql
--liquibase formatted sql
--changeset boomvox:017-add-variant-id-to-streaming-session

ALTER TABLE streaming_session
    ADD COLUMN variant_id BIGINT,
    ADD CONSTRAINT fk_streaming_session_variant
        FOREIGN KEY (variant_id) REFERENCES audio_variant (id);

--rollback ALTER TABLE streaming_session DROP CONSTRAINT fk_streaming_session_variant;
         ALTER TABLE streaming_session DROP COLUMN variant_id;
```

---

## Step 16 — AudioVariant Domain & Enums

### AudioVariantStatus enum

```java
// enums/AudioVariantStatus.java
public enum AudioVariantStatus {
    PROCESSING,  // FFmpeg job is running
    READY,       // uploaded to S3 and available for streaming
    FAILED       // FFmpeg or S3 upload failed for this variant
}
```

### SongProcessingStatus enum

```java
// enums/SongProcessingStatus.java
public enum SongProcessingStatus {
    PROCESSING,  // at least one variant is still being processed
    ACTIVE,      // all variants are READY — song is streamable
    FAILED       // processing failed; no variants are available
}
```

### AudioVariant entity

```java
// domain/AudioVariant.java
@Entity
@Table(name = "audio_variant")
@NoArgsConstructor
public class AudioVariant {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongFormat format;

    @Getter
    @Column(name = "bitrate_kbps", nullable = false)
    private int bitrateKbps;

    @Getter
    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @Getter
    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    /**
     * Internal S3 URI — e.g. "s3://your-bucket/variants/song_42_128k.mp3".
     * Never returned to clients.
     */
    @Getter
    @Column(name = "storage_url", nullable = false)
    private String storageUrl;

    /**
     * The S3 object key used for pre-signing — e.g. "variants/song_42_128k.mp3".
     * Never returned to clients.
     */
    @Getter
    @Column(name = "streaming_key", nullable = false)
    private String streamingKey;

    /** True for the 128 kbps MP3 — served to guests and free users. */
    @Getter
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudioVariantStatus status;

    @Version
    private long version;

    public AudioVariant(Song song, SongFormat format, int bitrateKbps, int durationSec,
                        long fileSizeBytes, String storageUrl, String streamingKey,
                        boolean isDefault) {
        this.song          = song;
        this.format        = format;
        this.bitrateKbps   = bitrateKbps;
        this.durationSec   = durationSec;
        this.fileSizeBytes = fileSizeBytes;
        this.storageUrl    = storageUrl;
        this.streamingKey  = streamingKey;
        this.isDefault     = isDefault;
        this.status        = AudioVariantStatus.PROCESSING;
    }
}
```

### AudioVariantRepository

```java
// repository/AudioVariantRepository.java
public interface AudioVariantRepository extends JpaRepository<AudioVariant, Long> {

    List<AudioVariant> findBySongIdAndStatus(long songId, AudioVariantStatus status);
}
```

---

## Step 17 — Song Domain Changes

Add `processingStatus` to the `Song` entity. The constructor sets it to `PROCESSING` for new
uploads; existing rows default to `ACTIVE` via the Liquibase migration.

```java
// Add to Song.java — new field only, nothing else changes

@Getter
@Setter
@Enumerated(EnumType.STRING)
@Column(name = "processing_status", nullable = false)
private SongProcessingStatus processingStatus;
```

Update the constructor to accept `processingStatus`, or set it explicitly after construction
in `SongService`:

```java
// In SongService.uploadSong(), after building the Song object:
song.setProcessingStatus(SongProcessingStatus.PROCESSING);
```

Add a repository method for the status update:

```java
// In SongRepository.java
@Modifying
@Query("UPDATE Song s SET s.processingStatus = :status WHERE s.id = :id")
void updateProcessingStatus(@Param("id") long id,
                            @Param("status") SongProcessingStatus status);
```

---

## Step 18 — FileStorageService Additions

The core methods from Phase 1 are unchanged. Add one new method for deleting all variants
of a song (called by `SongService.deleteSong`):

```java
// Add to FileStorageService.java

/**
 * Deletes all three standard quality variants from S3 for a given song.
 * Called when a song is permanently deleted.
 */
public void deleteSongVariants(long songId) {
    List<String> keys = List.of(
        variantKey(songId, 128, "mp3"),
        variantKey(songId, 320, "mp3"),
        variantKey(songId, 256, "aac")
    );
    keys.forEach(this::deleteFile);
}
```

Update `SongService.deleteSong` to call this instead of `deleteFile(song.getStorageKey())`:

```java
public void deleteSong(long id) {
    Song song = songRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Song", id));

    // Phase 2: delete all variant files and the original raw key if it still exists
    fileStorageService.deleteSongVariants(id);
    fileStorageService.deleteFile(song.getStorageKey()); // raw/uploads key — safe if already gone

    songRepository.deleteById(id);
}
```

---

## Step 19 — AudioProcessingJob (FFmpeg pipeline)

This async job runs after `SongService.uploadSong()` saves the entity. It downloads the raw
file, transcodes it into three variants, uploads each to S3, creates `AudioVariant` rows, and
flips the song to `ACTIVE`.

### Maven dependency (pom.xml)

```xml
<!-- FFmpeg Java wrapper -->
<dependency>
    <groupId>net.bramp.ffmpeg</groupId>
    <artifactId>ffmpeg</artifactId>
    <version>0.8.0</version>
</dependency>
```

### Enable @Async in Spring Boot

```java
// config/AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("audioProcessingExecutor")
    public Executor audioProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("audio-proc-");
        executor.initialize();
        return executor;
    }
}
```

### AudioProcessingJob

```java
// job/AudioProcessingJob.java
@Component
@Slf4j
public class AudioProcessingJob {

    private final FileStorageService      fileStorageService;
    private final AudioVariantRepository  audioVariantRepository;
    private final SongRepository          songRepository;
    private final S3Client                s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public AudioProcessingJob(
        FileStorageService     fileStorageService,
        AudioVariantRepository audioVariantRepository,
        SongRepository         songRepository,
        S3Client               s3Client
    ) {
        this.fileStorageService     = fileStorageService;
        this.audioVariantRepository = audioVariantRepository;
        this.songRepository         = songRepository;
        this.s3Client               = s3Client;
    }

    @Async("audioProcessingExecutor")
    public void process(long songId, String rawS3Key) {
        Path tempInput  = null;
        List<Path> tempOutputs = new ArrayList<>();

        try {
            log.info("Starting audio processing for songId={}", songId);

            // 1. Download the raw file from S3 to a local temp path
            tempInput = downloadToTemp(rawS3Key);

            // 2. Validate with FFprobe
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult probe = ffprobe.probe(tempInput.toString());
            if (probe.hasError()) {
                throw new AudioProcessingException("Invalid audio file for songId=" + songId);
            }
            int durationSec = (int) probe.getFormat().duration;

            // 3. Transcode into 3 variants
            FFmpeg ffmpeg = new FFmpeg();
            Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NotFoundException("Song", songId));

            List<VariantSpec> specs = List.of(
                new VariantSpec(128, "mp3", "libmp3lame", SongFormat.MP3, true),
                new VariantSpec(320, "mp3", "libmp3lame", SongFormat.MP3, false),
                new VariantSpec(256, "aac", "aac",        SongFormat.AAC, false)
            );

            for (VariantSpec spec : specs) {
                Path tempOut = Files.createTempFile("variant_", "." + spec.format());
                tempOutputs.add(tempOut);

                FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(tempInput.toString())
                    .addOutput(tempOut.toString())
                        .setAudioCodec(spec.codec())
                        .setAudioBitRate(spec.bitrateKbps() * 1000L)
                        .done();

                new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();

                // Upload variant to S3
                String variantKey  = FileStorageService.variantKey(songId, spec.bitrateKbps(), spec.format());
                String contentType = spec.format().equals("mp3") ? "audio/mpeg" : "audio/aac";

                try (InputStream is = Files.newInputStream(tempOut)) {
                    fileStorageService.uploadFile(variantKey, is, Files.size(tempOut), contentType);
                }

                // Persist AudioVariant row
                AudioVariant variant = new AudioVariant(
                    song,
                    spec.songFormat(),
                    spec.bitrateKbps(),
                    durationSec,
                    Files.size(tempOut),
                    "s3://" + bucket + "/" + variantKey,
                    variantKey,
                    spec.isDefault()
                );
                variant.setStatus(AudioVariantStatus.READY);
                audioVariantRepository.save(variant);

                log.info("Variant ready: songId={} {}k.{}", songId, spec.bitrateKbps(), spec.format());
            }

            // 4. Mark song as ACTIVE — now streamable
            songRepository.updateProcessingStatus(songId, SongProcessingStatus.ACTIVE);

            // 5. Delete the raw staging file
            fileStorageService.deleteFile(rawS3Key);

            log.info("Audio processing complete for songId={}", songId);

        } catch (Exception e) {
            log.error("Audio processing failed for songId={}", songId, e);
            songRepository.updateProcessingStatus(songId, SongProcessingStatus.FAILED);
        } finally {
            cleanupTemp(tempInput);
            tempOutputs.forEach(this::cleanupTemp);
        }
    }

    private Path downloadToTemp(String s3Key) throws IOException {
        Path temp = Files.createTempFile("raw_", ".audio");
        s3Client.getObject(
            GetObjectRequest.builder().bucket(bucket).key(s3Key).build(),
            temp
        );
        return temp;
    }

    private void cleanupTemp(Path path) {
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }

    record VariantSpec(int bitrateKbps, String format, String codec,
                       SongFormat songFormat, boolean isDefault) {}
}
```

---

## Step 20 — SongService Upload Changes

Update `uploadSong` to upload to `raw/uploads/` instead of `songs/`, set
`processingStatus = PROCESSING`, and fire the async job:

```java
// service/SongService.java — updated uploadSong

@Transactional
public SongResponse uploadSong(MultipartFile audioFile, SongUploadRequest request) {
    Album album = albumRepository.findById(request.albumId())
        .orElseThrow(() -> new NotFoundException("Album", request.albumId()));

    String uuid      = UUID.randomUUID().toString();
    String rawS3Key  = FileStorageService.rawUploadKey(uuid, audioFile.getOriginalFilename());

    // Upload raw file to staging area — the async job will delete it after processing
    fileStorageService.uploadMultipartFile(rawS3Key, audioFile);

    Song song = new Song(
        album,
        request.name().trim(),
        LocalDateTime.now(),
        request.format(),
        request.duration(),
        audioFile.getSize(),
        rawS3Key   // storageKey holds the raw key until variants are ready
    );
    song.setProcessingStatus(SongProcessingStatus.PROCESSING);

    song = songRepository.save(song);

    // Fire async — returns immediately; artist gets HTTP 201 while FFmpeg runs
    audioProcessingJob.process(song.getId(), rawS3Key);

    return SongResponse.from(song);
}
```

> **Add `AudioProcessingJob` to the `SongService` constructor.** Inject it the same way as the
> repositories — no Lombok, just a constructor parameter.

### SongResponse should expose processingStatus

```java
// dto/SongResponse.java — add this field
SongProcessingStatus processingStatus;

// In SongResponse.from(Song song):
.processingStatus(song.getProcessingStatus())
```

---

## Step 21 — StreamingController Changes

This is the only meaningful change to the existing streaming code. Replace the single line
that signs `song.storageKey` with variant selection logic.

```java
// controller/StreamingController.java — updated getStreamUrl

@GetMapping("/{songId}/stream")
public ResponseEntity<StreamUrlResponse> getStreamUrl(
        @PathVariable long songId,
        @AuthenticationPrincipal UserPrincipal currentUser) {

    Song song = songRepository.findById(songId)
        .orElseThrow(() -> new NotFoundException("Song", songId));

    // Gate: song must be fully processed before any signed URL is issued
    if (song.getProcessingStatus() != SongProcessingStatus.ACTIVE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    // Select the best available variant for this user's role
    List<AudioVariant> variants =
        audioVariantRepository.findBySongIdAndStatus(songId, AudioVariantStatus.READY);

    AudioVariant variant = selectVariant(variants, currentUser.getRole());

    // Sign the variant's streaming key — NOT song.storageKey
    String signedUrl = fileStorageService.generateSignedUrl(variant.getStreamingKey());

    StreamingSession session = playbackService.startSession(
        songId, currentUser.getId(), variant.getId()   // pass variantId — see Step 22
    );

    return ResponseEntity.ok(new StreamUrlResponse(
        signedUrl,
        Instant.now().plus(15, ChronoUnit.MINUTES),
        session.getId(),
        variant.getDurationSec(),
        variant.getBitrateKbps(),   // new fields in StreamUrlResponse
        variant.getFormat().name()
    ));
}

private AudioVariant selectVariant(List<AudioVariant> variants, UserRole role) {
    return switch (role) {
        case GUEST -> variants.stream()
            .filter(AudioVariant::isDefault)
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No default variant available"));

        case USER, ARTIST, ADMIN -> variants.stream()
            .max(Comparator.comparingInt(AudioVariant::getBitrateKbps))
            .orElseThrow(() -> new NotFoundException("No variant available"));
    };
}
```

### Add `AudioVariantRepository` to `StreamingController`'s constructor

```java
public StreamingController(
    SongRepository         songRepository,
    AudioVariantRepository audioVariantRepository,
    FileStorageService     fileStorageService,
    PlaybackService        playbackService
) {
    this.songRepository         = songRepository;
    this.audioVariantRepository = audioVariantRepository;
    this.fileStorageService     = fileStorageService;
    this.playbackService        = playbackService;
}
```

### Updated StreamUrlResponse

```java
// dto/StreamUrlResponse.java — add two new fields for Phase 2
public record StreamUrlResponse(
    String  streamUrl,
    Instant expiresAt,
    long    sessionId,
    long    durationSec,
    int     bitrateKbps,   // new
    String  format         // new — "MP3" or "AAC"
) {}
```

---

## Step 22 — StreamingSession variant_id FK

Update `PlaybackService.startSession` to accept and store the variant id.

```java
// domain/StreamingSession.java — add the new field

@Getter
@Setter
@Column(name = "variant_id")
private Long variantId;   // nullable — null for Phase 1 sessions
```

```java
// service/PlaybackService.java — updated startSession signature

public StreamingSession startSession(long songId, long userId, Long variantId) {
    StreamingSession session = new StreamingSession(userId, songId, null, LocalDateTime.now());
    session.setVariantId(variantId);
    session = sessionRepository.save(session);
    logEvent(session, StreamingEventType.START, 0L, null, null);
    return session;
}
```

The Phase 1 call in `StreamingController` used `startSession(songId, userId)` — update it to
`startSession(songId, userId, variant.getId())`. The Phase 1 signature is no longer needed once
this is in place, but you can keep it as an overload with `variantId = null` for backward
compatibility during the transition.

---

## Step 23 — Angular Player Changes

Add two new fields to `StreamUrlResponse` interface and optionally display quality info:

```typescript
// services/streaming.service.ts — updated interface
export interface StreamUrlResponse {
  streamUrl:   string;
  expiresAt:   string;
  sessionId:   number;
  durationSec: number;
  bitrateKbps: number;   // new
  format:      string;   // new
}
```

Expose quality in the player template if desired:

```typescript
// In AudioPlayerComponent:
bitrateKbps = 0;
format      = '';

// In loadSong():
this.bitrateKbps = response.bitrateKbps;
this.format      = response.format;
```

```html
<!-- Optional quality badge in template -->
<span class="quality-badge">{{ format }} {{ bitrateKbps }}k</span>
```

No other Angular changes are needed — the session/event calls, URL refresh logic, and
`ngOnDestroy` cleanup are identical.

---

---

## Security Checklist

Go through this before connecting a real S3 bucket.

### S3
- [ ] All public access is blocked (4 boxes checked under Permissions → Block public access)
- [ ] No bucket policy grants `s3:GetObject` to `Principal: "*"`
- [ ] CORS `AllowedOrigins` is your production domain only — `localhost` removed before go-live
- [ ] `Range` is in `AllowedHeaders` and `Content-Range`, `Accept-Ranges` in `ExposeHeaders`
- [ ] Lifecycle rule auto-deletes `raw/uploads/` objects after 1 day (safety net if the async job crashes)
- [ ] Server-side encryption (SSE-S3) is enabled

### IAM
- [ ] IAM user has no console access (programmatic only)
- [ ] Policy scoped to exactly: `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:ListBucket` on your bucket ARN
- [ ] Access keys stored as environment variables — never in `application.properties` or committed to git
- [ ] Rotate access keys every 90 days

### Spring Boot
- [ ] `song.storageKey` is **never returned** in any `SongResponse` DTO
- [ ] `audioVariant.streamingKey` and `storageUrl` are **never returned** in any DTO
- [ ] Signed URL is generated only after Spring Security has validated the JWT
- [ ] `processingStatus == ACTIVE` is checked before generating a signed URL (Phase 2)
- [ ] File MIME type validated on upload — consider Apache Tika for deep inspection beyond `Content-Type` header
- [ ] File size validated on upload (≤ 100 MB)

### Angular
- [ ] `streamUrl` is never logged to the browser console
- [ ] `streamUrl` is never stored in `localStorage` or `sessionStorage`
- [ ] `streamUrl` is used only as `<audio src>` — never displayed or linked

---

## Cost Optimization

| Phase | Scale | Strategy |
|---|---|---|
| 1 — Launch | 0–10k songs, small audience | S3 direct signed URLs, no CDN. Estimated $5–30/month (storage + egress). |
| 2 — Growth | 10k+ songs, global audience | Add CloudFront CDN in front of S3. Caches popular variants at edge, reduces S3 egress, improves latency. |
| 3 — Scale | Large catalog, high concurrency | HLS chunked streaming (`.ts` segments). Redis to cache signed URLs per user per song — avoid regenerating on every seek. |

**Storage estimate at 1,000 songs with 3 variants each:**

```
1,000 songs × 3 variants × 8 MB avg = 24 GB
Monthly storage: 24 × $0.023 ≈ $0.55

Egress at 100 full streams/day:
100 × 8 MB × 30 days = 24 GB/month → ~$2.16
```

At initial scale, cost is negligible. Egress grows linearly with streams.

---

*BoomVox S3 guide — adapted to the actual `Song` domain, `streaming_session` / `streaming_event`
schema, and both Phase 1 (single-file) and Phase 2 (audio_variants + FFmpeg) implementations.*
