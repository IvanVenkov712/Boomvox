package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "playlist")
public class Playlist extends SongCollection {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public Playlist(String name, Date createdAt) {
        super(name, createdAt);
    }

    public User getOwner() {
        return owner;
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
