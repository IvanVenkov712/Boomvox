package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Rating;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.ids.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    List<Rating> findByUserId(Long id);

    List<Rating> findByUser(User user);

    List<Rating> findByUserIdOrderByLastUpdatedAtDesc(Long id);

    List<Rating> findByUserOrderByLastUpdatedAtDesc(User user);

    List<Rating> findBySongId(Long id);

    List<Rating> findBySong(Song song);

    List<Rating> findBySongIdOrderByLastUpdatedAtDesc(Long id);

    List<Rating> findBySongOrderByLastUpdatedAtDesc(Song song);

    Long countBySongId(Long id);

    Long countBySong(Song song);

    @Query("select avg(r.grade) from Rating r where r.song.id = ?1")
    Double findAverageSongRatingBySongId(Long id);

    @Query("select avg(r.grade) from Rating r where r.song = ?1")
    Double findAverageSongRatingBySong(Song song);

}
