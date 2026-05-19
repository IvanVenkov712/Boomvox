package bg.fmi.uni.boomvox.ids;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RatingId implements Serializable {
    private long userId;

    private long songId;

    protected RatingId() {
    }

    public RatingId(long userId, long songId) {
        this.userId = userId;
        this.songId = songId;
    }

    public long getUserId() {
        return userId;
    }

    public long getSongId() {
        return songId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        RatingId ratingId = (RatingId) o;
        return userId == ratingId.userId && songId == ratingId.songId;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(userId);
        result = 31 * result + Long.hashCode(songId);
        return result;
    }
}
