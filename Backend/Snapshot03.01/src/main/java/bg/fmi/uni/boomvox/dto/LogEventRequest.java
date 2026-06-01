package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.StreamingEventType;

public record LogEventRequest(
    Long               sessionId,
    StreamingEventType type,
    long               positionSec,
    Long               seekFromSec,
    Long               seekToSec
) {}
