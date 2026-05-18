package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.FavouritesList;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.UserRequest;
import bg.fmi.uni.boomvox.dto.UserResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.repository.FavouritesListRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final FavouritesListRepository favouritesListRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserRepository userRepository,
        FavouritesListRepository favouritesListRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.favouritesListRepository = favouritesListRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(UserRequest request) {
        validateUserDoesNotExist(request);

        FavouritesList favouritesList = resolveFavouritesList(request);
        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.firstName(),
            request.lastName(),
            request.role(),
            favouritesList
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse login(String usernameOrEmail, String password) {
        User user = findByUsernameOrEmail(usernameOrEmail);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid username/email or password");
        }

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(long id) {
        return userRepository.findById(id)
            .map(UserResponse::from)
            .orElseThrow(() -> new NotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found with username: " + username);
        }

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + email);
        }

        return UserResponse.from(user);
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }

        userRepository.deleteById(id);
    }

    private void validateUserDoesNotExist(UserRequest request) {
        if (userRepository.findByUsername(request.username()) != null) {
            throw new ValidationException("Username is already taken: " + request.username());
        }

        if (userRepository.findByEmail(request.email()) != null) {
            throw new ValidationException("Email is already taken: " + request.email());
        }
    }

    private FavouritesList resolveFavouritesList(UserRequest request) {
        if (request.favouritesListId() != null) {
            return favouritesListRepository.findById(request.favouritesListId())
                .orElseThrow(() -> new NotFoundException("FavouritesList", request.favouritesListId()));
        }

        return new FavouritesList(request.username() + "'s favourites", LocalDateTime.now());
    }

    private User findByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user != null) {
            return user;
        }

        user = userRepository.findByEmail(usernameOrEmail);
        if (user == null) {
            throw new ValidationException("Invalid username/email or password");
        }

        return user;
    }
}
