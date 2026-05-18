package bg.fmi.uni.boomvox.dto;

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
}
