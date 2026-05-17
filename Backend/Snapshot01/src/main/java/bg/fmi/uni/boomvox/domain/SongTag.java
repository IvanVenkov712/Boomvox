package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.SongTagId;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "song_tag")
public class SongTag {
    @EmbeddedId
    private SongTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    @MapsId("songId")
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    @MapsId("tagId")
    private Tag tag;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Version
    private long version;

    public SongTag(Song song, Tag tag, LocalDateTime addedAt) {
        this.song = song;
        this.tag = tag;
        this.addedAt = addedAt;
        this.id = new SongTagId(song.getId(), tag.getId());
    }

    public Song getSong() {
        return song;
    }

    public Tag getTag() {
        return tag;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SongTag songTag = (SongTag) o;
        return Objects.equals(id, songTag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SongTag{" +
            "song=" + getSong() +
            ", tag=" + getTag() +
            ", addedAt=" + getAddedAt() +
            '}';
    }
}
