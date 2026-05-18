package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Song;
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
    public static SongResponse from(Song song) {
        return from(song, null);
    }

    public static SongResponse from(Song song, SongStatsResponse stats) {
        return new SongResponse(
            song.getId(),
            song.getAlbum().getId(),
            song.getName(),
            song.getUploadedAt(),
            song.getFormat(),
            song.getDuration(),
            song.getFileSize(),
            song.getStorageKey(),
            stats
        );
    }
}
