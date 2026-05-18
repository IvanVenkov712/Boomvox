package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "song")
@NoArgsConstructor
public class Song {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongFormat format;

    @Getter
    @Column(name = "duration", nullable = false)
    private long duration;

    @Getter
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    // We will eventually use AWS or the filesystem to store the uploaded songs.
    // This will be a path to a file or a URL address of the actual song data.
    @Getter
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "song_stats_id", nullable = false)
    private SongStats stats;

    @Version
    private long version;

    public Song(Album album, String name, LocalDateTime uploadedAt, SongFormat format, long duration, long fileSize,
                String storageKey) {
        this.album = album;
        this.name = name;
        this.uploadedAt = uploadedAt;
        this.format = format;
        this.duration = duration;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;
        return id == song.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Song{" +
            "id=" + getId() +
            ", album=" + getAlbum() +
            ", name='" + getName() + '\'' +
            ", uploadedAt=" + getUploadedAt() +
            ", format=" + getFormat() +
            ", duration=" + getDuration() +
            ", fileSize=" + getFileSize() +
            ", storageKey='" + getStorageKey() + '\'' +
            ", stats=" + stats +
            '}';
    }
}
