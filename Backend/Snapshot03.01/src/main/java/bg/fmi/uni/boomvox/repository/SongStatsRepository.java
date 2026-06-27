package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.SongStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongStatsRepository extends JpaRepository<SongStats, Long> {

}
