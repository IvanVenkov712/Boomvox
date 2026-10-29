package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByWordIgnoreCase(String word);

    List<Tag> findByWordContainingIgnoreCase(String word);
}
