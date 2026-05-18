package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.StreamingEventType;

import java.time.LocalDateTime;

public record StreamingEventResponse(
    long id,
    int sessionId,
    StreamingEventType type,
    long positionSec,
    Long seekFromSec,
    Long seekToSec,
    LocalDateTime createdAt
) {
}
