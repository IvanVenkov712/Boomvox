package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.enums.SessionStatus;

import java.time.LocalDateTime;

public record StreamingSessionResponse(
    int id,
    long userId,
    long songId,
    Long playlistId,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    SessionStatus status
) {
    public static StreamingSessionResponse from(StreamingSession session) {
        Long playlistId = session.getPlaylist() == null ? null : session.getPlaylist().getId();

        return new StreamingSessionResponse(
            session.getId(),
            session.getUser().getId(),
            session.getSong().getId(),
            playlistId,
            session.getStartedAt(),
            session.getEndedAt(),
            session.getStatus()
        );
    }
}
