package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PlaylistSongRequest(
    @Positive
    long playlistId,

    @Positive
    long songId,

    @PositiveOrZero
    int position
) {
}
