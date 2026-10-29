package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.NotBlank;

public record PlaylistRequest(
    @NotBlank String name
) {}
