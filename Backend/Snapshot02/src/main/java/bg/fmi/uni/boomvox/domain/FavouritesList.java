package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "favourites_list")
public class FavouritesList extends SongCollection {

    public FavouritesList(String name, LocalDateTime createdAt) {
        super(name, createdAt);
    }

    @Override
    public String toString() {
        return "Favourites{" +
            "id=" + getId() +
            ", name='" + getName() + '\'' +
            ", createdAt=" + getCreatedAt() +
            '}';
    }
}
