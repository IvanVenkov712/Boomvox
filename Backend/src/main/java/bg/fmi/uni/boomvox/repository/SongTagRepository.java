package bg.fmi.uni.boomvox.repository;

import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.SongTag;
import bg.fmi.uni.boomvox.domain.Tag;
import bg.fmi.uni.boomvox.ids.SongTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongTagRepository extends JpaRepository<SongTag, SongTagId> {
    List<SongTag> findBySongId(Long id);

    List<SongTag> findBySong(Song song);

    List<SongTag> findByTagId(Long id);

    List<SongTag> findByTag(Tag tag);
}
