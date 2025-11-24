package io.github.appleaww.messenger.repository;

import io.github.appleaww.messenger.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional <User> findByUsername(String username);
    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.chats WHERE u.id = :userId")
    Optional<User> findWithChatsById(Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.chats c LEFT JOIN FETCH c.messages WHERE u.id = :userId")
    Optional<User> findWithChatsAndMessagesById(Long userId);
}
