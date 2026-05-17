package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.RecommendationId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "recommendation")
public class Recommendation {

    @EmbeddedId
    private RecommendationId id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @MapsId("userId")
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    @MapsId("songId")
    private Song song;

    @Getter
    private int percent;

    @Version
    long version;

    public Recommendation(User user, Song song, int percent) {
        this.user = user;
        this.song = song;
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100!");
        }
        id = new RecommendationId(user.getId(), song.getId());
        this.percent = percent;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Recommendation that = (Recommendation) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Recommendation{" +
            "user=" + getUser() +
            ", song=" + getSong() +
            ", percent=" + getPercent() +
            '}';
    }
}
