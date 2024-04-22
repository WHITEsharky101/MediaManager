package ru.whitesharky.mediamanager.domain;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @Column(name = "user_id")
    private Long id;
    private String host;
    private int port;
    private String login;
    private String hashPassword;
    private String torrentsPath;
    @ElementCollection
    @CollectionTable(name = "medialib_paths", joinColumns = @JoinColumn(name = "settings_id"))
    private Set<String> libPath;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public UserSettings(String host, int port, String login, String hashPassword, Set<String> libPath, String torrentsPath) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.hashPassword = hashPassword;
        this.libPath = libPath;
        this.torrentsPath = torrentsPath;
    }

    public UserSettings() {
        this.host = "localhost";
        this.port = 8080;
        this.login = "user";
        this.hashPassword = "password";
        this.libPath = new HashSet<>();
        this.libPath.add("/");
        this.torrentsPath = "/";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public Set<String> getLibPath() {
        return libPath;
    }

    public void setLibPath(Set<String> libPath) {
        this.libPath = libPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTorrentsPath() {
        return torrentsPath;
    }

    public void setTorrentsPath(String torrentsPath) {
        this.torrentsPath = torrentsPath;
    }
}
