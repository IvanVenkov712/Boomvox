package bg.fmi.uni.boomvox.ids;

import jakarta.persistence.*;

import java.io.Serializable;

@Embeddable
public class PlaylistSongId implements Serializable {
    private long playlistId;

    private long songId;

    public PlaylistSongId() {
    }

    public PlaylistSongId(long playlistId, long songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public long getSongId() {
        return songId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PlaylistSongId that = (PlaylistSongId) o;
        return playlistId == that.playlistId && songId == that.songId;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(playlistId);
        result = 31 * result + Long.hashCode(songId);
        return result;
    }
}
