package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.RatingRequest;
import bg.fmi.uni.boomvox.dto.RatingResponse;
import bg.fmi.uni.boomvox.dto.SongStatsResponse;
import bg.fmi.uni.boomvox.security.UserPrincipal;
import bg.fmi.uni.boomvox.service.EngagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EngagementController {

    private final EngagementService engagementService;

    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @PostMapping("/songs/{songId}/ratings")
    public ResponseEntity<RatingResponse> rateSong(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long songId,
        @Valid @RequestBody RatingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(engagementService.rateSong(currentUser.getUser().getId(), request));
    }

    @GetMapping("/songs/{songId}/ratings/me")
    public ResponseEntity<RatingResponse> getMyRating(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long songId
    ) {
        return ResponseEntity.ok(
            engagementService.getRating(currentUser.getUser().getId(), songId)
        );
    }

    @DeleteMapping("/songs/{songId}/ratings/me")
    public ResponseEntity<Void> deleteMyRating(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long songId
    ) {
        engagementService.deleteRating(currentUser.getUser().getId(), songId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/songs/{songId}/ratings")
    public ResponseEntity<List<RatingResponse>> getSongFeedback(@PathVariable long songId) {
        return ResponseEntity.ok(engagementService.getSongFeedback(songId));
    }

    @GetMapping("/songs/{songId}/ratings/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable long songId) {
        return ResponseEntity.ok(engagementService.getAverageSongRating(songId));
    }

    @GetMapping("/users/me/ratings")
    public ResponseEntity<List<RatingResponse>> getMyRatings(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(
            engagementService.getRatingsByUser(currentUser.getUser().getId())
        );
    }

    @GetMapping("/songs/{songId}/stats")
    public ResponseEntity<SongStatsResponse> getSongStats(@PathVariable long songId) {
        return ResponseEntity.ok(engagementService.refreshSongRatingStats(songId));
    }
}
