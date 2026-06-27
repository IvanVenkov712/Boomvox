package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.StreamingEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record StreamingEventRequest(
    @Positive
    int sessionId,

    @NotNull
    StreamingEventType type,

    @PositiveOrZero
    long positionSec,

    @PositiveOrZero
    Long seekFromSec,

    @PositiveOrZero
    Long seekToSec
) {
    public static StreamingEventRequest of(int sessionId, StreamingEventType type, long positionSec,
                                           Long seekFromSec, Long seekToSec) {
        return new StreamingEventRequest(sessionId, type, positionSec, seekFromSec, seekToSec);
    }
}
