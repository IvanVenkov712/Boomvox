package bg.fmi.uni.boomvox.dto;

public record RecommendationResponse(
    long userId,
    long songId,
    int percent
) {
}
