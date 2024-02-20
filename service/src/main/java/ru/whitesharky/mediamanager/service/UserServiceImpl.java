package ru.whitesharky.mediamanager.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.whitesharky.mediamanager.adapter.dto.SettingsDto;
import ru.whitesharky.mediamanager.adapter.dto.UserDto;
import ru.whitesharky.mediamanager.adapter.hibernate.RoleRepo;
import ru.whitesharky.mediamanager.adapter.hibernate.SettingsRepo;
import ru.whitesharky.mediamanager.adapter.hibernate.UserRepo;
import ru.whitesharky.mediamanager.domain.RoleName;
import ru.whitesharky.mediamanager.domain.User;
import ru.whitesharky.mediamanager.domain.UserSettings;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private UserRepo userRepository;
    private RoleRepo roleRepository;
    private SettingsRepo settingsRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepository,
                           RoleRepo roleRepository,
                           SettingsRepo settingsRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerNewUserAccount(UserDto userDto) {
        User user = new User();
        user.setLogin(userDto.getLogin());
        user.setEmail(userDto.getEmail());
        user.setHashPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(RoleName.ROLE_USER)));
        userRepository.save(user);
    }

    public boolean isLoginExists(String login) {
        return userRepository.existsByLogin(login);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    @Override
    public User findUserByLogin(String login) {
        return userRepository.findByLoginIgnoreCase(login);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map((user) -> mapToUserDto(user))
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setLogin(user.getLogin());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    @Override
    public UserSettings findCurrentUserSettings() {
        return userRepository.findByLoginIgnoreCase(getCurrentUserLogin()).getSettings();
    }

    @Override
    public void saveSettings(SettingsDto settingsDto) {
        User user = userRepository.findByLoginIgnoreCase(getCurrentUserLogin());
        UserSettings userSettings = user.getSettings();
        if (userSettings == null) {
            userSettings = new UserSettings();
        }
        Set<String> set = new HashSet<>();
        set.add(settingsDto.getPath());
        userSettings.setPath(set);
        userSettings.setHost(settingsDto.getHost());
        userSettings.setPort(settingsDto.getPort());
        userSettings.setLogin(settingsDto.getLogin());
        userSettings.setHashPassword(Base64.getEncoder().encodeToString(settingsDto.getPassword().getBytes()));
        userSettings.setUser(user);
        settingsRepository.save(userSettings);
    }

    private String getCurrentUserLogin() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
