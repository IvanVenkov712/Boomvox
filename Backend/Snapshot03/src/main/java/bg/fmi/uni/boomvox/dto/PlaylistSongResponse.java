package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.PlaylistSong;

import java.time.LocalDateTime;

public record PlaylistSongResponse(
    long playlistId,
    long songId,
    int position,
    LocalDateTime addedAt
) {
    public static PlaylistSongResponse from(PlaylistSong playlistSong) {
        return new PlaylistSongResponse(
            playlistSong.getPlaylist().getId(),
            playlistSong.getSong().getId(),
            playlistSong.getPosition(),
            playlistSong.getAddedAt()
        );
    }
}
