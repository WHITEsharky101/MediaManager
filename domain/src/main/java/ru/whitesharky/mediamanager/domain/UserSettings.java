package ru.whitesharky.mediamanager.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

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
    @ElementCollection
    @CollectionTable(name = "medialib_paths", joinColumns = @JoinColumn(name = "settings_id"))
    private Set<String> path;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public UserSettings(String host, int port, String login, String hashPassword, Set<String> path) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.hashPassword = hashPassword;
        this.path = path;
    }

    public UserSettings() {
        this.host = "localhost";
        this.port = 8080;
        this.login = "user";
        this.hashPassword = "password";
        this.path = new HashSet<>();
        this.path.add("/");
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

    public Set<String> getPath() {
        return path;
    }

    public void setPath(Set<String> path) {
        this.path = path;
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
}
