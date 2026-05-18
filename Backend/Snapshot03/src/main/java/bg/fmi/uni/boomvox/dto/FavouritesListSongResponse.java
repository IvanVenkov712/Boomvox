package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record FavouritesListSongResponse(
    long favouritesListId,
    long songId,
    int position,
    LocalDateTime addedAt
) {
}
