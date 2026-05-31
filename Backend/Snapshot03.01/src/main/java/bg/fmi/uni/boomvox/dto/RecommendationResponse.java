package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Recommendation;

public record RecommendationResponse(
    long userId,
    long songId,
    int percent
) {
    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
            recommendation.getUser().getId(),
            recommendation.getSong().getId(),
            recommendation.getPercent()
        );
    }
}
