package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record SongTagResponse(
    long songId,
    long tagId,
    LocalDateTime addedAt
) {
}
