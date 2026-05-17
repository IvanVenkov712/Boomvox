package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Playlist;
import bg.fmi.uni.boomvox.domain.PlaylistSong;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.ids.PlaylistSongId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {

    List<PlaylistSong> findByPlaylistId(Long id);

    List<PlaylistSong> findByPlaylist(Playlist playlist);

    List<PlaylistSong> findByPlaylistIdOrderByPosition(Long id);

    List<PlaylistSong> findByPlaylistOrderByPosition(Playlist playlist);

    long countBySongId(Long id);

    long countBySong(Song song);

}
