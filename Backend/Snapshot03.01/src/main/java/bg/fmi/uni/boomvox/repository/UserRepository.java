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

    @Query(value = "SELECT * FROM boomvox.\"user\" u WHERE " +
        "(:search IS NULL OR u.username ILIKE CONCAT('%', :search, '%') OR " +
        "u.email ILIKE CONCAT('%', :search, '%') OR " +
        "u.first_name ILIKE CONCAT('%', :search, '%') OR " +
        "u.last_name ILIKE CONCAT('%', :search, '%')) AND " +
        "(:role IS NULL OR u.role = CAST(:role AS VARCHAR))",
        nativeQuery = true)
    List<User> findBySearchAndRole(
        @Param("search") String search,
        @Param("role") String role
    );
}
