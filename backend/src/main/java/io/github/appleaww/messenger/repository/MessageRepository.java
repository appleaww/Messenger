package io.github.appleaww.messenger.repository;

import io.github.appleaww.messenger.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}

