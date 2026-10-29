package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.StreamingEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Table(name = "streaming_event")
public class StreamingEvent {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private StreamingSession session;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamingEventType type;

    @Getter
    @Column(name = "position_sec", nullable = false)
    private long positionSec;

    @Getter
    @Column(name = "seek_from_sec", nullable = true)
    private Long seekFromSec;

    @Getter
    @Column(name = "seek_to_sec", nullable = true)
    private Long seekToSec;

    @Getter
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    private long version;

    public StreamingEvent(StreamingSession session, StreamingEventType type, long positionSec, Long seekFromSec,
                          Long seekToSec) {
        this.session = session;
        this.type = type;
        this.positionSec = positionSec;
        this.seekFromSec = seekFromSec;
        this.seekToSec = seekToSec;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        StreamingEvent that = (StreamingEvent) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "StreamingEvent{" +
            "id=" + getId() +
            ", session=" + getSession() +
            ", type=" + getType() +
            ", positionSec=" + getPositionSec() +
            ", seekFromSec=" + getSeekFromSec() +
            ", seekToSec=" + getSeekToSec() +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
