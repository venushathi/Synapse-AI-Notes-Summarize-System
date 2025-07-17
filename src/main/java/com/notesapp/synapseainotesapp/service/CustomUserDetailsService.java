package com.notesapp.synapseainotesapp.service; // Ensure this package name is correct

import com.notesapp.synapseainotesapp.model.User;
import com.notesapp.synapseainotesapp.repository.UserRepository;
import com.notesapp.synapseainotesapp.security.UserPrincipal; // This class will need to be created/confirmed next
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Marks this class as a Spring service component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired // Constructor injection for UserRepository
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user by their email address (which is used as the username for authentication).
     * This method is called by Spring Security during the authentication process.
     *
     * @param email The email address (username) of the user to load.
     * @return UserDetails object (UserPrincipal in our case) containing the user's information.
     * @throws UsernameNotFoundException if the user with the given email is not found.
     */
    @Override
    @Transactional(readOnly = true) // Marks the method as transactional and read-only
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        // Create and return a UserPrincipal object using the found User entity
        return UserPrincipal.create(user);
    }

    /**
     * Optional: Method to load user by ID.
     * This can be used by Spring Security in some scenarios, e.g., if you are using JWTs
     * and need to re-fetch user details based on ID stored in the token.
     *
     * @param id The ID of the user to load.
     * @return UserDetails object (UserPrincipal).
     * @throws UsernameNotFoundException if the user with the given ID is not found.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id : " + id)
        );
        return UserPrincipal.create(user);
    }
}
