package com.example.informationprotection.service.license;

import com.example.informationprotection.entity.User;
import com.example.informationprotection.exception.ForbiddenOperationException;
import com.example.informationprotection.exception.NotFoundException;
import com.example.informationprotection.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserService {

    private final UserRepository userRepository;

    public ApplicationUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ForbiddenOperationException("User is not authenticated");
        }
        return getActiveUserOrFailByUsername(authentication.getName()).getId();
    }

    public User getActiveUserOrFail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        ensureAccountActive(user);
        return user;
    }

    public User getActiveUserOrFailByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        ensureAccountActive(user);
        return user;
    }

    public void ensureAccountActive(User user) {
        boolean active = Boolean.TRUE.equals(user.getIsActive())
                && !Boolean.TRUE.equals(user.getIsAccountExpired())
                && !Boolean.TRUE.equals(user.getIsAccountLocked())
                && !Boolean.TRUE.equals(user.getIsCredentialsExpired())
                && !Boolean.TRUE.equals(user.getIsDisabled());

        if (!active) {
            throw new ForbiddenOperationException("User account is not active");
        }
    }
}
