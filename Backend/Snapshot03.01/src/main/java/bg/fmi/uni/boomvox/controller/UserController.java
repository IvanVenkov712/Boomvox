package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.ListeningHistoryResponse;
import bg.fmi.uni.boomvox.dto.UpdateUserRequest;
import bg.fmi.uni.boomvox.dto.UserPreferenceResponse;
import bg.fmi.uni.boomvox.dto.UserResponse;
import bg.fmi.uni.boomvox.security.UserPrincipal;
import bg.fmi.uni.boomvox.service.UserPreferenceService;
import bg.fmi.uni.boomvox.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserPreferenceService userPreferenceService;

    public UserController(UserService userService, UserPreferenceService userPreferenceService) {
        this.userService = userService;
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getUser().getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(currentUser.getUser().getId(), request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/me/listening-history")
    public ResponseEntity<Page<ListeningHistoryResponse>> getListeningHistory(
        @AuthenticationPrincipal UserPrincipal currentUser,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            userService.getListeningHistory(currentUser.getUser().getId(), pageable)
        );
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<UserPreferenceResponse> getPreferences(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(
            userPreferenceService.getPreferences(currentUser.getUser().getId())
        );
    }
}
