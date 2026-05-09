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

    @Column(name = "release_date", nullable = false)
    Date releaseDate;

    @Column()

    @Version
    private long version;
}
