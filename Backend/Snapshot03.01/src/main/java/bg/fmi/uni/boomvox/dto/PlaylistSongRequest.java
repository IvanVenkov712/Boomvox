package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PlaylistSongRequest(

    @Positive
    long songId,

    @PositiveOrZero
    int position
) {
    public static PlaylistSongRequest of(long songId, int position) {
        return new PlaylistSongRequest(songId, position);
    }
}
