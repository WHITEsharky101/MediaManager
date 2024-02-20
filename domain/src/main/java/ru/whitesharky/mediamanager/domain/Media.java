package ru.whitesharky.mediamanager.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String description;
    private String torrentHash;
    private Date releaseDate;
    private int runtime;
    private String dbLink;
    private String category;
    private String path;
    private boolean scanCheck;

    public Media() {
    }

    public Media(String name, String category, String path) {
        this.name = name;
        this.category = category;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTorrentHash() {
        return torrentHash;
    }

    public void setTorrentHash(String torrentHash) {
        this.torrentHash = torrentHash;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getDbLink() {
        return dbLink;
    }

    public void setDbLink(String dbLink) {
        this.dbLink = dbLink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isScanCheck() {
        return scanCheck;
    }

    public void setScanCheck(boolean scanCheck) {
        this.scanCheck = scanCheck;
    }
}
