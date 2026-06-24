package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.RecommendationResponse;
import bg.fmi.uni.boomvox.security.UserPrincipal;
import bg.fmi.uni.boomvox.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<RecommendationResponse>> generateRecommendations(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
            recommendationService.generateRecommendations(currentUser.getUser().getId(), limit)
        );
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
            recommendationService.getRecommendations(currentUser.getUser().getId(), limit)
        );
    }
}
