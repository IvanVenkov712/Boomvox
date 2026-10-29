package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.FavouritesList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouritesListRepository extends JpaRepository<FavouritesList, Long> {
}
