package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank
    @Size(max = 20)
    String username,

    @NotBlank
    @Email
    @Size(max = 50)
    String email,

    @NotBlank
    String password,

    @NotBlank
    @Size(max = 20)
    String firstName,

    @NotBlank
    @Size(max = 20)
    String lastName,

    @NotNull
    UserRole role,

    @Positive
    Long favouritesListId
) {
}
