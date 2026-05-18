package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.Tag;

public record TagResponse(
    long id,
    String word
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getWord());
    }
}
