package com.notesapp.synapseainotesapp.controller;

import com.notesapp.synapseainotesapp.service.AuthService;
import com.notesapp.synapseainotesapp.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }


    /**
     * Endpoint for user registration.
     * Accepts a JSON payload with name, email, and password.
     *
     * @param registerRequest The registration request data.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {

        if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty() ||
                registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty() ||
                registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Name, email, and password are required."));
        }

        if (registerRequest.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Password must be at least 6 characters long."));
        }


        try {
            User newUser = authService.registerUser(
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully!"));


        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred during registration."));
        }
    }

}
