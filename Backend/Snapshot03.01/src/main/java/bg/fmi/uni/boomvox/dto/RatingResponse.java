package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
    long userId,
    long songId,
    int grade,
    String comment,
    LocalDateTime lastUpdatedAt
) {
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
            rating.getUser().getId(),
            rating.getSong().getId(),
            rating.getGrade(),
            rating.getComment(),
            rating.getLastUpdatedAt()
        );
    }
}
