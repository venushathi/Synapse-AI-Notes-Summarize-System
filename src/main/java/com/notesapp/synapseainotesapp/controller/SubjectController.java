package com.notesapp.synapseainotesapp.controller;

import com.notesapp.synapseainotesapp.model.Subject;
import com.notesapp.synapseainotesapp.service.SubjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public static class SubjectNameRequest {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


    private Map<String, Object> subjectToMap(Subject subject) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", subject.getId());
        map.put("name", subject.getName());

        return map;
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody SubjectNameRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Subject name cannot be empty."));
        }
        try {
            Subject newSubject = subjectService.createSubject(request.getName().trim());

            return ResponseEntity.status(HttpStatus.CREATED).body(subjectToMap(newSubject));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error creating subject."));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSubjectsForCurrentUser() {
        try {
            List<Subject> subjects = subjectService.getAllSubjectsForCurrentUser();
            // Map to a list of simplified maps
            List<Map<String, Object>> subjectMaps = subjects.stream()
                    .map(this::subjectToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(subjectMaps);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error fetching subjects."));
        }
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long subjectId) {
        try {
            Optional<Subject> subjectOptional = subjectService.getSubjectByIdForCurrentUser(subjectId);
            if (subjectOptional.isPresent()) {
                return ResponseEntity.ok(subjectToMap(subjectOptional.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Subject not found or not accessible."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error fetching subject."));
        }
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<?> updateSubject(@PathVariable Long subjectId, @RequestBody SubjectNameRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New subject name cannot be empty."));
        }
        try {
            Subject updatedSubject = subjectService.updateSubject(subjectId, request.getName().trim());
            return ResponseEntity.ok(subjectToMap(updatedSubject));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error updating subject."));
        }
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            return ResponseEntity.ok(Map.of("message", "Subject deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error deleting subject. Check if it's in use."));
        }
    }
}
