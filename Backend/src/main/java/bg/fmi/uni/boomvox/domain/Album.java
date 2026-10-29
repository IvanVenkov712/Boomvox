package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.Genre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "album")
@NoArgsConstructor
public class Album extends SongCollection {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter
    private Genre genre;

    public Album(String name, LocalDateTime createdAt, User author, Genre genre) {
        super(name, createdAt);
        this.author = author;
        this.genre = genre;
    }

    public void update(String name, Genre genre) {
        setName(name);
        this.genre = genre;
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