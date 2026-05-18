package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record RecommendationRequest(
    @Positive
    long userId,

    @Positive
    long songId,

    @Min(0)
    @Max(100)
    int percent
) {
}
