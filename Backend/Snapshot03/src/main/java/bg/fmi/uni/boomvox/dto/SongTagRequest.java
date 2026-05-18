package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.Positive;

public record SongTagRequest(
    @Positive
    long songId,

    @Positive
    long tagId
) {
    public static SongTagRequest of(long songId, long tagId) {
        return new SongTagRequest(songId, tagId);
    }
}
