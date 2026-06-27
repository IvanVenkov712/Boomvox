package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.dto.SongRequest;
import bg.fmi.uni.boomvox.dto.SongResponse;
import bg.fmi.uni.boomvox.dto.SongUploadRequest;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.job.AudioProcessingJob;
import bg.fmi.uni.boomvox.repository.AlbumRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.TagRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SongService extends BaseService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final AudioProcessingJob audioProcessingJob;

    public SongService(
        SongRepository songRepository,
        AlbumRepository albumRepository,
        UserRepository userRepository,
        TagRepository tagRepository,
        FileStorageService fileStorageService,
        AudioProcessingJob audioProcessingJob
    ) {
        this.songRepository = songRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.fileStorageService = fileStorageService;
        this.audioProcessingJob = audioProcessingJob;
    }

    public SongResponse uploadSong(MultipartFile audioFile, SongUploadRequest request) {
        Album album = findAlbum(request.albumId());
        SongFormat format = audioFile.getContentType().equals("audio/mpeg") ? SongFormat.MP3 : SongFormat.WAV;

        String uuid      = UUID.randomUUID().toString();
        String rawS3Key = FileStorageService.rawUploadKey(uuid, audioFile.getOriginalFilename());

        fileStorageService.uploadMultipartFile(rawS3Key, audioFile);

        Song song = new Song(
            album,
            request.name().trim(),
            LocalDateTime.now(),
            format,
            request.duration(),
            audioFile.getSize(),
            rawS3Key
        );

        song.setProcessingStatus(SongProcessingStatus.PROCESSING);

        song = songRepository.save(song);

        audioProcessingJob.process(song.getId(), rawS3Key);

        return SongResponse.from(song);
    }

    @Transactional(readOnly = true)
    public SongResponse getSongById(long id) {
        return SongResponse.from(findSong(id));
    }

    @Transactional(readOnly = true)
    public List<SongResponse> browseSongs() {
        return songRepository.findByProcessingStatus(SongProcessingStatus.ACTIVE).stream()
            .map(SongResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByAlbum(long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("Album", albumId);
        }

        return songRepository.findByAlbumId(albumId).stream()
            .map(SongResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SongResponse> searchSongs(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return browseSongs();
        }

        return songRepository.findByNameContainingIgnoreCase(normalizedQuery).stream()
            .map(SongResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SongResponse> filterSongs(
        String query,
        Long albumId,
        Long authorId,
        Genre genre,
        SongFormat format,
        Long tagId
    ) {
        validateOptionalReferences(albumId, authorId, tagId);

        return songRepository.searchCatalog(normalizeQuery(query), albumId, authorId, genre != null ? genre.name() : null,
                format != null ? format.name() : null, tagId).stream()
            .map(SongResponse::from)
            .toList();
    }
    public SongResponse updateSong(long id, SongRequest request) {
        Song song = songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));

        Album album = albumRepository.findById(request.albumId())
            .orElseThrow(() -> new NotFoundException("Album", request.albumId()));

        song.update(request.name().trim(), request.duration(), request.fileSize(), request.storageKey());

        return SongResponse.from(songRepository.save(song));
    }

    public void deleteSong(long id) {
        Song song = songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));

        fileStorageService.deleteSongVariants(id);
        fileStorageService.deleteFile(song.getStorageKey());
        songRepository.deleteById(id);
    }

    private Song findSong(long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));
    }

    private Album findAlbum(Long id) {
        if (id == null) return null;
        return albumRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Album", id));
    }

    private void validateOptionalReferences(Long albumId, Long authorId, Long tagId) {
        if (albumId != null && !albumRepository.existsById(albumId)) {
            throw new NotFoundException("Album", albumId);
        }

        if (authorId != null && !userRepository.existsById(authorId)) {
            throw new NotFoundException("User", authorId);
        }

        if (tagId != null && !tagRepository.existsById(tagId)) {
            throw new NotFoundException("Tag", tagId);
        }
    }

    private String resolveExtension(SongFormat format) {
        return switch (format) {
            case MP3  -> "mp3";
            case WAV  -> "wav";
            case FLAC -> "flac";
            case AAC ->  "aac";
            // extend as SongFormat grows
        };
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByArtist(long artistId) {
        if (!userRepository.existsById(artistId)) {
            throw new NotFoundException("User", artistId);
        }
        return songRepository.findByAlbumAuthorId(artistId).stream()
            .map(SongResponse::from)
            .toList();
    }
}
