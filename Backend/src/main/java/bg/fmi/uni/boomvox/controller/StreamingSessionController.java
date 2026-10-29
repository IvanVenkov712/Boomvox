package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.dto.LogEventRequest;
import bg.fmi.uni.boomvox.dto.UpdateSessionRequest;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.StreamingSessionRepository;
import bg.fmi.uni.boomvox.service.PlaybackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/streaming")
public class StreamingSessionController {

    private final PlaybackService            playbackService;
    private final StreamingSessionRepository sessionRepository;

    public StreamingSessionController(
        PlaybackService playbackService,
        StreamingSessionRepository sessionRepository
    ) {
        this.playbackService    = playbackService;
        this.sessionRepository  = sessionRepository;
    }

    /**
     * PATCH /api/streaming/sessions/{id}
     * Angular calls this when playback ends (COMPLETED, SKIPPED, or ABANDONED).
     */
    @PatchMapping("/sessions/{id}")
    public ResponseEntity<Void> updateSession(
        @PathVariable long id,
        @Valid @RequestBody UpdateSessionRequest request) {

        playbackService.endSession(id, request.status());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/streaming/events
     * Angular calls this on PAUSE, RESUME, SEEK, REPLAY — fire-and-forget from the client.
     */
    @PostMapping("/events")
    public ResponseEntity<Void> logEvent(@Valid @RequestBody LogEventRequest request) {
        StreamingSession session = sessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new NotFoundException("StreamingSession", request.sessionId()));

        playbackService.logEvent(
            session,
            request.type(),
            request.positionSec(),
            request.seekFromSec(),
            request.seekToSec()
        );
        return ResponseEntity.noContent().build();
    }
}
