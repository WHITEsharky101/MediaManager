package ru.whitesharky.mediamanager.service;

import java.util.Map;

public interface QbitService {
    void setNewTorrentFiles(Map<String, String> torrents, String category);
    void renameIncorrectFilesInTorrentByName(String name);
    void initQbitAPI();
}
