package ru.whitesharky.mediamanager.adapter.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.whitesharky.mediamanager.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByLoginIgnoreCase(String login);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}