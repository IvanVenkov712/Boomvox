package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findByRole(UserRole role);

    Optional<User> findByFavouritesListId(Long favouritesListId);

    Optional<User> findByResetToken(String resetToken);
}
