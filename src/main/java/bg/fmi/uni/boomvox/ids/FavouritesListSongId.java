package bg.fmi.uni.boomvox.ids;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FavouritesListSongId implements Serializable {
    private long favouritesListId;

    private long songId;

    public FavouritesListSongId(long favouritesListId, long songId) {
        this.favouritesListId = favouritesListId;
        this.songId = songId;
    }

    public long getFavouritesListId() {
        return favouritesListId;
    }

    public long getSongId() {
        return songId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        FavouritesListSongId that = (FavouritesListSongId) o;
        return favouritesListId == that.favouritesListId && songId == that.songId;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(favouritesListId);
        result = 31 * result + Long.hashCode(songId);
        return result;
    }
}
