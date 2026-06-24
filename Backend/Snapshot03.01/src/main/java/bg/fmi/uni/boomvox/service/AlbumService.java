package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.AlbumRequest;
import bg.fmi.uni.boomvox.dto.AlbumResponse;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.AlbumRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlbumService extends BaseService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    public AlbumService(AlbumRepository albumRepository, UserRepository userRepository) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    public AlbumResponse createAlbum(AlbumRequest request) {
        User author = findUser(request.authorId());
        Album album = new Album(request.name().trim(), LocalDateTime.now(), author, request.genre());

        return AlbumResponse.from(albumRepository.save(album));
    }

    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(long id) {
        return AlbumResponse.from(findAlbum(id));
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> browseAlbums() {
        return albumRepository.findAll().stream()
            .map(AlbumResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> searchAlbums(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return browseAlbums();
        }

        return albumRepository.findByNameContainingIgnoreCase(normalizedQuery).stream()
            .map(AlbumResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> filterAlbums(String query, Long authorId, Genre genre) {
        if (authorId != null && !userRepository.existsById(authorId)) {
            throw new NotFoundException("User", authorId);
        }

        return albumRepository.searchCatalog(normalizeQuery(query), authorId, genre).stream()
            .map(AlbumResponse::from)
            .toList();
    }

    public void deleteAlbum(long id) {
        if (!albumRepository.existsById(id)) {
            throw new NotFoundException("Album", id);
        }

        albumRepository.deleteById(id);
    }

    private Album findAlbum(long id) {
        return albumRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Album", id));
    }

    private User findUser(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User", id));
    }

    public AlbumResponse updateAlbum(long id, AlbumRequest request) {
        Album album = findAlbum(id);
        album.update(request.name().trim(), request.genre());
        return AlbumResponse.from(albumRepository.save(album));
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> getAlbumsByAuthor(long authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("User", authorId);
        }
        return albumRepository.findByAuthorId(authorId).stream()
            .map(AlbumResponse::from)
            .toList();
    }
}
