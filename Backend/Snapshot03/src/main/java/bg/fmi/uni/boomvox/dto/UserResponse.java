package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.User;
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
    public static UserResponse from(User user) {
        Long favouritesListId = user.getFavouritesList() == null ? null : user.getFavouritesList().getId();

        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            favouritesListId
        );
    }
}
