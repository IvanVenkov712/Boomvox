package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FavouritesListSongRequest(
    @Positive
    long favouritesListId,

    @Positive
    long songId,

    @PositiveOrZero
    int position
) {
    public static FavouritesListSongRequest of(long favouritesListId, long songId, int position) {
        return new FavouritesListSongRequest(favouritesListId, songId, position);
    }
}
