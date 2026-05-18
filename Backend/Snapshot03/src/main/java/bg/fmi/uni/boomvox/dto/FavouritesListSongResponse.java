package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.FavouritesListSong;

import java.time.LocalDateTime;

public record FavouritesListSongResponse(
    long favouritesListId,
    long songId,
    int position,
    LocalDateTime addedAt
) {
    public static FavouritesListSongResponse from(FavouritesListSong favouritesListSong) {
        return new FavouritesListSongResponse(
            favouritesListSong.getFavouritesList().getId(),
            favouritesListSong.getSong().getId(),
            favouritesListSong.getPosition(),
            favouritesListSong.getAddedAt()
        );
    }
}
