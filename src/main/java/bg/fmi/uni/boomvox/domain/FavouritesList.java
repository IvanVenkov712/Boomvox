package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "favourites_list")
public class FavouritesList extends SongCollection {

    @OneToOne(mappedBy = "favourites")
    User user;

    public FavouritesList(String name, Date createdAt, User user) {
        super(name, createdAt);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Favourites{" +
            "id=" + getId() +
            ", user=" + getUser() +
            ", name='" + getName() + '\'' +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
