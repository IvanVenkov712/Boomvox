package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.RatingId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "rating")
@NoArgsConstructor
public class Rating {
    @EmbeddedId
    private RatingId id;

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
    @Column(name = "rating_grade")
    private int grade;

    @Getter
    @Column(name = "comment", columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Getter
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Version
    private long version;

    public Rating(User user, Song song, int grade, String comment, LocalDateTime lastUpdatedAt) {
        this.user = user;
        this.song = song;
        update(grade, comment, lastUpdatedAt);
        this.id = new RatingId(user.getId(), song.getId());
    }

    public void update(int grade, String comment, LocalDateTime lastUpdatedAt) {
        if (grade < 0 || grade > 10) {
            throw new IllegalArgumentException("Grade should be between 0 and 10");
        }

        this.grade = grade;
        this.comment = comment;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Rating{" +
            "user=" + getUser() +
            ", song=" + getSong() +
            ", grade=" + getGrade() +
            ", comment='" + getComment() + '\'' +
            ", lastUpdatedAt=" + getLastUpdatedAt() +
            '}';
    }
}
