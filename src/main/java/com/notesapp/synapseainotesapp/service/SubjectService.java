package com.notesapp.synapseainotesapp.service;

import com.notesapp.synapseainotesapp.model.Subject;
import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.SubjectRepository;
import com.notesapp.synapseainotesapp.repository.UserRepository;
// DTOs will be created later, e.g., SubjectRequestDto, SubjectResponseDto
// For now, we'll pass parameters directly or use the entity.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository; // To fetch the current user

    @Autowired
    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    // Helper method to get the currently authenticated user
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated for this operation.");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
    }

    @Transactional
    public Subject createSubject(String name) {
        User currentUser = getCurrentAuthenticatedUser();

        // Check if subject with the same name already exists for this user
        if (subjectRepository.existsByNameAndUserId(name, currentUser.getId())) {
            throw new IllegalArgumentException("Subject with name '" + name + "' already exists for this user.");
        }

        Subject subject = new Subject();
        subject.setName(name);
        subject.setUser(currentUser);
        // createdAt and updatedAt will be handled by @CreationTimestamp and @UpdateTimestamp

        return subjectRepository.save(subject);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjectsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return subjectRepository.findByUserIdOrderByNameAsc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Subject> getSubjectByIdForCurrentUser(Long subjectId) {
        User currentUser = getCurrentAuthenticatedUser();
        // Fetch subject by ID and also verify it belongs to the current user
        return subjectRepository.findById(subjectId)
                .filter(subject -> subject.getUser().getId().equals(currentUser.getId()));
    }

    @Transactional
    public Subject updateSubject(Long subjectId, String newName) {
        User currentUser = getCurrentAuthenticatedUser();
        Subject subjectToUpdate = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        // Check if the subject belongs to the current user
        if (!subjectToUpdate.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to update this subject.");
        }

        // Check if the new name already exists for another subject of this user
        if (!subjectToUpdate.getName().equalsIgnoreCase(newName) &&
                subjectRepository.existsByNameAndUserId(newName, currentUser.getId())) {
            throw new IllegalArgumentException("Another subject with name '" + newName + "' already exists.");
        }

        subjectToUpdate.setName(newName);
        return subjectRepository.save(subjectToUpdate);
    }

    @Transactional
    public void deleteSubject(Long subjectId) {
        User currentUser = getCurrentAuthenticatedUser();
        Subject subjectToDelete = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        // Check if the subject belongs to the current user
        if (!subjectToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to delete this subject.");
        }


        subjectRepository.delete(subjectToDelete);
    }
}
