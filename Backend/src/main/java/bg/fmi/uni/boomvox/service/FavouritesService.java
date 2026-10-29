package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.FavouritesList;
import bg.fmi.uni.boomvox.domain.FavouritesListSong;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.FavouritesListResponse;
import bg.fmi.uni.boomvox.dto.FavouritesListSongRequest;
import bg.fmi.uni.boomvox.dto.FavouritesListSongResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.ids.FavouritesListSongId;
import bg.fmi.uni.boomvox.repository.FavouritesListRepository;
import bg.fmi.uni.boomvox.repository.FavouritesListSongRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FavouritesService extends BaseService {

    private final FavouritesListRepository favouritesListRepository;
    private final FavouritesListSongRepository favouritesListSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public FavouritesService(
        FavouritesListRepository favouritesListRepository,
        FavouritesListSongRepository favouritesListSongRepository,
        SongRepository songRepository,
        UserRepository userRepository
    ) {
        this.favouritesListRepository = favouritesListRepository;
        this.favouritesListSongRepository = favouritesListSongRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public FavouritesListResponse getFavouritesList(long userId) {
        return FavouritesListResponse.from(findUserFavouritesList(userId));
    }

    @Transactional(readOnly = true)
    public FavouritesListResponse getFavouritesListById(long userId, long favouritesListId) {
        return FavouritesListResponse.from(findOwnedFavouritesList(userId, favouritesListId));
    }

    public FavouritesListSongResponse addSongToFavourites(long userId, FavouritesListSongRequest request) {
        FavouritesList favouritesList = findOwnedFavouritesList(userId, request.favouritesListId());
        Song song = findSong(request.songId());
        FavouritesListSongId id = new FavouritesListSongId(favouritesList.getId(), song.getId());

        if (favouritesListSongRepository.existsById(id)) {
            throw new ValidationException("Song already exists in favourites");
        }

        FavouritesListSong favouritesListSong = new FavouritesListSong(
            favouritesList,
            song,
            request.position(),
            LocalDateTime.now()
        );

        return FavouritesListSongResponse.from(favouritesListSongRepository.save(favouritesListSong));
    }

    public void removeSongFromFavourites(long userId, long favouritesListId, long songId) {
        findOwnedFavouritesList(userId, favouritesListId);
        FavouritesListSongId id = new FavouritesListSongId(favouritesListId, songId);

        if (!favouritesListSongRepository.existsById(id)) {
            throw new NotFoundException(
                "FavouritesListSong not found with favourites list id " + favouritesListId + " and song id " + songId
            );
        }

        favouritesListSongRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FavouritesListSongResponse> getFavouriteSongs(long userId) {
        FavouritesList favouritesList = findUserFavouritesList(userId);

        return favouritesListSongRepository.findByFavouritesList(favouritesList).stream()
            .map(FavouritesListSongResponse::from)
            .toList();
    }

    private FavouritesList findOwnedFavouritesList(long userId, long favouritesListId) {
        FavouritesList favouritesList = favouritesListRepository.findById(favouritesListId)
            .orElseThrow(() -> new NotFoundException("FavouritesList", favouritesListId));
        User owner = userRepository.findByFavouritesListId(favouritesListId)
            .orElseThrow(() -> new ValidationException("Favourites list has no owner"));

        if (owner.getId() != userId) {
            throw new ValidationException("Favourites list does not belong to user");
        }

        return favouritesList;
    }

    private FavouritesList findUserFavouritesList(long userId) {
        User user = findUser(userId);

        if (user.getFavouritesList() == null) {
            throw new NotFoundException("FavouritesList for user", userId);
        }

        return user.getFavouritesList();
    }

    private Song findSong(long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));
    }

    private User findUser(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User", id));
    }
}
