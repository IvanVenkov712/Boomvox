package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.domain.ListeningHistory;

import java.time.LocalDateTime;

public record ListeningHistoryResponse(
    long id,
    long songId,
    String songTitle,
    LocalDateTime listenedAt,
    long listenedDurationSec
) {
    public static ListeningHistoryResponse from(ListeningHistory history) {
        return new ListeningHistoryResponse(
            history.getId(),
            history.getSong().getId(),
            history.getSong().getName(),
            history.getListenedAt(),
            history.getListenedDurationSec()
        );
    }
}