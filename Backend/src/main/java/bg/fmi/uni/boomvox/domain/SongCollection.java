package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor
public abstract class SongCollection {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    private long version;

    public SongCollection(String name, LocalDateTime createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SongCollection that = (SongCollection) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}