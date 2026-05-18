package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "playlist")
@NoArgsConstructor
public class Playlist extends SongCollection {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    private User owner;

    public Playlist(String name, LocalDateTime createdAt, User owner) {
        super(name, createdAt);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Playlist{" +
            "id=" + getId() +
            ", owner=" + getOwner() +
            ", name='" + getName() + '\'' +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
