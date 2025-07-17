package com.notesapp.synapseainotesapp.repository; // Ensure this package name is correct

import com.notesapp.synapseainotesapp.model.User; // Import the User entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or an empty Optional otherwise.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email address already exists.
     * This is useful for the registration process to prevent duplicate emails.
     *
     * @param email The email address to check.
     * @return true if a user with the email exists, false otherwise.
     */
    Boolean existsByEmail(String email);


}
