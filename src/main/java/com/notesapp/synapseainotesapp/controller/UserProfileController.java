package com.notesapp.synapseainotesapp.controller;

import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.UserRepository;
import com.notesapp.synapseainotesapp.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Autowired
    public UserProfileController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }


    public static class UpdateProfileRequest {
        private String name;
        private String imageUrl;
        private String currentPassword;
        private String newPassword;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated. Please log in."));
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User profile not found for email: " + email));

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", user.getId());
        profileData.put("name", user.getName());
        profileData.put("email", user.getEmail());
        if (user.getCreatedAt() != null) {
            profileData.put("createdAt", user.getCreatedAt().toString());
        }
        profileData.put("accountStatus", "Active");

        long subjectsCount = authService.getSubjectsCountForUser(user.getId());
        profileData.put("subjectsCount", subjectsCount);

        // Fetch and add notes count
        long notesCount = authService.getNotesCountForUser(user.getId());
        profileData.put("notesCount", notesCount);

        return ResponseEntity.ok(profileData);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UpdateProfileRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated."));
        }
        String email = authentication.getName();

        if (updateRequest.getName() == null || updateRequest.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name cannot be empty."));
        }
        if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
            if (updateRequest.getCurrentPassword() == null || updateRequest.getCurrentPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Current password is required to change password."));
            }
            if (updateRequest.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters long."));
            }
        }

        try {
            User updatedUser = authService.updateUserProfile(
                    email,
                    updateRequest.getName(),
                    updateRequest.getImageUrl(),
                    updateRequest.getCurrentPassword(),
                    updateRequest.getNewPassword()
            );

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", updatedUser.getId());
            responseData.put("name", updatedUser.getName());
            responseData.put("email", updatedUser.getEmail());
            if (updatedUser.getCreatedAt() != null) {
                responseData.put("createdAt", updatedUser.getCreatedAt().toString());
            }
            responseData.put("accountStatus", "Active");
            responseData.put("message", "Profile updated successfully!");
            return ResponseEntity.ok(responseData);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred while updating profile."));
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated."));
        }
        String email = authentication.getName();

        try {
            authService.deleteUserAccount(email);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully. You will be logged out."));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while deleting your account. Please try again later."));
        }
    }
}
