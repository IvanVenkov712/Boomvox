package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.util.Date;

@MappedSuperclass
public abstract class SongCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Version
    private long version;

    public SongCollection(String name, Date createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
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
