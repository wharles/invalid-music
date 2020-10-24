package com.charles.invalidmusic.core.kugou;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.Quality;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KugouMusicApi extends MusicApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(KugouMusicApi.class);

    private final HttpClientService httpClientService;

    @Autowired
    public KugouMusicApi(@Qualifier("KugouClientService") HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.KUGOU;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        var url = "http://mobilecdn.kugou.com/api/v3/search/song";
        var params = Map.of(
                "api_ver", "1",
                "area_code", "1",
                "correct", "1",
                "keyword", keyword,
                "pagesize", String.valueOf(limit),
                "plat", "2",
                "tag", "1",
                "sver", "5",
                "showtype", "10",
                "page", String.valueOf(page)
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content, "errcode");
            if (resultModel != null) {
                return getSearchItemPageList(limit, page, resultModel, "/data/total", "/data/info");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return getSearchItemPageList(limit, page);
    }

    @Override
    public Song getSongById(String songId, Quality quality) {
        var url = "https://wwwapi.kugou.com/yy/index.php?r=play%2Fgetdata&hash=" + songId;
        try {
            var content = httpClientService.get(url);
            var resultModel = checkAndGetJson(content, "err_code");
            if (resultModel != null) {
                var urlInfo = mapper.treeToValue(resultModel.at("/data"), UrlInfo.class);
                urlInfo.setBitrate(urlInfo.getBitrate() * 1000);
                urlInfo.setFormat(urlInfo.getUrl().substring(urlInfo.getUrl().lastIndexOf('.') + 1));
                var song = mapper.treeToValue(resultModel.at("/data"), Song.class);
                song.setUrlInfo(urlInfo);
                return song;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        var url = "http://mobilecdn.kugou.com/api/v3/special/song";
        var params = Map.of("specialid", playlistId,
                "area_code", "1",
                "page", "1",
                "plat", "2",
                "pagesize", "-1",
                "version", "8990");
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content, "errcode");
            if (resultModel != null) {
                var playlist = new Playlist();
                playlist.setId(playlistId);
                var songs = new ArrayList<Song>();
                var infoNodes = resultModel.at("/data/info");
                for (var infoNode : infoNodes) {
                    var song = getSong(infoNode);
                    songs.add(song);
                }
                playlist.setSongs(songs);
                return playlist;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get playlist by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public List<UrlInfo> getUrlById(Quality quality, String... songIds) {
        var url = "https://wwwapi.kugou.com/yy/index.php";

        var paramsList = Stream.of(songIds).map(songId -> Map.of("r=play/getdata&hash", songId)).collect(Collectors.toList());
        var contents = httpClientService.getRequests(url, paramsList);
        return contents.stream().map(content -> {
            try {
                var resultModel = checkAndGetJson(content, "err_code");
                if (resultModel != null) {
                    var urlInfo = mapper.treeToValue(resultModel.at("/data"), UrlInfo.class);
                    urlInfo.setBitrate(urlInfo.getBitrate() * 1000);
                    urlInfo.setFormat(urlInfo.getUrl().substring(urlInfo.getUrl().lastIndexOf('.') + 1));
                    return urlInfo;
                }
            } catch (IOException e) {
                LOGGER.error("get song by id failed, reason:", e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    @Override
    public Lyric getLyricById(String songId) {
        var url = "https://wwwapi.kugou.com/yy/index.php?r=play%2Fgetdata&hash=" + songId;
        try {
            var content = httpClientService.get(url);
            var resultModel = checkAndGetJson(content, "err_code");
            if (resultModel != null) {
                var lyric = new Lyric();
                lyric.setContent(resultModel.at("/data/lyrics").asText());
                return lyric;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get lyric by id failed, reason:", e);
        }
        return null;
    }

    private JsonNode checkAndGetJson(String json, String errorPath) throws IOException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        var resultModel = mapper.readTree(json);
        if (resultModel == null || resultModel.path(errorPath).asInt() != 0) {
            return null;
        }
        return resultModel;
    }

    private Song getSong(JsonNode infoNode) {
        var song = new Song();
        song.setId(infoNode.path("hash").asText());
        var filename = infoNode.path("filename").asText();
        if (filename != null && filename.contains("-")) {
            song.setName(filename.split("-")[1].trim());
            song.setArtistName(filename.split("-")[0].trim());
        }
        return song;
    }
}
