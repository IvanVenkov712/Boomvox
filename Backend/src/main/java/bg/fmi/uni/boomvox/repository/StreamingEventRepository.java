package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.StreamingEvent;
import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.enums.StreamingEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StreamingEventRepository extends JpaRepository<StreamingEvent, Long> {

    List<StreamingEvent> findBySessionId(Long id);

    List<StreamingEvent> findBySession(StreamingSession session);

    List<StreamingEvent> findBySessionIdAndType(Long sessionId, StreamingEventType type);

    List<StreamingEvent> findBySessionAndType(StreamingSession session, StreamingEventType type);

    List<StreamingEvent> findBySessionIdAndCreatedAtAfter(Long sessionId, LocalDateTime time);

    List<StreamingEvent> findBySessionAndCreatedAtAfter(StreamingSession session, LocalDateTime time);

    List<StreamingEvent> findBySessionIdAndTypeAndCreatedAtAfter(Long sessionId, StreamingEventType type, LocalDateTime time);

    List<StreamingEvent> findBySessionAndTypeAndCreatedAtAfter(StreamingSession session, StreamingEventType type, LocalDateTime time);

}
