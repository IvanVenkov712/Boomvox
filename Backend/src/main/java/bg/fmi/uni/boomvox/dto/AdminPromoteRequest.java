package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.UserRole;

public record AdminPromoteRequest(UserRole role) {
}
