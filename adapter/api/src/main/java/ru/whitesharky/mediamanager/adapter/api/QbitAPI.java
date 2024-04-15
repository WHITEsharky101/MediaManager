package ru.whitesharky.mediamanager.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.whitesharky.mediamanager.domain.Torrent;
import ru.whitesharky.mediamanager.domain.UserSettings;

import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.util.*;
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
    }

    private void loginAndSetSID() {
        if (!isSIDActive()) {
            Map<String, String> postBody = new HashMap<>();
            postBody.put("username", username);
            postBody.put("password", password);

            HttpResponse<String> response = getResponse(makeRequest("auth/login", postBody));
            Pattern pattern = Pattern.compile("=(.*?);");
            Matcher matcher = pattern.matcher(response.headers().allValues("set-cookie").getFirst());
            if (matcher.find()) {
                setSessionCookie(matcher.group(1));
            }
            setSessionExpDate();
        }
    }

    private void setSessionExpDate() {
        this.sessionExpDate = System.currentTimeMillis() + (getSessionTimeout()-10) * 60L;
    }

    private int getSessionTimeout() {
        HttpResponse<String> response = getResponse(makeRequest("app/preferences"), getSessionCookie());
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

    public ArrayList<Torrent> getTorrentList(Map<String, String> parameters) {
        loginAndSetSID();
        HttpResponse<String> response = getResponse(makeRequest("torrents/info", parameters), getSessionCookie());
        return splitInfoByTorrent(response.body());
    }

    public ArrayList<Torrent> getTorrentList() {
        return getTorrentList(new HashMap<>());
    }

    private ArrayList<Torrent> splitInfoByTorrent(String allTorrentInfo) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Torrent> torrentList = new ArrayList<>();
        JsonNode actualObj;
        try {
            actualObj = mapper.readTree(allTorrentInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (JsonNode oneObj : actualObj) {
            torrentList.add(new Torrent(
                    oneObj.get("name").asText(),
                    oneObj.get("content_path").asText(),
                    oneObj.get("hash").asText(),
                    oneObj.get("category").asText(),
                    oneObj.get("added_on").asInt())
            );
        }
        return torrentList;
    }

    public Torrent setTorrentContent(Torrent torrent) {
        loginAndSetSID();
        ObjectMapper mapper = new ObjectMapper();
        Set<String> torrentFilesName = new TreeSet<>();
        HttpResponse<String> response =
                getResponse(makeRequest("torrents/files", Map.of("hash", torrent.getHash())), getSessionCookie());
        JsonNode actualObj;
        try {
            actualObj = mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (JsonNode oneObj : actualObj) {
            torrentFilesName.add(oneObj.get("name").asText());
        }
        torrent.setTorrentFiles(torrentFilesName);
        return torrent;
    }

    private Torrent getTorrentFromName(String torrentName) {
        Torrent searchingTorrent = null;
        ArrayList<Torrent> torrensList = getTorrentList();
        for (Torrent torrent : torrensList) {
            if (torrent.getName().equals(torrentName)) searchingTorrent = torrent;
        }
        return searchingTorrent;
    }

    private void renameIncorrectFilesInTorrent(Torrent torrent) {
        torrent = setTorrentContent(torrent);
        String path = torrent.getPath();
        String hash = torrent.getHash();
        if (torrent.getTorrentFiles().size() == 1) {
            path = path.replaceAll("\\\\([^\\\\]+$)|/([^/]+$)", "");
        }
        if (path.charAt(0) == '/') path = path.substring(path.lastIndexOf("/") + 1);
        else path = path.substring(path.lastIndexOf("\\") + 1);

        Map<String, String> postBody = new HashMap<>();
        int index = 1;
        for (String torrentFile : torrent.getTorrentFiles()) {
            if (isTorrentFileNameContainsPattern(torrentFile)) break;
            postBody.clear();
            postBody.put("hash", hash);
            postBody.put("oldPath", torrentFile);
            postBody.put("newPath", makeNewPath(path, torrentFile, index++));
            getResponse(makeRequest("torrents/renameFile", postBody), getSessionCookie());
        }
    }

    private boolean isTorrentFileNameContainsPattern(String torrentFileName) {
        return torrentFileName.matches(".*[S,s]\\d+[E,e]\\d+.*");
    }

    public void renameIncorrectFilesInTorrentByName(String torrentName) {
        loginAndSetSID();
        Torrent torrent = getTorrentFromName(torrentName);
        renameIncorrectFilesInTorrent(torrent);
    }

    private String makeNewPath(String path, String torrentFile, int index) {
        String s = String.format("%sE%02d%s",
                path.replaceAll(" \\(.*\\)", ""),
                index,
                torrentFile.substring(torrentFile.lastIndexOf(".")));
        if (torrentFile.contains("/")) {
            return path + "/" + s;
        }
        return s;
    }

    public void setNewTorrentFiles(Map<String, String> torrents, String category) {
        final String tmpLibPath = "/home/whitesharky/disks/a/tmp/media/" + category + "/";
        List<String> media = new ArrayList<>(torrents.values());
        loginAndSetSID();
        int index = 0;
        for (String torrentPath : torrents.keySet()) {
            addNewTorrent(torrentPath, media.get(index++), tmpLibPath);
            delayFor(500);
            Torrent torrent = getTorrentList(Map.of("category", "autoNotSet", "reverse", "true")).getFirst();
            renameIncorrectFilesInTorrent(torrent);
            delayFor(200);
            getResponse(makeRequest("torrents/recheck",
                    Map.of("hashes", torrent.getHash())), getSessionCookie());
            delayFor(200);
            getResponse(makeRequest("torrents/setCategory",
                    Map.of("hashes", torrent.getHash(), "category", category)), getSessionCookie());
            delayFor(200);
            getResponse(makeRequest("torrents/resume",
                    Map.of("hashes", torrent.getHash())), getSessionCookie());
        }
    }

    private void addNewTorrent(String torrentPath, String media, String tmpLibPath) {
        Map<String, String> postBody = new HashMap<>();
        String[] mediaNameAndMeta = media.split("\\[");
        String mediaName = mediaNameAndMeta[0].trim().replaceAll("\\?", "");
        String mediaMeta = mediaNameAndMeta[1].replaceAll("]", "");
        postBody.put("urls", torrentPath);
        postBody.put("savepath", String.format("%s%s/%s %s", tmpLibPath, mediaName, mediaName, mediaMeta));
        postBody.put("category", "autoNotSet");
        postBody.put("root_folder", "false");
        postBody.put("rename", String.format("%s %s", mediaName, mediaMeta));
        getResponse(makeRequest("torrents/add", postBody), getSessionCookie());
    }

    private void delayFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    public void setSessionCookie(String sessionID) {
        sessionCookie = new HttpCookie("SID", sessionID);
        sessionCookie.setVersion(0);
        sessionCookie.setPath("/");
    }
}
