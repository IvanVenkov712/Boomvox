package bg.fmi.uni.boomvox.ids;

import jakarta.persistence.*;

import java.io.Serializable;

@Embeddable
public class SongTagId implements Serializable {
    private long songId;

    private long tagId;

    public SongTagId(long songId, long tagId) {
        this.songId = songId;
        this.tagId = tagId;
    }

    public long getSongId() {
        return songId;
    }

    public long getTagId() {
        return tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SongTagId songTagId = (SongTagId) o;
        return songId == songTagId.songId && tagId == songTagId.tagId;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(songId);
        result = 31 * result + Long.hashCode(tagId);
        return result;
    }
}
