package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);
}
