package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.UserRole;

public record UserResponse(
    long id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    Long favouritesListId
) {
}
