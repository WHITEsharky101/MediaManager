package ru.whitesharky.mediamanager.adapter.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.whitesharky.mediamanager.domain.UserSettings;

public interface SettingsRepo extends JpaRepository<UserSettings, Long> {
}
