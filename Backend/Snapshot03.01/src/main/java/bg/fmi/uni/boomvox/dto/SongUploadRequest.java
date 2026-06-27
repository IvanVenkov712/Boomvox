package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SongUploadRequest(
    Long albumId,
    @NotBlank String name,
    SongFormat format,
    long duration   // seconds
) {}
