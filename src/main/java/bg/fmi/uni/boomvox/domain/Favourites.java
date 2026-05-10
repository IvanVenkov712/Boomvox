package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

public class Favourites extends SongCollection {

    @OneToOne()
    User user;
}
