package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.ListeningHistory;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.StreamingEvent;
import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.SessionStatus;
import bg.fmi.uni.boomvox.enums.StreamingEventType;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.ListeningHistoryRepository;
import bg.fmi.uni.boomvox.repository.StreamingEventRepository;
import bg.fmi.uni.boomvox.repository.StreamingSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
public class PlaybackService {

    private final StreamingSessionRepository sessionRepository;
    private final StreamingEventRepository eventRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;

    public PlaybackService(
        StreamingSessionRepository sessionRepository,
        StreamingEventRepository eventRepository,
        ListeningHistoryRepository listeningHistoryRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.listeningHistoryRepository = listeningHistoryRepository;
    }

    /**
     * Opens a new ACTIVE session and logs a START event.
     * Called by StreamingController when a signed URL is issued.
     */
    public StreamingSession startSession(Song song, User user, Long variantId) {
        StreamingSession session = new StreamingSession(user, song, null, LocalDateTime.now());
        session.setVariantId(variantId);
        session = sessionRepository.save(session);
        logEvent(session, StreamingEventType.START, 0L, null, null);
        return session;
    }

    /**
     * Logs any mid-session event (PAUSE, RESUME, SEEK, REPLAY).
     * Called from StreamingSessionController on POST /api/streaming/events.
     */
    public void logEvent(StreamingSession session, StreamingEventType type,
                         long positionSec, Long seekFromSec, Long seekToSec) {
        eventRepository.save(
            new StreamingEvent(session, type, positionSec, seekFromSec, seekToSec)
        );
    }

    /**
     * Closes a session with its final status.
     * Called from StreamingSessionController on PATCH /api/streaming/sessions/{id}.
     */
    public void endSession(long sessionId, SessionStatus finalStatus) {
        StreamingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("StreamingSession", sessionId));

        LocalDateTime endedAt = LocalDateTime.now();
        session.setStatus(finalStatus);
        session.setEndedAt(endedAt);
        sessionRepository.save(session);

        if (!listeningHistoryRepository.existsBySessionId(sessionId)) {
            long elapsedSeconds = Duration.between(session.getStartedAt(), endedAt).toSeconds();
            long listenedDuration = Math.min(Math.max(elapsedSeconds, 0), session.getSong().getDuration());
            listeningHistoryRepository.save(new ListeningHistory(session, endedAt, listenedDuration));
        }
    }
}
