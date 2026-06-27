package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.SongTag;
import bg.fmi.uni.boomvox.domain.Tag;
import bg.fmi.uni.boomvox.dto.SongResponse;
import bg.fmi.uni.boomvox.dto.SongTagRequest;
import bg.fmi.uni.boomvox.dto.SongTagResponse;
import bg.fmi.uni.boomvox.dto.TagResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.ids.SongTagId;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.SongTagRepository;
import bg.fmi.uni.boomvox.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SongTagService extends BaseService {

    private final SongRepository songRepository;
    private final TagRepository tagRepository;
    private final SongTagRepository songTagRepository;

    public SongTagService(
        SongRepository songRepository,
        TagRepository tagRepository,
        SongTagRepository songTagRepository
    ) {
        this.songRepository = songRepository;
        this.tagRepository = tagRepository;
        this.songTagRepository = songTagRepository;
    }

    public SongTagResponse attachTagToSong(SongTagRequest request) {
        Song song = findSong(request.songId());
        Tag tag = findTag(request.tagId());
        SongTagId id = new SongTagId(song.getId(), tag.getId());

        if (songTagRepository.existsById(id)) {
            throw new ValidationException("Tag " + tag.getId() + " is already attached to song " + song.getId());
        }

        return SongTagResponse.from(songTagRepository.save(new SongTag(song, tag, LocalDateTime.now())));
    }

    public void detachTagFromSong(long songId, long tagId) {
        SongTagId id = new SongTagId(songId, tagId);
        if (!songTagRepository.existsById(id)) {
            throw new NotFoundException("SongTag not found with song id: " + songId + " and tag id: " + tagId);
        }

        songTagRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTagsForSong(long songId) {
        Song song = findSong(songId);

        return songTagRepository.findBySong(song).stream()
            .map(SongTag::getTag)
            .map(TagResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByTag(long tagId) {
        Tag tag = findTag(tagId);

        return songTagRepository.findByTag(tag).stream()
            .map(SongTag::getSong)
            .map(SongResponse::from)
            .toList();
    }

    private Song findSong(long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));
    }

    private Tag findTag(long id) {
        return tagRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tag", id));
    }
}
