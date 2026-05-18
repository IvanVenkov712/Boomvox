package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StreamingSessionRequest(
    @Positive
    long userId,

    @Positive
    long songId,

    @Positive
    Long playlistId,

    @NotNull
    SessionStatus status
) {
    public static StreamingSessionRequest of(long userId, long songId, Long playlistId, SessionStatus status) {
        return new StreamingSessionRequest(userId, songId, playlistId, status);
    }
}
