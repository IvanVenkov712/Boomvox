package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;


@Entity
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "word", nullable = false)
    private String word;

    public Tag(long id, String word) {
        this.id = id;
        this.word = word;
    }

    public long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;
        return id == tag.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Tag{" +
            "id=" + getId() +
            ", word='" + getWord() + '\'' +
            '}';
    }
}
