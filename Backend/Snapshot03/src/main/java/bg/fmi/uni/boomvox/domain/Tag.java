package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "tag")
@NoArgsConstructor
public class Tag {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Column(name = "word", nullable = false)
    private String word;

    public Tag(String word) {
        this.word = word;
    }

    public Tag(long id, String word) {
        this.id = id;
        this.word = word;
    }

    public void update(String word) {
        this.word = word;
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
