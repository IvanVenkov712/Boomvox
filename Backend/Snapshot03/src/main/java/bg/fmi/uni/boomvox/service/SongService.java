package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.dto.SongRequest;
import bg.fmi.uni.boomvox.dto.SongResponse;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.AlbumRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.TagRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SongService extends BaseService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public SongService(
        SongRepository songRepository,
        AlbumRepository albumRepository,
        UserRepository userRepository,
        TagRepository tagRepository
    ) {
        this.songRepository = songRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public SongResponse uploadSong(SongRequest request) {
        Album album = findAlbum(request.albumId());
        Song song = new Song(
            album,
            request.name().trim(),
            LocalDateTime.now(),
            request.format(),
            request.duration(),
            request.fileSize(),
            request.storageKey().trim()
        );

        return SongResponse.from(songRepository.save(song));
    }

    @Transactional(readOnly = true)
    public SongResponse getSongById(long id) {
        return SongResponse.from(findSong(id));
    }

    @Transactional(readOnly = true)
    public List<SongResponse> browseSongs() {
        return songRepository.findAll().stream()
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

        return songRepository.searchCatalog(normalizeQuery(query), albumId, authorId, genre, format, tagId).stream()
            .map(SongResponse::from)
            .toList();
    }

    public void deleteSong(long id) {
        if (!songRepository.existsById(id)) {
            throw new NotFoundException("Song", id);
        }

        songRepository.deleteById(id);
    }

    private Song findSong(long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));
    }

    private Album findAlbum(long id) {
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
}
