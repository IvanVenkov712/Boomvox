package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank @Size(max = 20) String username,
    @NotBlank @Size(max = 20) String firstName,
    @NotBlank @Size(max = 20) String lastName
) {}
