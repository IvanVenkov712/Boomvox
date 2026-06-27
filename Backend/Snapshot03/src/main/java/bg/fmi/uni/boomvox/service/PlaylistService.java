package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Playlist;
import bg.fmi.uni.boomvox.domain.PlaylistSong;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.PlaylistRequest;
import bg.fmi.uni.boomvox.dto.PlaylistResponse;
import bg.fmi.uni.boomvox.dto.PlaylistSongRequest;
import bg.fmi.uni.boomvox.dto.PlaylistSongResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.ids.PlaylistSongId;
import bg.fmi.uni.boomvox.repository.PlaylistRepository;
import bg.fmi.uni.boomvox.repository.PlaylistSongRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PlaylistService extends BaseService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public PlaylistService(
        PlaylistRepository playlistRepository,
        PlaylistSongRepository playlistSongRepository,
        SongRepository songRepository,
        UserRepository userRepository
    ) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
    }

    public PlaylistResponse createPlaylist(PlaylistRequest request) {
        User owner = findUser(request.ownerId());
        Playlist playlist = new Playlist(request.name().trim(), LocalDateTime.now(), owner);

        return PlaylistResponse.from(playlistRepository.save(playlist));
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(long userId, long playlistId) {
        return PlaylistResponse.from(findOwnedPlaylist(userId, playlistId));
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPlaylistsByOwner(long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("User", ownerId);
        }

        return playlistRepository.findByOwnerId(ownerId).stream()
            .map(PlaylistResponse::from)
            .toList();
    }

    public void deletePlaylist(long userId, long playlistId) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        playlistRepository.delete(playlist);
    }

    public PlaylistSongResponse addSongToPlaylist(long userId, PlaylistSongRequest request) {
        Playlist playlist = findOwnedPlaylist(userId, request.playlistId());
        Song song = findSong(request.songId());
        PlaylistSongId id = new PlaylistSongId(playlist.getId(), song.getId());

        if (playlistSongRepository.existsById(id)) {
            throw new ValidationException("Song already exists in playlist");
        }

        PlaylistSong playlistSong = new PlaylistSong(playlist, song, request.position(), LocalDateTime.now());

        return PlaylistSongResponse.from(playlistSongRepository.save(playlistSong));
    }

    public void removeSongFromPlaylist(long userId, long playlistId, long songId) {
        findOwnedPlaylist(userId, playlistId);
        PlaylistSongId id = new PlaylistSongId(playlistId, songId);

        if (!playlistSongRepository.existsById(id)) {
            throw new NotFoundException("PlaylistSong not found with playlist id " + playlistId + " and song id " + songId);
        }

        playlistSongRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PlaylistSongResponse> getPlaylistSongs(long userId, long playlistId) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);

        return playlistSongRepository.findByPlaylistOrderByPosition(playlist).stream()
            .map(PlaylistSongResponse::from)
            .toList();
    }

    private Playlist findOwnedPlaylist(long userId, long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new NotFoundException("Playlist", playlistId));

        if (playlist.getOwner().getId() != userId) {
            throw new ValidationException("Playlist does not belong to user");
        }

        return playlist;
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
