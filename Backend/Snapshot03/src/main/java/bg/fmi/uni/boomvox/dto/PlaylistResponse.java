package bg.fmi.uni.boomvox.dto;

import java.time.LocalDateTime;

public record PlaylistResponse(
    long id,
    String name,
    LocalDateTime createdAt,
    long ownerId
) {
}
