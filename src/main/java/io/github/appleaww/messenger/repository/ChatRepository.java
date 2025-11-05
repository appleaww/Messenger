package io.github.appleaww.messenger.repository;

import io.github.appleaww.messenger.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id IN (:user1Id, :user2Id) GROUP BY c HAVING COUNT(DISTINCT p.id) = 2")
    Optional<Chat> findExistingChatBetweenUserIds(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}

