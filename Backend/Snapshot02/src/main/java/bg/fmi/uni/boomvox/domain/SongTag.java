package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.ids.SongTagId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "song_tag")
@NoArgsConstructor
public class SongTag {
    @EmbeddedId
    private SongTagId id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    @MapsId("songId")
    private Song song;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    @MapsId("tagId")
    private Tag tag;

    @Getter
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
