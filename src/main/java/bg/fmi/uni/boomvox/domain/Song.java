package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uploaded_at", nullable = false)
    private Date uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongFormat format;

    @Column(name = "duration", nullable = false)
    private long duration;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    // We will eventually use AWS or the filesystem to store the uploaded songs.
    // This will be a path to a file or a URL address of the actual song data.
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Version
    private long version;

    public Song(Album album, String name, Date uploadedAt, SongFormat format, long duration, long fileSize,
                String storageKey) {
        this.album = album;
        this.name = name;
        this.uploadedAt = uploadedAt;
        this.format = format;
        this.duration = duration;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
    }

    public long getId() {
        return id;
    }

    public Album getAlbum() {
        return album;
    }

    public String getName() {
        return name;
    }

    public SongFormat getFormat() {
        return format;
    }

    public long getDuration() {
        return duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Date getUploadedAt() {
        return uploadedAt;
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
            "id=" + id +
            ", album=" + album +
            ", name='" + name + '\'' +
            ", uploadedAt=" + uploadedAt +
            ", format=" + format +
            ", duration=" + duration +
            ", fileSize=" + fileSize +
            ", storageKey='" + storageKey + '\'' +
            '}';
    }
}
