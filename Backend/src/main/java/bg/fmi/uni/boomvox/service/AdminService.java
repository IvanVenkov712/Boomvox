package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.UserResponse;
import bg.fmi.uni.boomvox.enums.UserRole;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<UserResponse> getUsers(String search, UserRole role) {
        return userRepository.findBySearchAndRole(search, role == null ? null : role.name())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public UserResponse promoteUser(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getFavouritesList().getId()
        );
    }
}
