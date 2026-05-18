package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record FavouritesListResponse(
    long id,
    String name,
    LocalDateTime createdAt
) {
}
