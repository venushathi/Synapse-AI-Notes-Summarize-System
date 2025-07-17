package com.notesapp.synapseainotesapp.service;

import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.UserRepository;
import com.notesapp.synapseainotesapp.repository.SubjectRepository;
import com.notesapp.synapseainotesapp.service.NoteService; // Import NoteService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final NoteService noteService; // Added NoteService

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, SubjectRepository subjectRepository, NoteService noteService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.noteService = noteService; // Injected NoteService
    }

    @Transactional
    public User registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email address already in use!");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(String email, String newName, String newImageUrl, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (newName != null && !newName.trim().isEmpty()) {
            user.setName(newName.trim());
        }


        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                throw new IllegalArgumentException("Current password is required to change password.");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password.");
            }
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters long.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        } else if (currentPassword != null && !currentPassword.isEmpty() && (newPassword == null || newPassword.isEmpty())) {
            throw new IllegalArgumentException("New password cannot be empty if you intend to change it.");
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public long getSubjectsCountForUser(Long userId) {

        return subjectRepository.findByUserIdOrderByNameAsc(userId).size();

    }

    @Transactional(readOnly = true)
    public long getNotesCountForUser(Long userId) { // Updated to use the correct method in NoteService
        return noteService.countNotesByUserId(userId);
    }

    @Transactional
    public void deleteUserAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email + ". Cannot delete."));

        // Cascading delete for subjects (and notes if configured) will be handled by JPA
        userRepository.delete(user);
    }
}
