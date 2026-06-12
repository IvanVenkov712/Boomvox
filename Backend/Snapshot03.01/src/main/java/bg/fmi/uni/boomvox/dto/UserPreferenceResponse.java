package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.Genre;

import java.util.Map;

public record UserPreferenceResponse(
    long userId,
    Map<Genre, Double> genreScores,
    Map<Long, Double> artistScores,
    Map<String, Double> tagScores
) {
}
