package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.AdminPromoteRequest;
import bg.fmi.uni.boomvox.dto.UserResponse;
import bg.fmi.uni.boomvox.enums.UserRole;
import bg.fmi.uni.boomvox.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UserRole role
    ) {
        return ResponseEntity.ok(adminService.getUsers(search, role));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> promoteUser(
        @PathVariable Long userId,
        @RequestBody AdminPromoteRequest request
    ) {
        return ResponseEntity.ok(adminService.promoteUser(userId, request.role()));
    }
}
