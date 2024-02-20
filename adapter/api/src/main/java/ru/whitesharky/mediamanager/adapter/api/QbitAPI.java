package ru.whitesharky.mediamanager.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.whitesharky.mediamanager.domain.Torrent;

import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QbitAPI extends API {
    private String username;
    private String password;
    private HttpCookie SessionCookie;

    public QbitAPI(String host, int port, String username, String password) {
        super(host, port, "/api/v2/");
        this.username = username;
        this.password = password;
    }

    public void loginAndSetSID() {
        Map<String, String> postBody = new HashMap<>();
        postBody.put("username", username);
        postBody.put("password", password);

        HttpResponse<String> response = getResponse(makeRequest("auth/login", postBody));
        Pattern pattern = Pattern.compile("=(.*?);");
        Matcher matcher = pattern.matcher(response.headers().allValues("set-cookie").getFirst());
        if (matcher.find()) {
            setSessionCookie(matcher.group(1));}
    }

    public ArrayList<Torrent> getTorrentList() {
        HttpResponse<String> response = getResponse(makeRequest("torrents/info"), getSessionCookie());
        return splitInfoByTorrent(response.body());
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
        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, String> torrentFileName = new HashMap<>();
        HttpResponse<String> response =
                getResponse(makeRequest("torrents/files", Map.of("hash", torrent.getHash())), getSessionCookie());
        JsonNode actualObj;
        try {
            actualObj = mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (JsonNode oneObj : actualObj) {
            torrentFileName.put(oneObj.get("index").asInt(),oneObj.get("name").asText());
        }
        torrent.setTorrentFiles(torrentFileName);
        return torrent;
    }

    public ArrayList<Torrent> setAllTorrentsContent(ArrayList<Torrent> torrentList) {
        for (Torrent torrent : torrentList) {
            setTorrentContent(torrent);
        }
        return torrentList;
    }

    private Torrent getTorrentFromName(String torrentName) {
        Torrent searchingTorrent = null;
        ArrayList<Torrent> torrensList = getTorrentList();
        for (Torrent torrent : torrensList) {
            if (torrent.getName().equals(torrentName)) searchingTorrent = torrent;
        }
        return searchingTorrent;
    }

    public void renameIncorrectFilesInTorrent(String torrentName) {
        Torrent torrent = getTorrentFromName(torrentName);
        String path = torrent.getPath();
        String hash = torrent.getHash();

        path = path.substring(path.lastIndexOf("\\") + 1);

        Map<String, String> postBody = new HashMap<>();
        for (Map.Entry<Integer, String> torrentFile : torrent.getTorrentFiles().entrySet()) {
            postBody.clear();
            postBody.put("hash", hash);
            postBody.put("oldPath", torrentFile.getValue());
            postBody.put("newPath", makeNewPath(path, torrentFile));
            getResponse(makeRequest("torrents/renameFile", postBody), getSessionCookie());
        }
    }

    private String makeNewPath(String path, Map.Entry<Integer, String> torrentFile) {
        return String.format("%s/%sE%02d%s", path, path, torrentFile.getKey()+1, torrentFile.getValue().substring(torrentFile.getValue().lastIndexOf(".")));
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
        return SessionCookie;
    }

    public void setSessionCookie(String sessionID) {
        SessionCookie = new HttpCookie("SID", sessionID);
        SessionCookie.setVersion(0);
        SessionCookie.setPath("/");
    }
}
