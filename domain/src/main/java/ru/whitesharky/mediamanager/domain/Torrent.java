package ru.whitesharky.mediamanager.domain;

import java.util.Set;

public class Torrent {
    private String name;
    private String path;
    private String category;
    private final String hash;
    private final int addDate;
    private Set<String> torrentFiles;

    public Torrent(String name, String path, String hash, String category, int date) {
        this.name = name;
        this.path = path;
        this.hash = hash;
        this.category = category;
        this.addDate = date;
    }

    public int getAddDate() {
        return addDate;
    }

    public String getHash() {
        return hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Set<String> getTorrentFiles() {
        return torrentFiles;
    }

    public void setTorrentFiles(Set<String> torrentFiles) {
        this.torrentFiles = torrentFiles;
    }
}