package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.Genre;
import jakarta.persistence.*;

import java.util.Date;


@Table(name = "album")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(name = "upload_date", nullable = false)
    private Date uploadDate;

    @Version
    private long version;

    public Album(User author, String name, Genre genre, Date uploadDate) {
        this.author = author;
        this.name = name;
        this.genre = genre;
        this.uploadDate = uploadDate;
    }

    public long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public Genre getGenre() {
        return genre;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;
        return id == album.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Album{" +
            "id=" + id +
            ", author=" + author +
            ", name='" + name + '\'' +
            ", genre=" + genre +
            ", uploadDate=" + uploadDate +
            '}';
    }
}
