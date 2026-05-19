package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "song_stats")
@NoArgsConstructor
public class SongStats {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Column(name = "ratings_count", nullable = false)
    private long ratingsCount;

    @Getter
    @Column(name = "avg_rating", nullable = false)
    private double avgRating;

    @Getter
    @Column(name = "plays_count", nullable = false)
    private long playsCount;

    @Getter
    @Column(name = "playlists_count", nullable = false)
    private long playlistsCount;

    @Getter
    @Column(name = "recommendations_count", nullable = false)
    private long recommendationsCount;

    @Version
    private long version;

    public SongStats(long recommendationsCount, long playlistsCount, long playsCount, double avgRating,
                     long ratingsCount) {
        this.recommendationsCount = recommendationsCount;
        this.playlistsCount = playlistsCount;
        this.playsCount = playsCount;
        this.avgRating = avgRating;
        this.ratingsCount = ratingsCount;
    }

    @Override
    public String toString() {
        return "SongStats{" +
            "id=" + getId() +
            ", ratingsCount=" + getRatingsCount() +
            ", avgRating=" + getAvgRating() +
            ", playsCount=" + getPlaysCount() +
            ", playlistsCount=" + getPlaylistsCount() +
            ", recommendationsCount=" + getRecommendationsCount() +
            '}';
    }
}
