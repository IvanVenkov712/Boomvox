package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "streaming_session")
@NoArgsConstructor
public class StreamingSession {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = true)
    private Playlist playlist;

    @Getter
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Getter
    @Column(name = "ended_at", nullable = true)
    private LocalDateTime endedAt;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Getter
    @Setter
    @Column(name = "variant_id")
    private Long variantId;

    @Version
    private long version;

    public StreamingSession(User user, Song song, Playlist playlist, LocalDateTime startedAt) {
        this.user = user;
        this.song = song;
        this.playlist = playlist;
        this.startedAt = startedAt;
        this.status = SessionStatus.ACTIVE;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        StreamingSession that = (StreamingSession) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
