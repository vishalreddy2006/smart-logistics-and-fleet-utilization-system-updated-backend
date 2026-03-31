package com.smartlogistics.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.smartlogistics.entity.User;
import com.smartlogistics.repository.UserRepository;

@Component
public class UserContext {

    private final UserRepository userRepository;

    public UserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extracts the current logged-in user's ID from the security context.
     * 
     * @return the ID of the currently authenticated user
     * @throws IllegalStateException if no user is authenticated or user not found
     */
    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        String email = principal.toString();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));

        return user.getId();
    }

    /**
     * Safely gets the current user ID or returns null if not authenticated.
     * 
     * @return the ID of the currently authenticated user, or null if not authenticated
     */
    public Long getCurrentUserIdOrNull() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                return null;
            }

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal == null) {
                return null;
            }

            String email = principal.toString();
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
