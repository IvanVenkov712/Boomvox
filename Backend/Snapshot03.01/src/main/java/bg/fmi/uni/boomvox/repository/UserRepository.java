package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findByRole(UserRole role);

    Optional<User> findByFavouritesListId(Long favouritesListId);

    Optional<User> findByResetToken(String resetToken);

    @Query("SELECT u FROM User u WHERE " +
        "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "(:role IS NULL OR u.role = :role)")
    List<User> findBySearchAndRole(
        @Param("search") String search,
        @Param("role") UserRole role
    );
}
