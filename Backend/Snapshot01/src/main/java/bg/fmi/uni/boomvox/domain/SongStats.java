package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "song_stats")
public class SongStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(mappedBy = "stats")
    private Song song;

    @Column(name = "ratings_count", nullable = false)
    private long ratingsCount;

    @Column(name = "avg_rating", nullable = false)
    private double avgRating;

    @Column(name = "plays_count", nullable = false)
    private long playsCount;

    @Column(name = "playlists_count", nullable = false)
    private long playlistsCount;

    @Column(name = "recommendations_count", nullable = false)
    private long recommendationsCount;

    @Version
    private long version;

    public SongStats(long recommendationsCount, long playlistsCount, long playsCount, double avgRating,
                     long ratingsCount,
                     Song song) {
        this.recommendationsCount = recommendationsCount;
        this.playlistsCount = playlistsCount;
        this.playsCount = playsCount;
        this.avgRating = avgRating;
        this.ratingsCount = ratingsCount;
        this.song = song;
    }

    public long getId() {
        return id;
    }

    public long getRecommendationsCount() {
        return recommendationsCount;
    }

    public long getPlaylistsCount() {
        return playlistsCount;
    }

    public long getPlaysCount() {
        return playsCount;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public long getRatingsCount() {
        return ratingsCount;
    }

    public Song getSong() {
        return song;
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
