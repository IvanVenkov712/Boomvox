package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.ListeningHistory;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {

    List<ListeningHistory> findByUserIdOrderByListenedAtDesc(Long userId);

    List<ListeningHistory> findByUserOrderByListenedAtDesc(User user);

    List<ListeningHistory> findBySongIdOrderByListenedAtDesc(Long songId);

    List<ListeningHistory> findBySongOrderByListenedAtDesc(Song song);

    boolean existsBySessionId(Long sessionId);

    long countByUserIdAndSongId(Long userId, Long songId);
}
