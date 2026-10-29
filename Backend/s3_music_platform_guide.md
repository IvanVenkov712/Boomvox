# S3 storage and audio processing

This guide documents the current implementation. It replaces the earlier proposed integration tutorial and its cost estimates. No hosted application, production bucket name, or bucket region is established by the repository. Supply your own bucket and credentials using the [configuration reference](../docs/deployment.md#configuration-reference).

## Runtime model

Spring Boot uploads audio to S3 and generates temporary signed GET URLs. The browser plays those URLs directly; the API does not proxy audio bytes. Audio processing runs in the backend JVM through an in-memory executor, using FFmpeg/FFprobe and temporary files on the same host.

| Object | Key pattern | Lifecycle |
| --- | --- | --- |
| Raw upload | `raw/uploads/song_{uuid}_{sanitisedFilename}` | Uploaded before processing; deleted after successful processing |
| Default MP3 | `variants/song_{songId}_128k.mp3` | 128 kbps, used for authenticated `GUEST` accounts |
| High bitrate MP3 | `variants/song_{songId}_320k.mp3` | Highest-bitrate choice for ordinary users/authors/admins |
| AAC | `variants/song_{songId}_256k.aac` | Generated and stored; selection currently uses highest bitrate, not codec preference |

`FileStorageService` also has `songs/`, `covers/`, and `avatars/` key builders. Their existence does not imply corresponding upload endpoints. The current audio upload path uses `raw/uploads/` followed by `variants/`.

## Configuration and permissions

Create a bucket using the [AWS bucket creation instructions](https://docs.aws.amazon.com/AmazonS3/latest/userguide/create-bucket-overview.html), retain Block Public Access, and record its name and region. The current client needs object put/get/delete permissions scoped to that bucket's raw and variant prefixes. `S3Config` constructs both the client and presigner with the configured region and static basic credentials. It does not read a session token, use instance roles, or implement a configurable local S3 endpoint.

Copy [`.env.example`](.env.example) to an ignored `.env` from `Backend/` if no `.env` exists. Set `aws.region`, `aws.accessKeyId`, `aws.secretAccessKey`, `aws.s3.bucket`, and `aws.s3.signedUrlExpiryMinutes`. Alternatively, set the explicitly mapped `AWS_*` process variables. The Java-properties file format and all variable purposes are in [deployment/configuration](../docs/deployment.md).

No AWS or database credential belongs in the Angular source. The browser receives only temporary playback URLs. S3 access is authorised by the URL signature, independently of the application's JWT.

If browser requests use CORS, allow the frontend's exact origin and the GET/HEAD operations needed for audio. For local development that origin is `http://localhost:4200`. The following is an example bucket CORS rule, not an automatically installed configuration:

```json
[
  {
    "AllowedOrigins": ["http://localhost:4200"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedHeaders": ["Range"],
    "ExposeHeaders": ["Content-Range", "Accept-Ranges", "Content-Length"],
    "MaxAgeSeconds": 3600
  }
]
```

Browser audio can use different cross-origin request modes; this rule does not grant public bucket access or replace signed URL permissions. API CORS in `SecurityConfig` is separate from S3 CORS. The backend's server-to-server uploads do not require a browser PUT rule.

## Upload and processing

1. An intended author/admin sends `POST /api/songs` with multipart `audio` and a JSON `metadata` part. Role enforcement is incomplete; see [known problems](../docs/known-problems.md#authentication-and-access-control).
2. `SongController` checks the exact MIME type (`audio/mpeg` or `audio/wav`) and a 100 MiB file ceiling. The dev servlet file/request limits are both `100MB` and include request overhead.
3. `SongService` uploads a raw object, saves a song with `PROCESSING`, and submits `AudioProcessingJob`. The 201 response does not mean the audio is ready.
4. The worker downloads the raw object to temporary disk and runs FFprobe for duration, then FFmpeg for three encoded variants.
5. It uploads each variant and creates a `READY` `AudioVariant` row. After all variants, it updates measured duration, marks the song `ACTIVE`, and deletes the raw object.
6. A caught processing failure attempts to mark the song `FAILED`. The worker cleans known temporary paths, but it does not roll back S3 writes or implement retries.

The upload is transactional but job submission happens before commit. A worker can therefore read before the new song is committed. In-memory jobs also do not survive process restart. Avoid treating this as a durable processing queue; [known problems](../docs/known-problems.md#audio-and-playback) tracks the required fixes.

The frontend polls song details every three seconds. Catalogue browsing includes active songs; a song detail request can expose processing/failed status.

## Playback and expiry

`GET /api/songs/{songId}/stream` requires a JWT, checks `ACTIVE`, selects a `READY` variant, signs its object key, and creates a session with `START`. It returns:

```text
streamUrl, expiresAt, sessionId, durationSec, bitrateKbps, format
```

No usable variant raises a custom exception. A song still processing or failed returns HTTP 503. Use `streamUrl` as the browser audio source; keep it in memory rather than durable client storage.

The signer uses configurable expiry minutes, default 15. The controller currently reports `expiresAt` as 15 minutes regardless of that value. Keep the default until fixed. It also overrides response content type to `audio/mpeg` for every variant. Requesting another stream URL creates another session; it is not a pure renewal operation.

Pause/resume/seek events go to `/api/streaming/events`. Terminal status goes to `/api/streaming/sessions/{id}`. Backend terminal statuses are `COMPLETED`, `SKIPPED`, and `ABANDONED`; the UI's current `INTERRUPTED` value is incompatible.

## Cleanup and troubleshooting

Deleting a song attempts to delete the three standard variant keys and its stored raw key. Failed S3 deletions are logged without aborting the operation. PostgreSQL foreign keys can still reject parent deletion, so file/database consistency is not guaranteed. The song's original storage key remains in its DTO even after the corresponding raw object is deleted.

| Symptom | Checks |
| --- | --- |
| Upload rejected before the controller | File/request size limits and multipart overhead; both need explicit configuration outside dev |
| Upload rejected as unsupported | Exact MIME type must be `audio/mpeg` or `audio/wav` |
| Processing becomes `FAILED` | FFmpeg/FFprobe availability, codecs, backend logs, temp disk space, bucket/credential permissions, async commit timing |
| Song remains `PROCESSING` after restart | No durable job recovery exists; inspect DB and S3 state before repairing/retrying |
| Signed URL is rejected | Correct object key/region, signer credentials, time/expiry, private-object permissions |
| Browser cannot play or seek | Reachable object, valid URL, browser audio support, request mode/CORS and Range response behaviour |
| Expiry differs from `expiresAt` | Fixed controller expiry versus configured signing duration |
| Raw or partial variant objects accumulate | Failure cleanup is incomplete; a bucket lifecycle policy for raw uploads may be configured externally, but none is managed by this repository |

There is no implemented CDN, HLS/DASH pipeline, adaptive quality selection, or signed URL cache. Performance/cost depends on your bucket, region, catalogue, and traffic; no current hosting cost has been measured.
