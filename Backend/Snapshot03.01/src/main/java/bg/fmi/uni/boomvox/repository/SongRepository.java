package bg.fmi.uni.boomvox.repository;

import java.util.List;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByAlbumId(Long id);

    List<Song> findByAlbum(Album album);

    List<Song> findByAlbumAuthorId(long authorId);

    List<Song> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT DISTINCT s
        FROM Song s
        LEFT JOIN SongTag st ON st.song = s
        WHERE (:query IS NULL
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(s.album.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:albumId IS NULL OR s.album.id = :albumId)
          AND (:authorId IS NULL OR s.album.author.id = :authorId)
          AND (:genre IS NULL OR s.album.genre = :genre)
          AND (:format IS NULL OR s.format = :format)
          AND (:tagId IS NULL OR st.tag.id = :tagId)
        """)
    List<Song> searchCatalog(
        @Param("query") String query,
        @Param("albumId") Long albumId,
        @Param("authorId") Long authorId,
        @Param("genre") Genre genre,
        @Param("format") SongFormat format,
        @Param("tagId") Long tagId
    );

    @Modifying
    @Query("UPDATE Song s SET s.processingStatus = :status WHERE s.id = :id")
    void updateProcessingStatus(@Param("id") long id,
                                @Param("status") SongProcessingStatus status);
}
