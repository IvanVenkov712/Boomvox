package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.AudioVariant;
import bg.fmi.uni.boomvox.enums.AudioVariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioVariantRepository extends JpaRepository<AudioVariant, Long> {

    List<AudioVariant> findBySongIdAndStatus(long songId, AudioVariantStatus status);
}
