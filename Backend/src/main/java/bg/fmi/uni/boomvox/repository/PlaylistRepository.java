package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Playlist;
import bg.fmi.uni.boomvox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByOwnerId(Long id);

    List<Playlist> findByOwner(User owner);

    List<Playlist> findByOwnerAndCreatedAtAfterOrderByCreatedAtDesc(User owner, LocalDateTime time);

    List<Playlist> findByOwnerIdAndCreatedAtAfterOrderByCreatedAtDesc(Long ownerId, LocalDateTime time);

}
