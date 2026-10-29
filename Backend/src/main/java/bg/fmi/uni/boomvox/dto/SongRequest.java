package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SongRequest(
    @Positive
    long albumId,

    @NotBlank
    String name,

    @NotNull
    SongFormat format,

    @Positive
    long duration,

    @PositiveOrZero
    long fileSize,

    @NotBlank
    String storageKey
) {
    public static SongRequest of(long albumId, String name, SongFormat format, long duration, long fileSize,
                                 String storageKey) {
        return new SongRequest(albumId, name, format, duration, fileSize, storageKey);
    }
}
