package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.Genre;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "album")
public class Album extends SongCollection {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    public Album(User author, String name, Date createdAt, Genre genre) {
        super(author, name, createdAt);
        this.genre = genre;
    }

    public Genre getGenre() {
        return genre;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;
        return getId() == album.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Album{" +
            "id=" + getId() +
            ", author=" + getAuthor() +
            ", name='" + getName() + '\'' +
            ", createdAt=" + getCreatedAt() +
            ", genre=" + getGenre() +
            '}';
    }
}
