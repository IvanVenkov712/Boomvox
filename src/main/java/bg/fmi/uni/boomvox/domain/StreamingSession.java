package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.SessionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "streaming_session")
public class StreamingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = true)
    private Playlist playlist;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = true)
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Version
    private long version;

    public StreamingSession(User user, Song song, Playlist playlist, LocalDateTime startedAt, LocalDateTime endedAt,
                            SessionStatus status) {
        this.user = user;
        this.song = song;
        this.playlist = playlist;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public Song getSong() {
        return song;
    }

    public User getUser() {
        return user;
    }

    public int getId() {
        return id;
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
