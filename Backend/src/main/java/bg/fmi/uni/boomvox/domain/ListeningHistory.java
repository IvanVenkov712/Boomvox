package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "listening_history")
@NoArgsConstructor
public class ListeningHistory {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private StreamingSession session;

    @Getter
    @Column(name = "listened_at", nullable = false)
    private LocalDateTime listenedAt;

    @Getter
    @Column(name = "listened_duration_sec", nullable = false)
    private long listenedDurationSec;

    @Version
    private long version;

    public ListeningHistory(StreamingSession session, LocalDateTime listenedAt, long listenedDurationSec) {
        if (listenedDurationSec < 0) {
            throw new IllegalArgumentException("Listened duration cannot be negative");
        }

        this.session = session;
        this.user = session.getUser();
        this.song = session.getSong();
        this.listenedAt = listenedAt;
        this.listenedDurationSec = listenedDurationSec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ListeningHistory that = (ListeningHistory) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
