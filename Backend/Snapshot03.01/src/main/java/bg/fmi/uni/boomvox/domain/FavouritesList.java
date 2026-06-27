package bg.fmi.uni.boomvox.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "favourites_list")
@NoArgsConstructor
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
