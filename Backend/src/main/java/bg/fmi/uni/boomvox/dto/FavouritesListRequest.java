package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.NotBlank;

public record FavouritesListRequest(
    @NotBlank
    String name
) {
    public static FavouritesListRequest of(String name) {
        return new FavouritesListRequest(name);
    }
}
