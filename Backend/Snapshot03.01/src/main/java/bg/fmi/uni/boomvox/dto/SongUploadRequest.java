package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SongUploadRequest(
    @NotNull  long      albumId,
    @NotBlank String    name,
    @NotNull SongFormat format,
    long duration   // seconds
) {}
