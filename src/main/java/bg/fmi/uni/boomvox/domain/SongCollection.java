package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.util.Date;

@MappedSuperclass
public abstract class SongCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Version
    private long version;

    public SongCollection(User author, String name, Date createdAt) {
        this.author = author;
        this.name = name;
        this.createdAt = createdAt;
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

    public Date getCreatedAt() {
        return createdAt;
    }
}
