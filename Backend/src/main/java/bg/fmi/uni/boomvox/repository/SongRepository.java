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
import org.springframework.transaction.annotation.Transactional;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByAlbumId(Long id);

    List<Song> findByAlbum(Album album);

    List<Song> findByAlbumAuthorId(long authorId);

    List<Song> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Song s SET s.duration = :duration WHERE s.id = :id")
    void updateDuration(@Param("id") long id, @Param("duration") long duration);

    @Query(value = """
    SELECT DISTINCT s.*
    FROM boomvox.song s
    LEFT JOIN boomvox.song_tag st ON st.song_id = s.id
    LEFT JOIN boomvox.album a ON a.id = s.album_id
    WHERE s.processing_status = 'ACTIVE'
      AND (:query IS NULL
          OR LOWER(s.name) ILIKE CONCAT('%', :query, '%')
          OR LOWER(a.name) ILIKE CONCAT('%', :query, '%'))
      AND (:albumId IS NULL OR s.album_id = :albumId)
      AND (:authorId IS NULL OR a.user_id = :authorId)
      AND (:genre IS NULL OR a.genre = CAST(:genre AS VARCHAR))
      AND (:format IS NULL OR s.format = CAST(:format AS VARCHAR))
      AND (:tagId IS NULL OR st.tag_id = :tagId)
    """, nativeQuery = true)
    List<Song> searchCatalog(
        @Param("query") String query,
        @Param("albumId") Long albumId,
        @Param("authorId") Long authorId,
        @Param("genre") String genre,
        @Param("format") String format,
        @Param("tagId") Long tagId
    );

    @Modifying
    @Query("UPDATE Song s SET s.processingStatus = :status WHERE s.id = :id")
    void updateProcessingStatus(@Param("id") long id,
                                @Param("status") SongProcessingStatus status);

    List<Song> findByProcessingStatus(SongProcessingStatus status);
}
