package ru.whitesharky.mediamanager.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.whitesharky.mediamanager.domain.UserSettings;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QbitAPI extends API {
    private String username;
    private String password;
    private HttpCookie sessionCookie;
    private Long sessionExpDate;

    public QbitAPI(UserSettings userSettings) {
        super(userSettings.getHost(), userSettings.getPort(), "/api/v2/");
        this.username = userSettings.getLogin();
        this.password = new String(Base64.getDecoder().decode(userSettings.getHashPassword()));
        loginAndSetSID();
    }

    private void loginAndSetSID() {
        if (!isSIDActive()) {
            Map<String, String> postBody = new HashMap<>();
            postBody.put("username", username);
            postBody.put("password", password);
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
            HttpResponse<String> response = getResponse(makeRequest("auth/login", postBody));
            Pattern pattern = Pattern.compile("=(.*?);");
            Matcher matcher = pattern.matcher(response.headers().allValues("set-cookie").getFirst());
            if (matcher.find()) {
                HttpCookie httpCookie = new HttpCookie("SID", matcher.group(1));
                httpCookie.setVersion(0);
                httpCookie.setPath("/");
                setSessionCookie(httpCookie);
            }
            setSessionExpDate();
        }
    }

    private void setSessionExpDate() {
        this.sessionExpDate = System.currentTimeMillis() + (getSessionTimeout() - 10) * 60L;
    }

    private int getSessionTimeout() {
        HttpResponse<String> response = getAppPreferences();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj;
        try {
            actualObj = mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return actualObj.get("web_ui_session_timeout").asInt();
    }

    private boolean isSIDActive() {
        if (sessionExpDate == null) return false;
        return System.currentTimeMillis() <= sessionExpDate;
    }

    private HttpResponse<String> getAppPreferences() {
        return getResponse(makeRequest("app/preferences"), getSessionCookie());
    }

    public HttpResponse<String> getTorrentList(Map<String, String> parameters) {
        loginAndSetSID();
        return getResponse(makeRequest("torrents/info", parameters), getSessionCookie());
    }

    public HttpResponse<String> getTorrentList() {
        return getTorrentList(new HashMap<>());
    }

    public HttpResponse<String> getTorrentContent(String hash) {
        loginAndSetSID();
        return getResponse(makeRequest("torrents/files", Map.of("hash", hash)), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> renameTorrentFile(String hash, String oldPath, String newPath) {
        loginAndSetSID();
        Map<String, String> postBody = new HashMap<>();
        postBody.put("hash", hash);
        postBody.put("oldPath", oldPath);
        postBody.put("newPath", newPath);
        return getFuture(makeRequest("torrents/renameFile", postBody), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> renameFolder(String hash, String oldPath, String newPath) {
        loginAndSetSID();
        Map<String, String> postBody = new HashMap<>();
        postBody.put("hash", hash);
        postBody.put("oldPath", oldPath);
        postBody.put("newPath", newPath);
        return getFuture(makeRequest("torrents/renameFolder", postBody), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> recheckTorrent(Set<String> hashes) {
        loginAndSetSID();
        return getFuture(makeRequest("torrents/recheck",
                Map.of("hashes", String.join("|", hashes))), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> recheckTorrent(String hash) {
        return recheckTorrent(Set.of(hash));
    }

    public CompletableFuture<HttpResponse<String>> setCategory(Set<String> hashes, String category) {
        loginAndSetSID();
        return getFuture(makeRequest("torrents/setCategory",
                Map.of("hashes", String.join("|", hashes), "category", category)), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> setCategory(String hash, String category) {
        return setCategory(Set.of(hash), category);
    }

    public CompletableFuture<HttpResponse<String>> addTorrent(Set<String> urls, Map<String, String> parameters) {
        loginAndSetSID();
        parameters.put("urls", String.join("\r\n", urls));
        return getFuture(makeRequest("torrents/add", parameters), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> addTorrent(String url, String savePath, String newName, String category) {
        Map<String, String> postBody = new HashMap<>();
        postBody.put("savepath", savePath);
        postBody.put("category", category);
        postBody.put("rename", newName);
        postBody.put("root_folder", "false");
        postBody.put("skip_checking", "true");
        return addTorrent(Set.of(url), postBody);
    }

    public CompletableFuture<HttpResponse<String>> resume(Set<String> hashes) {
        loginAndSetSID();
        return getFuture(makeRequest("torrents/resume", Map.of("hashes", String.join("|", hashes))), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> resume(String hash) {
        return resume(Set.of(hash));
    }

    public CompletableFuture<HttpResponse<String>> pause(Set<String> hashes) {
        loginAndSetSID();
        return getFuture(makeRequest("torrents/pause", Map.of("hashes", String.join("|", hashes))), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> pause(String hash) {
        return pause(Set.of(hash));
    }

    public CompletableFuture<HttpResponse<String>> delete(Set<String> hashes, Boolean deleteFiles) {
        loginAndSetSID();
        Map<String, String> postBody = new HashMap<>();
        postBody.put("hashes", String.join("|", hashes));
        postBody.put("deleteFiles", deleteFiles.toString());
        return getFuture(makeRequest("torrents/delete", postBody), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> delete(String hash, Boolean deleteFiles) {
        return delete(Set.of(hash), deleteFiles);
    }

    public CompletableFuture<HttpResponse<String>> setLocation(Set<String> hashes, String location) {
        loginAndSetSID();
        Map<String, String> postBody = new HashMap<>();
        postBody.put("hashes", String.join("|", hashes));
        postBody.put("location", location);
        return getFuture(makeRequest("torrents/setLocation", postBody), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> setLocation(String hash, String location) {
        return setLocation(Set.of(hash), location);
    }

    public CompletableFuture<HttpResponse<String>> setName(String hash, String name) {
        loginAndSetSID();
        Map<String, String> postBody = new HashMap<>();
        postBody.put("hash", hash);
        postBody.put("name", name);
        return getFuture(makeRequest("torrents/setName", postBody), getSessionCookie());
    }

    public CompletableFuture<HttpResponse<String>> addCategory(String name) {
        loginAndSetSID();
        return getFuture(makeRequest("torrents/createCategory", Map.of("category", name)), getSessionCookie());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HttpCookie getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(HttpCookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }
}
