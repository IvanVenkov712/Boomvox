package bg.fmi.uni.boomvox.dto;

public record SongStatsResponse(
    long id,
    long songId,
    long ratingsCount,
    double avgRating,
    long playsCount,
    long playlistsCount,
    long recommendationsCount
) {
}
