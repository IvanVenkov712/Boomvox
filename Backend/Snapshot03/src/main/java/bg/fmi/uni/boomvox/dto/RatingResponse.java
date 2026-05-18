package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record RatingResponse(
    long userId,
    long songId,
    int grade,
    String comment,
    LocalDateTime lastUpdatedAt
) {
}
