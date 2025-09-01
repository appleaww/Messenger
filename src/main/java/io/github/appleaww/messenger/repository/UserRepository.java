package io.github.appleaww.messenger.repository;

import io.github.appleaww.messenger.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
