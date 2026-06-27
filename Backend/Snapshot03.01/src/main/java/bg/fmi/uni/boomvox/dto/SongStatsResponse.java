package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.SongStats;

public record SongStatsResponse(
    long id,
    long ratingsCount,
    double avgRating,
    long playsCount,
    long playlistsCount,
    long recommendationsCount
) {
    public static SongStatsResponse from(SongStats stats) {
        return new SongStatsResponse(
            stats.getId(),
            stats.getRatingsCount(),
            stats.getAvgRating(),
            stats.getPlaysCount(),
            stats.getPlaylistsCount(),
            stats.getRecommendationsCount()
        );
    }
}
