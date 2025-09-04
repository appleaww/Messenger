package io.github.appleaww.messenger.repository;

import io.github.appleaww.messenger.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {

}
