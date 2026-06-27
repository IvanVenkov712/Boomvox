package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;

import java.time.LocalDateTime;

public record SongResponse(
    long id,
    Long albumId,
    String name,
    LocalDateTime uploadedAt,
    SongFormat format,
    long duration,
    long fileSize,
    String storageKey,
    Long statsId,
    SongProcessingStatus processingStatus
) {

    public static SongResponse from(Song song) {
        return new SongResponse(
            song.getId(),
            song.getAlbum() != null ? song.getAlbum().getId() : null,
            song.getName(),
            song.getUploadedAt(),
            song.getFormat(),
            song.getDuration(),
            song.getFileSize(),
            song.getStorageKey(),
            song.getStats() != null ? song.getStats().getId() : null,
            song.getProcessingStatus()
        );
    }
}