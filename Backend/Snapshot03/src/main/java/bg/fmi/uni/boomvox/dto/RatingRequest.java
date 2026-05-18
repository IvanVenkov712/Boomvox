package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RatingRequest(
    @Positive
    long userId,

    @Positive
    long songId,

    @Min(0)
    @Max(10)
    int grade,

    @NotBlank
    String comment
) {
    public static RatingRequest of(long userId, long songId, int grade, String comment) {
        return new RatingRequest(userId, songId, grade, comment);
    }
}
