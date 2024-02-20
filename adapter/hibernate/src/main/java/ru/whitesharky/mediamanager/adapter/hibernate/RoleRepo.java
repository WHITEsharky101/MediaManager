package ru.whitesharky.mediamanager.adapter.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.whitesharky.mediamanager.domain.Role;
import ru.whitesharky.mediamanager.domain.RoleName;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
}
