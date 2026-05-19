package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Tag;
import bg.fmi.uni.boomvox.dto.TagRequest;
import bg.fmi.uni.boomvox.dto.TagResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagService extends BaseService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public TagResponse createTag(TagRequest request) {
        String word = request.word().trim();
        if (tagRepository.findByWordIgnoreCase(word) != null) {
            throw new ValidationException("Tag already exists: " + word);
        }

        return TagResponse.from(tagRepository.save(new Tag(word)));
    }

    public TagResponse updateTag(long id, TagRequest request) {
        Tag tag = findTag(id);
        String word = request.word().trim();
        Tag existing = tagRepository.findByWordIgnoreCase(word);
        if (existing != null && existing.getId() != id) {
            throw new ValidationException("Tag already exists: " + word);
        }

        tag.update(word);

        return TagResponse.from(tag);
    }

    @Transactional(readOnly = true)
    public TagResponse getTagById(long id) {
        return TagResponse.from(findTag(id));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> browseTags() {
        return tagRepository.findAll().stream()
            .map(TagResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> searchTags(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return browseTags();
        }

        return tagRepository.findByWordContainingIgnoreCase(normalizedQuery).stream()
            .map(TagResponse::from)
            .toList();
    }

    public void deleteTag(long id) {
        if (!tagRepository.existsById(id)) {
            throw new NotFoundException("Tag", id);
        }

        tagRepository.deleteById(id);
    }

    private Tag findTag(long id) {
        return tagRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tag", id));
    }
}
