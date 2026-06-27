package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Recommendation;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.ids.RecommendationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, RecommendationId> {

    List<Recommendation> findByUserIdOrderByPercentDesc(Long id);

    List<Recommendation> findByUserOrderByPercentDesc(User user);

    void deleteByUser(User user);

    Long countBySongId(Long id);

    Long countBySong(Song song);
}
