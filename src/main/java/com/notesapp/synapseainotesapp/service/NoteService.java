package com.notesapp.synapseainotesapp.service;

import com.notesapp.synapseainotesapp.model.Note;
import com.notesapp.synapseainotesapp.model.Subject;
import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.NoteRepository;
import com.notesapp.synapseainotesapp.repository.SubjectRepository;
import com.notesapp.synapseainotesapp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final AIService aiService;

    @Autowired
    public NoteService(NoteRepository noteRepository, UserRepository userRepository, SubjectRepository subjectRepository, AIService aiService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.aiService = aiService;
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

    @Transactional
    public Note createNote(String title, String content, Long subjectId) {
        User currentUser = getCurrentAuthenticatedUser();

        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Note title cannot be empty.");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Note content cannot be empty.");
        }

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUser(currentUser);
        // Initially, summary can be null or empty
        note.setSummary(null);

        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId)
                    .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Subject ID or subject does not belong to user."));
            note.setSubject(subject);
        }

        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotesForCurrentUser(Long subjectIdFilter, String searchTerm) {
        User currentUser = getCurrentAuthenticatedUser();
        Long currentUserId = currentUser.getId();

        if (StringUtils.hasText(searchTerm)) {
            if (subjectIdFilter != null) {
                return noteRepository.searchUserNotesBySubject(currentUserId, subjectIdFilter, searchTerm);
            } else {
                return noteRepository.searchUserNotes(currentUserId, searchTerm);
            }
        } else {
            if (subjectIdFilter != null) {
                return noteRepository.findByUserIdAndSubjectIdOrderByUpdatedAtDesc(currentUserId, subjectIdFilter);
            } else {
                return noteRepository.findByUserIdOrderByUpdatedAtDesc(currentUserId);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Note> getNoteByIdForCurrentUser(Long noteId) {
        User currentUser = getCurrentAuthenticatedUser();
        return noteRepository.findByIdAndUserId(noteId, currentUser.getId());
    }

    @Transactional
    public Note updateNote(Long noteId, String newTitle, String newContent, Long newSubjectId) {
        User currentUser = getCurrentAuthenticatedUser();
        Note noteToUpdate = noteRepository.findByIdAndUserId(noteId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId + " or user not authorized."));

        if (StringUtils.hasText(newTitle)) {
            noteToUpdate.setTitle(newTitle);
        } else {
            throw new IllegalArgumentException("Note title cannot be empty for update.");
        }

        if (StringUtils.hasText(newContent)) {
            noteToUpdate.setContent(newContent);
        } else {
            throw new IllegalArgumentException("Note content cannot be empty for update.");
        }
        // Summary is not directly updated here, only via summarizeNoteContent method

        if (newSubjectId != null) {
            if (newSubjectId.equals(0L)) {
                noteToUpdate.setSubject(null);
            } else {
                Subject subject = subjectRepository.findById(newSubjectId)
                        .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid new Subject ID or subject does not belong to user."));
                noteToUpdate.setSubject(subject);
            }
        }
        return noteRepository.save(noteToUpdate);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        User currentUser = getCurrentAuthenticatedUser();
        Note noteToDelete = noteRepository.findByIdAndUserId(noteId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId + " or user not authorized."));
        noteRepository.delete(noteToDelete);
    }

    @Transactional(readOnly = true)
    public long countNotesForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return noteRepository.countByUserId(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public long countNotesByUserId(Long userId) {
        return noteRepository.countByUserId(userId);
    }

    @Transactional
    public String summarizeAndSaveNoteContent(Long noteId) {
        User currentUser = getCurrentAuthenticatedUser();
        Note note = noteRepository.findByIdAndUserId(noteId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId + " for the current user."));

        if (!StringUtils.hasText(note.getContent())) {

            return "Note content is empty, cannot summarize.";
        }

        String generatedSummary = aiService.summarizeText(note.getContent());

        if (generatedSummary != null && !generatedSummary.startsWith("Error:")) {
            note.setSummary(generatedSummary);
            noteRepository.save(note);
            return generatedSummary;
        } else if (generatedSummary != null && generatedSummary.startsWith("Error:")) {

            return generatedSummary;
        } else {

            return "Error: Failed to generate summary (null response from AI service).";
        }
    }
}
