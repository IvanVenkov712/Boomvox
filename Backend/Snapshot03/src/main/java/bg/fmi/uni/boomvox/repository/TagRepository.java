package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
