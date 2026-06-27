package bg.fmi.uni.boomvox.dto;

public record AuthResponse(
    String token,
    String email,
    String role
) {}
