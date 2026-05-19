package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByAuthorId(Long userId);

    List<Album> findByAuthor(User user);

    List<Album> findByGenre(Genre genre);

    List<Album> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT a
        FROM Album a
        WHERE (:query IS NULL
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.author.username) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:authorId IS NULL OR a.author.id = :authorId)
          AND (:genre IS NULL OR a.genre = :genre)
        """)
    List<Album> searchCatalog(
        @Param("query") String query,
        @Param("authorId") Long authorId,
        @Param("genre") Genre genre
    );
}
