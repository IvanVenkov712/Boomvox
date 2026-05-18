package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.Genre;

import java.time.LocalDateTime;

public record AlbumResponse(
    long id,
    String name,
    LocalDateTime createdAt,
    long authorId,
    Genre genre
) {
}
