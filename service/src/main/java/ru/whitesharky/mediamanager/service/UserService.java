package ru.whitesharky.mediamanager.service;

import ru.whitesharky.mediamanager.adapter.dto.SettingsDto;
import ru.whitesharky.mediamanager.adapter.dto.UserDto;
import ru.whitesharky.mediamanager.domain.User;
import ru.whitesharky.mediamanager.domain.UserSettings;

import java.util.List;

public interface UserService {
    void registerNewUserAccount(UserDto userDto);
    User findUserByLogin(String login);
    List<UserDto> findAllUsers();
    UserSettings findCurrentUserSettings();
    void saveSettings(SettingsDto settingsDto);
    boolean isLoginExists(String login);
    boolean isEmailExists(String email);
}