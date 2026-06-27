package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Playlist;

import java.time.LocalDateTime;

public record PlaylistResponse(
    long id,
    String name,
    LocalDateTime createdAt,
    long ownerId
) {
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
            playlist.getId(),
            playlist.getName(),
            playlist.getCreatedAt(),
            playlist.getOwner().getId()
        );
    }
}
