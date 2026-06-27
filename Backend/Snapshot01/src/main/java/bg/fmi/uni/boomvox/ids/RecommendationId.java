package bg.fmi.uni.boomvox.ids;

import jakarta.persistence.*;

import java.io.Serializable;

@Embeddable
public class RecommendationId implements Serializable {
    private long userId;

    private long songId;

    public RecommendationId() {

    }

    public RecommendationId(long userId, long songId) {
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

        RecommendationId that = (RecommendationId) o;
        return userId == that.userId && songId == that.songId;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(userId);
        result = 31 * result + Long.hashCode(songId);
        return result;
    }
}
