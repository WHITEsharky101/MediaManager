package ru.whitesharky.mediamanager.adapter.dto;

public class SettingsDto {
    private String libPath;
    private String host;
    private int port;
    private String login;
    private String password;
    private String torrentsPath;

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTorrentsPath() {
        return torrentsPath;
    }

    public void setTorrentsPath(String torrentsPath) {
        this.torrentsPath = torrentsPath;
    }
}
