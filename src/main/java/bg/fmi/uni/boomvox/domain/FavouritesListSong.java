package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.FavouritesListSongId;
import bg.fmi.uni.boomvox.ids.PlaylistSongId;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "favourites_list_song")
public class FavouritesListSong {
    @EmbeddedId
    private FavouritesListSongId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favourites_list_id", nullable = false)
    @MapsId("playlistId")
    private FavouritesList favouritesList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    @MapsId("songId")
    private Song song;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public FavouritesListSong(FavouritesList favouritesList, Song song, int position, LocalDateTime addedAt) {
        this.favouritesList = favouritesList;
        this.song = song;
        this.position = position;
        this.addedAt = addedAt;
        this.id = new FavouritesListSongId(favouritesList.getId(), song.getId());
    }

    public FavouritesList getFavouritesList() {
        return favouritesList;
    }

    public Song getSong() {
        return song;
    }

    public int getPosition() {
        return position;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        FavouritesListSong that = (FavouritesListSong) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PlaylistSong{" +
            "favouritesList=" + getFavouritesList() +
            ", song=" + getSong() +
            ", position=" + getPosition() +
            ", addedAt=" + getAddedAt() +
            '}';
    }
}
