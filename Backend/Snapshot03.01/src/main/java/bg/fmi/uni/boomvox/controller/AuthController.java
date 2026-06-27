package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.AuthResponse;
import bg.fmi.uni.boomvox.dto.ForgotPasswordRequest;
import bg.fmi.uni.boomvox.dto.LoginRequest;
import bg.fmi.uni.boomvox.dto.PasswordResetTokenResponse;
import bg.fmi.uni.boomvox.dto.RefreshResponse;
import bg.fmi.uni.boomvox.dto.RegisterRequest;
import bg.fmi.uni.boomvox.dto.ResetPasswordRequest;
import bg.fmi.uni.boomvox.security.UserPrincipal;
import bg.fmi.uni.boomvox.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(authService.refresh(currentUser.getUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal currentUser) {
        authService.logout(currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetTokenResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        PasswordResetTokenResponse response = authService.sendPasswordReset(request.email());
        return response != null
            ? ResponseEntity.ok(response)
            : ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
