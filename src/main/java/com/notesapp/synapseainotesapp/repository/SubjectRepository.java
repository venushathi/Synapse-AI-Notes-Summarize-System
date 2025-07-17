package com.notesapp.synapseainotesapp.repository; // Ensure this package name is correct

import com.notesapp.synapseainotesapp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Finds all subjects belonging to a specific user, ordered by name.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @param userId The ID of the user.
     * @return A list of subjects for the given user, ordered by name.
     */
    List<Subject> findByUserIdOrderByNameAsc(Long userId);

    /**
     * Finds a subject by its name and user ID.
     * This is useful to check if a subject with a given name already exists for a user.
     *
     * @param name The name of the subject.
     * @param userId The ID of the user.
     * @return An Optional containing the Subject if found, or an empty Optional otherwise.
     */
    Optional<Subject> findByNameAndUserId(String name, Long userId);

    /**
     * Checks if a subject with the given name already exists for a specific user.
     *
     * @param name The name of the subject.
     * @param userId The ID of the user.
     * @return true if a subject with the name exists for the user, false otherwise.
     */
    boolean existsByNameAndUserId(String name, Long userId);


}
