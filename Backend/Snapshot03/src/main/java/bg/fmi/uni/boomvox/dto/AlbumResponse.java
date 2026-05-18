package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.enums.Genre;

import java.time.LocalDateTime;

public record AlbumResponse(
    long id,
    String name,
    LocalDateTime createdAt,
    long authorId,
    Genre genre
) {
    public static AlbumResponse from(Album album) {
        return new AlbumResponse(
            album.getId(),
            album.getName(),
            album.getCreatedAt(),
            album.getAuthor().getId(),
            album.getGenre()
        );
    }
}
