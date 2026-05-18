package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PlaylistRequest(
    @NotBlank
    String name,

    @Positive
    long ownerId
) {
    public static PlaylistRequest of(String name, long ownerId) {
        return new PlaylistRequest(name, ownerId);
    }
}
