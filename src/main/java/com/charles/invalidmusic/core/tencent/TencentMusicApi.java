package com.charles.invalidmusic.core.tencent;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.Quality;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Component
public class TencentMusicApi extends MusicApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(TencentMusicApi.class);

    private final HttpClientService httpClientService;

    @Autowired
    public TencentMusicApi(@Qualifier("TencentClientService") HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    private static final Map<String, String> bitMap = Map.of(
            "M800", "size_320mp3",
            "C600", "size_192aac",
            "M500", "size_128mp3",
            "C400", "size_96aac",
            "C200", "size_48aac",
            "C100", "size_24aac"
    );

    @Override
    public Platform getPlatform() {
        return Platform.TENCENT;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        var url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp";
        var params = Map.of(
                "format", "json",
                "type", String.valueOf(type),
                "n", String.valueOf(limit),
                "w", keyword,
                "p", String.valueOf(page),
                "new_json", "1",
                "cr", "1",
                "lossless", "1",
                "aggr", "1"
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return getSearchItemPageList(limit, page, resultModel, "/data/song/totalnum", "/data/song/list");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return getSearchItemPageList(limit, page);
    }

    @Override
    public Song getSongById(String songId, Quality quality) {
        var url = "https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg";
        var params = Map.of(
                "songmid", String.valueOf(songId),
                "platform", "yqq",
                "format", "json"
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                var songNode = resultModel.path("data").get(0);
                var urlInfo = getUrlById(quality, songId).get(0);
                setFileSize(urlInfo, songNode);
                var song = mapper.treeToValue(songNode, Song.class);
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
        var url = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_playlist_cp.fcg";
        var params = Map.of(
                "id", playlistId,
                "platform", "jqspaframe.json",
                "format", "json",
                "newsong", "1"
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return mapper.treeToValue(resultModel.at("/data/cdlist/0"), Playlist.class);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public List<UrlInfo> getUrlById(Quality quality, String... songIds) {
        try {
            var url = "https://u.y.qq.com/cgi-bin/musicu.fcg";
            var dataJson = getDataJson(quality, songIds);

            var params = Map.of(
                    "platform", "yqq.json",
                    "format", "json",
                    "needNewCode", "0",
                    "data", dataJson
            );
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return getUrlInfos(quality.getBitrate(), resultModel.at("/req_0/data"));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get url by song id failed, reason:", e);
        }
        return null;
    }

    public List<UrlInfo> getUrlInfos(int bitrate, JsonNode dataNode) {
        var infoNodes = dataNode.path("midurlinfo");
        var urlInfos = new ArrayList<UrlInfo>();
        for (var infoNode : infoNodes) {
            var sip = dataNode.path("sip").get(0).asText();
            var purl = infoNode.path("purl").asText();
            var fileName = infoNode.path("filename").asText();

            var urlInfo = new UrlInfo();
            urlInfo.setUrl(sip + purl);
            urlInfo.setId(infoNode.path("songmid").asText());
            urlInfo.setBitrate(bitrate);
            urlInfo.setFormat(fileName.substring(fileName.lastIndexOf('.') + 1));

            urlInfos.add(urlInfo);
        }
        return urlInfos;
    }

    @Override
    public Lyric getLyricById(String songId) {
        var url = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg";
        var params = Map.of(
                "songmid", String.valueOf(songId),
                "g_tk", "5381",
                "format", "json"
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return mapper.treeToValue(resultModel, Lyric.class);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get lyric by song id failed, reason:", e);
        }
        return null;
    }

    private JsonNode checkAndGetJson(String json) throws IOException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        var resultModel = mapper.readTree(json);
        if (resultModel == null || resultModel.path("code").asInt() != 0) {
            return null;
        }
        return resultModel;
    }

    private String getDataJson(Quality quality, String[] songIds) throws JsonProcessingException {
        var ts = String.valueOf(System.currentTimeMillis() / 1000);
        var bit = quality.getBitrate() / 1000;
        var fileNames = new ArrayList<String>();
        for (var mid : songIds) {
            for (var entry : bitMap.entrySet()) {
                if (entry.getValue().contains(String.valueOf(bit))) {
                    var v = entry.getValue().substring(entry.getValue().length() - 3);
                    v = "aac".equals(v) ? "m4a" : v;
                    var fileName = entry.getKey() + mid + "." + v;
                    fileNames.add(fileName);
                    break;
                }
            }
        }
        fileNames.sort(Collections.reverseOrder());

        var rootMap = Map.of(
                "req_0", Map.of(
                        "module", "vkey.GetVkeyServer",
                        "method", "CgiGetVkey",
                        "param", Map.of(
                                "guid", String.valueOf(new Random().nextInt(Integer.MAX_VALUE)),
                                "uin", ts,
                                "loginflag", 1,
                                "platform", "20",
                                "songmid", songIds,
                                "filename", fileNames,
                                "songtype", new int[]{0})),
                "common", Map.of(
                        "uin", ts,
                        "format", "json",
                        "ct", bit,
                        "cv", 0));

        return mapper.writeValueAsString(rootMap);
    }

    private void setFileSize(UrlInfo urlInfo, JsonNode songNode) {
        if (urlInfo.getUrl() != null && urlInfo.getUrl().lastIndexOf('/') != urlInfo.getUrl().length() - 1) {
            var bitStr = urlInfo.getUrl().substring(urlInfo.getUrl().lastIndexOf('/') + 1, urlInfo.getUrl().lastIndexOf('/') + 5);
            var sizePath = bitMap.get(bitStr);
            urlInfo.setSize(songNode.path("file").path(sizePath).asLong());
            urlInfo.setBitrate(Integer.parseInt(sizePath.substring(sizePath.indexOf('_') + 1, sizePath.length() - 3)) * 1000);
        }
    }
}
