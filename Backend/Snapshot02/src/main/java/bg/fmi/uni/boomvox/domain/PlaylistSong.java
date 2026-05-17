package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.PlaylistSongId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_song")
@NoArgsConstructor
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongId id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    @MapsId("playlistId")
    private Playlist playlist;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    @MapsId("songId")
    private Song song;

    @Getter
    @Column(name = "position", nullable = false)
    private int position;

    @Getter
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public PlaylistSong(Playlist playlist, Song song, int position, LocalDateTime addedAt) {
        this.playlist = playlist;
        this.song = song;
        this.position = position;
        this.addedAt = addedAt;
        this.id = new PlaylistSongId(playlist.getId(), song.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PlaylistSong that = (PlaylistSong) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PlaylistSong{" +
            "playlist=" + getPlaylist() +
            ", song=" + getSong() +
            ", position=" + getPosition() +
            ", addedAt=" + getAddedAt() +
            '}';
    }
}
