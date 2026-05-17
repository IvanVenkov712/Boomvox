package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.FavouritesList;
import bg.fmi.uni.boomvox.domain.FavouritesListSong;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.ids.FavouritesListSongId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FavouritesListSongRepository extends JpaRepository<FavouritesListSong, FavouritesListSongId> {

    List<FavouritesListSong> findBySongId(Long id);

    List<FavouritesListSong> findBySong(Song song);

    long countBySongId(Long id);

    long countBySong(Song song);

    List<FavouritesListSong> findByFavouritesListId(Long id);

    List<FavouritesListSong> findByFavouritesList(FavouritesList list);

    List<FavouritesListSong> findBySongAndAddedAtAfter(Song song, LocalDateTime time);

    List<FavouritesListSong> findByFavouritesListAndAddedAtAfter(FavouritesList list, LocalDateTime time);
}
