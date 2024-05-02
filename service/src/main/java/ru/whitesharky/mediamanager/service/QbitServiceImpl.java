package ru.whitesharky.mediamanager.service;

import org.springframework.stereotype.Service;
import ru.whitesharky.mediamanager.adapter.api.QbitAPI;
import ru.whitesharky.mediamanager.domain.Torrent;

import java.net.http.HttpResponse;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.Thread.sleep;

@Service
public class QbitServiceImpl implements QbitService {

    private UserService userService;
    private QbitAPI qbitAPI;

    public QbitServiceImpl(UserService userService) {
        this.userService = userService;
    }

    public void initQbitAPI() {
        if (qbitAPI == null) qbitAPI = new QbitAPI(userService.findCurrentUserSettings());
    }

    public ArrayList<Torrent> getTorrentList(Map<String, String> parameters) {
        HttpResponse<String> response = qbitAPI.getTorrentList(parameters);
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
                    oneObj.get("added_on").asInt(),
                    oneObj.get("state").asText())
            );
        }
        return torrentList;
    }

    public Torrent setTorrentContent(Torrent torrent) {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> torrentFilesName = new TreeSet<>();
        HttpResponse<String> response = qbitAPI.getTorrentContent(torrent.getHash());
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

        int index = 1;
        for (String torrentFile : torrent.getTorrentFiles()) {
            if (isTorrentFileNameContainsPattern(torrentFile)) break;
            qbitAPI.renameTorrentFile(hash, torrentFile, makeNewPath(path, torrentFile, index++));
        }
    }

    private boolean isTorrentFileNameContainsPattern(String torrentFileName) {
        return torrentFileName.matches(".*[S,s]\\d+[E,e]\\d+.*");
    }

    public void renameIncorrectFilesInTorrentByName(String torrentName) {
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
        int index = 0;
        for (String torrentPath : torrents.keySet()) {
            addNewTorrent(torrentPath, media.get(index++), tmpLibPath);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    sleep(50);
                    Torrent torrent = getTorrentList(Map.of("category", "autoNotSet", "reverse", "true")).getFirst();
                    renameIncorrectFilesInTorrent(torrent);
                    qbitAPI.recheckTorrent(torrent.getHash());
                    qbitAPI.setCategory(torrent.getHash(), category);
                    qbitAPI.resume(torrent.getHash());
                    for (Torrent serchingTorrent: getTorrentList(
                            Map.of("filter", "completed", "category", category, "sort", "name"))) {
                        if (serchingTorrent.getName().equals(torrent.getName())
                                && !serchingTorrent.getState().equals("checkingResumeData")) {
                            qbitAPI.delete(serchingTorrent.getHash(), false);
                            break;
                        }
                    }
                    break;
                } catch (NoSuchElementException | InterruptedException e) {
                    if (++count == maxTries) throw new RuntimeException(e);
                }
            }
        }
    }

    private void addNewTorrent(String torrentPath, String media, String tmpLibPath) {
        String[] mediaNameAndMeta = media.split("\\[");
        String mediaName = mediaNameAndMeta[0].trim().replaceAll("[|/\\\\<>!:\"*]", "");
        String mediaMeta = mediaNameAndMeta[1].replaceAll("]", "");
        String savePath = String.format("%s%s/%s %s", tmpLibPath, mediaName, mediaName, mediaMeta);
        String torrentName = String.format("%s %s", mediaName, mediaMeta);
        qbitAPI.addTorrent(torrentPath, savePath, torrentName, "autoNotSet");
    }
}