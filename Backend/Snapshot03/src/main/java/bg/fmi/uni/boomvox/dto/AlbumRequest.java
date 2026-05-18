package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlbumRequest(
    @NotBlank
    String name,

    @Positive
    long authorId,

    @NotNull
    Genre genre
) {
}
