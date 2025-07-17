package com.notesapp.synapseainotesapp.controller;

import com.notesapp.synapseainotesapp.model.Note;
import com.notesapp.synapseainotesapp.model.Subject;
import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.SubjectRepository;
import com.notesapp.synapseainotesapp.repository.UserRepository;
import com.notesapp.synapseainotesapp.service.AIService;
import com.notesapp.synapseainotesapp.service.NoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;
    private final AIService aiService;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    private static final List<String> DEFAULT_CANDIDATE_LABELS = Arrays.asList(
            "Personal", "Work", "Ideas", "Tasks", "Meetings", "Travel", "Finance", "Health", "Education", "Shopping",
            "Books", "Movies", "Music", "Recipes", "Home Management", "Goals", "Journal",
            "Programming", "Java", "Spring Boot", "JavaScript", "Python", "SQL", "Database Design", "Web Development",
            "Frontend", "Backend", "API Design", "Mobile Development", "Android", "iOS", "AI", "Machine Learning",
            "Data Science", "DevOps", "Cloud Computing", "AWS", "Azure", "GCP", "Algorithms", "Data Structures",
            "Software Architecture", "Project Management", "Agile", "Scrum", "Bug Report", "Feature Request",
            "Code Snippets", "Tutorial Notes", "Research", "Security"
    );

    @Autowired
    public NoteController(NoteService noteService, AIService aiService, UserRepository userRepository, SubjectRepository subjectRepository) {
        this.noteService = noteService;
        this.aiService = aiService;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated for this operation.");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
    }

    public static class NoteRequest {
        private String title;
        private String content;
        private Long subjectId;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    }

    public static class CategorySuggestionRequest {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }


    private Map<String, Object> noteToMap(Note note) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", note.getId());
        map.put("title", note.getTitle());
        map.put("content", note.getContent());
        map.put("summary", note.getSummary());
        if (note.getSubject() != null) {
            Map<String, Object> subjectMap = new HashMap<>();
            subjectMap.put("id", note.getSubject().getId());
            subjectMap.put("name", note.getSubject().getName());
            map.put("subject", subjectMap);
        } else {
            map.put("subject", null);
        }
        map.put("createdAt", note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        map.put("updatedAt", note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null);
        return map;
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody NoteRequest noteRequest) {
        if (!StringUtils.hasText(noteRequest.getTitle()) || !StringUtils.hasText(noteRequest.getContent())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title and content cannot be empty."));
        }
        try {
            Note newNote = noteService.createNote(
                    noteRequest.getTitle(),
                    noteRequest.getContent(),
                    noteRequest.getSubjectId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(noteToMap(newNote));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error creating note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error creating note."));
        }
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getAllNotesForCurrentUser(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String searchTerm) {
        try {
            List<Note> notes = noteService.getAllNotesForCurrentUser(subjectId, searchTerm);
            List<Map<String, Object>> noteMaps = notes.stream()
                    .map(this::noteToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(noteMaps);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error fetching notes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error fetching notes."));
        }
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId) {
        try {
            Optional<Note> noteOptional = noteService.getNoteByIdForCurrentUser(noteId);
            if (noteOptional.isPresent()) {
                return ResponseEntity.ok(noteToMap(noteOptional.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Note not found or not accessible."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error fetching note by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error fetching note."));
        }
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable Long noteId, @RequestBody NoteRequest noteRequest) {
        if (!StringUtils.hasText(noteRequest.getTitle()) || !StringUtils.hasText(noteRequest.getContent())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title and content cannot be empty for update."));
        }
        try {
            Note updatedNote = noteService.updateNote(
                    noteId,
                    noteRequest.getTitle(),
                    noteRequest.getContent(),
                    noteRequest.getSubjectId()
            );
            return ResponseEntity.ok(noteToMap(updatedNote));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error updating note."));
        }
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId) {
        try {
            noteService.deleteNote(noteId);
            return ResponseEntity.ok(Map.of("message", "Note deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error deleting note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error deleting note."));
        }
    }

    @PostMapping("/notes/{noteId}/summarize")
    public ResponseEntity<?> getSummaryForNote(@PathVariable Long noteId) {
        try {
            String summary = noteService.summarizeAndSaveNoteContent(noteId);

            if ("Note content is empty, cannot summarize.".equals(summary)) {
                return ResponseEntity.badRequest().body(Map.of("message", summary));
            } else if (summary != null && summary.startsWith("Error:")) {
                System.err.println("Summarization service error: " + summary);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to generate summary due to an internal error."));
            } else if (summary == null) {
                System.err.println("Summarization service returned null unexpectedly.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to generate summary (AI service returned null)."));
            }
            return ResponseEntity.ok(Map.of("summary", summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error during summarization: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred while summarizing the note."));
        }
    }

    @PostMapping("/ai/suggest-category")
    public ResponseEntity<?> suggestCategoryForText(@RequestBody CategorySuggestionRequest request) {
        if (!StringUtils.hasText(request.getText())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Text to categorize cannot be empty."));
        }
        try {
            User currentUser = getCurrentAuthenticatedUser();


            String suggestedLabel = aiService.suggestCategory(request.getText(), DEFAULT_CANDIDATE_LABELS);

            if (suggestedLabel != null && suggestedLabel.startsWith("Error:")) {
                System.err.println("Category suggestion service error: " + suggestedLabel);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to suggest category due to an AI service error."));
            } else if (suggestedLabel == null) {
                System.err.println("Category suggestion service returned null unexpectedly.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to suggest category (AI service returned null)."));
            }


            List<Subject> userSubjects = subjectRepository.findByUserIdOrderByNameAsc(currentUser.getId());
            Optional<Subject> existingUserSubjectMatch = userSubjects.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(suggestedLabel))
                    .findFirst();

            if (existingUserSubjectMatch.isPresent()) {

                Subject matchedSubject = existingUserSubjectMatch.get();
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("subjectId", matchedSubject.getId());
                responseMap.put("subjectName", matchedSubject.getName());
                responseMap.put("isExisting", true);
                return ResponseEntity.ok(responseMap);
            } else {

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("suggestedCategoryName", suggestedLabel);
                responseMap.put("isExisting", false);
                return ResponseEntity.ok(responseMap);
            }

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error during category suggestion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred while suggesting a category."));
        }
    }
}
