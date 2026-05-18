package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record PlaylistSongResponse(
    long playlistId,
    long songId,
    int position,
    LocalDateTime addedAt
) {
}
