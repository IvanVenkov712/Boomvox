package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.FavouritesList;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.*;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.repository.FavouritesListRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import bg.fmi.uni.boomvox.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService extends BaseService {

    private static final long RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final FavouritesListRepository favouritesListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        FavouritesListRepository favouritesListRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.favouritesListRepository = favouritesListRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()) != null) {
            throw new ValidationException("Username is already taken: " + request.username());
        }
        if (userRepository.findByEmail(request.email()) != null) {
            throw new ValidationException("Email is already taken: " + request.email());
        }

        FavouritesList favouritesList = new FavouritesList(
            request.username() + "'s favourites", LocalDateTime.now()
        );

        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            null,
            null,
            null,
            favouritesList
        );

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getUsername());
        return new AuthResponse(token, saved.getEmail(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public RefreshResponse refresh(User user) {
        String token = jwtService.generateToken(user.getUsername());
        return new RefreshResponse(token);
    }

    public void logout(User user) {
        // JWT is stateless — logout is handled client-side by discarding the token.
    }

    public PasswordResetTokenResponse sendPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null; // silent — don't leak whether email exists
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token, LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));
        userRepository.save(user);

        return new PasswordResetTokenResponse(token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new ValidationException("Invalid or expired reset token"));

        if (!user.isResetTokenValid(token)) {
            throw new ValidationException("Invalid or expired reset token");
        }

        user.resetPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}