package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.FavouritesList;

import java.time.LocalDateTime;

public record FavouritesListResponse(
    long id,
    String name,
    LocalDateTime createdAt
) {
    public static FavouritesListResponse from(FavouritesList favouritesList) {
        return new FavouritesListResponse(
            favouritesList.getId(),
            favouritesList.getName(),
            favouritesList.getCreatedAt()
        );
    }
}
