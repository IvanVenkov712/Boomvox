package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Album;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByUserId(Long userId);

    List<Album> findByUser(User user);

    List<Album> findByGenre(Genre genre);
}
