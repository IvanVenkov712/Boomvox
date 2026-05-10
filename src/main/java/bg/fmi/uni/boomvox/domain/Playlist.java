package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "playlist")
public class Playlist extends SongCollection {
    public Playlist(User author, String name, Date createdAt) {
        super(author, name, createdAt);
    }

    @Override
    public String toString() {
        return "Playlist{" +
            "id=" + getId() +
            ", author=" + getAuthor() +
            ", name='" + getName() + '\'' +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
