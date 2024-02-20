package ru.whitesharky.mediamanager.adapter.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.whitesharky.mediamanager.adapter.dto.SettingsDto;
import ru.whitesharky.mediamanager.domain.UserSettings;
import ru.whitesharky.mediamanager.service.UserService;

@Controller
public class SettingsController {
    private UserService userService;
    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String showSettingsForm(Model model) {
        UserSettings userSettings = userService.findCurrentUserSettings();
        if (userSettings == null) {
            userSettings = new UserSettings();
        }
        model.addAttribute("settings", mapToSettingsDto(userSettings));
        return "/settings";
    }

    private SettingsDto mapToSettingsDto(UserSettings userSettings) {
        SettingsDto settingsDto = new SettingsDto();
        settingsDto.setPath(userSettings.getPath().stream().findFirst().get());
        settingsDto.setHost(userSettings.getHost());
        settingsDto.setPort(userSettings.getPort());
        settingsDto.setLogin(userSettings.getLogin());
        return settingsDto;
    }

    @PostMapping("/settings")
    public String setSettings(@Valid @ModelAttribute("settings") SettingsDto settingsDto) {
        userService.saveSettings(settingsDto);
        return "redirect:/settings?success";
    }
}