package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.StreamingEvent;
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
    public static StreamingEventResponse from(StreamingEvent event) {
        return new StreamingEventResponse(
            event.getId(),
            event.getSession().getId(),
            event.getType(),
            event.getPositionSec(),
            event.getSeekFromSec(),
            event.getSeekToSec(),
            event.getCreatedAt()
        );
    }
}
