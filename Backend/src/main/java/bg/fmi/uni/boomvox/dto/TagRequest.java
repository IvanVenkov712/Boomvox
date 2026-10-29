package bg.fmi.uni.boomvox.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
    @NotBlank
    String word
) {
    public static TagRequest of(String word) {
        return new TagRequest(word);
    }
}
