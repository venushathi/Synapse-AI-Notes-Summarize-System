package com.notesapp.synapseainotesapp.repository; // Ensure this package name is correct

import com.notesapp.synapseainotesapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Note> findByUserIdAndSubjectIdOrderByUpdatedAtDesc(Long userId, Long subjectId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(n.content AS String)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " + // Cast content to String
            "ORDER BY n.updatedAt DESC")
    List<Note> searchUserNotes(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND n.subject.id = :subjectId AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(n.content AS String)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " + // Cast content to String
            "ORDER BY n.updatedAt DESC")
    List<Note> searchUserNotesBySubject(@Param("userId") Long userId, @Param("subjectId") Long subjectId, @Param("searchTerm") String searchTerm);

    long countByUserId(Long userId);
    long countByUserIdAndSubjectId(Long userId, Long subjectId);

}
