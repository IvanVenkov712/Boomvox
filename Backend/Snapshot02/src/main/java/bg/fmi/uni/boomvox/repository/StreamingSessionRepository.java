package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Playlist;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StreamingSessionRepository extends JpaRepository<StreamingSession, Long> {
    List<StreamingSession> findByUserId(Long id);

    List<StreamingSession> findByUser(User user);

    List<StreamingSession> findBySongId(Long id);

    List<StreamingSession> findBySong(Song song);

    List<StreamingSession> findByPlaylistId(Long id);

    List<StreamingSession> findByPlaylist(Playlist playlist);

    List<StreamingSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    List<StreamingSession> findByUserAndStatus(User user, SessionStatus status);

    List<StreamingSession> findByUserIdAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<StreamingSession> findByUserAndStartedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    List<StreamingSession> findByUserIdAndEndedAtNotNull(Long id);

    List<StreamingSession> findByUserAndEndedAtNotNull(User user);

    List<StreamingSession> findByUserIdAndEndedAtNull(Long id);

    List<StreamingSession> findByUserAndEndedNull(User user);

    List<StreamingSession> findBySongIdAndStatus(Long songId, SessionStatus status);

    List<StreamingSession> findBySongAndStatus(Song song, SessionStatus status);

    List<StreamingSession> findBySongIdAndStartedAtBetween(Long songId, LocalDateTime start, LocalDateTime end);

    List<StreamingSession> findBySongAndStartedAtBetween(Song song, LocalDateTime start, LocalDateTime end);

    List<StreamingSession> findBySongIdAndEndedAtNotNull(Long id);

    List<StreamingSession> findBySongAndEndedAtNotNull(Song song);

    List<StreamingSession> findBySongIdAndEndedAtNull(Long id);

    List<StreamingSession> findBySongAndEndedNull(Song user);

    Long countBySongId(Long id);

    Long countBySong(Song song);

}
