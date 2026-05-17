package bg.fmi.uni.boomvox.repository;

import java.util.List;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByAlbumId(Long id);

    List<Song> findByAlbum(Album album);
}
