package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SongFormat;

import java.time.LocalDateTime;

public record SongResponse(
    long id,
    long albumId,
    String name,
    LocalDateTime uploadedAt,
    SongFormat format,
    long duration,
    long fileSize,
    String storageKey,
    SongStatsResponse stats
) {
}
