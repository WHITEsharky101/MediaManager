package ru.whitesharky.mediamanager.adapter.controller;

import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.whitesharky.mediamanager.adapter.api.QbitAPI;
import ru.whitesharky.mediamanager.domain.UserSettings;
import ru.whitesharky.mediamanager.service.UserService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class MediaController {

    private UserService userService;

    public MediaController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/qbittorrent")
    public String showTorrentControlForm(Model model) {
        Map<File, String> torrentFiles = listTorrentFilesUsingJavaIO(userService.findCurrentUserSettings().getTorrentsPath());
        model.addAttribute("torrentFiles", torrentFiles);
        return "qbittorrent";
    }

    public Map<File, String> listTorrentFilesUsingJavaIO(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".torrent"))
                .collect(Collectors.toMap(File::getAbsoluteFile, File::getName));
    }

    @PostMapping("/qbittorrent/rename")
    public String renameTorrentFiles(@RequestParam(value = "torrentName", required = false) String torrentName) {
        UserSettings userSettings = userService.findCurrentUserSettings();
        QbitAPI qbitAPI = new QbitAPI(userSettings);
        qbitAPI.renameIncorrectFilesInTorrentByName(torrentName);
        return "redirect:/qbittorrent";
    }

    @PostMapping("/qbittorrent/addTorrent")
    public String addTorrent(@RequestParam("torrentFile") Set<File> torrentFiles,
                             @RequestParam(value = "torrentName", required = true) String torrentNames,
                             @RequestParam(value = "torrentCategory", required = true) String torrentCategory) {
        Map<String, String> torrents = new HashMap<>();
        String[] torrentNamesArray = torrentNames.trim().split("\r\n");
        if(torrentFiles != null && torrentFiles.size() == torrentNamesArray.length) {
            int i = 0;
            for (File torrentFile: torrentFiles) {
                torrents.put(torrentFile.getAbsolutePath(), torrentNamesArray[i]);
                i++;
            }
            UserSettings userSettings = userService.findCurrentUserSettings();
            QbitAPI qbitAPI = new QbitAPI(userSettings);
            qbitAPI.setNewTorrentFiles(torrents, torrentCategory);
        }
        return "redirect:/qbittorrent";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Set.class, new CustomCollectionEditor(Set.class));
    }
}
