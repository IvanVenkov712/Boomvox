package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.SongTag;

import java.time.LocalDateTime;

public record SongTagResponse(
    long songId,
    long tagId,
    LocalDateTime addedAt
) {
    public static SongTagResponse from(SongTag songTag) {
        return new SongTagResponse(
            songTag.getSong().getId(),
            songTag.getTag().getId(),
            songTag.getAddedAt()
        );
    }
}
