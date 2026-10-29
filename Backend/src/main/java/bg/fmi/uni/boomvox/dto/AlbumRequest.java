package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlbumRequest(
    @NotBlank
    String name,

    @NotNull
    Genre genre
) {
    public static AlbumRequest of(String name, Genre genre) {
        return new AlbumRequest(name, genre);
    }
}
