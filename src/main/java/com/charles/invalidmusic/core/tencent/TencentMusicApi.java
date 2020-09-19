package com.charles.invalidmusic.core.tencent;

import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Component
public class TencentMusicApi implements MusicApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(TencentMusicApi.class);

    private final HttpClientService httpClientService;

    private final JsonBeanService jsonBeanService;

    @Autowired
    public TencentMusicApi(@Qualifier("TencentClientService") HttpClientService httpClientService,
                           @Qualifier("TencentJsonService") JsonBeanService jsonBeanService) {
        this.httpClientService = httpClientService;
        this.jsonBeanService = jsonBeanService;
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
        String url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp";
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
            String content = httpClientService.get(url, params);
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getSearchItemPageList(limit, page, resultModel.path("data").path("song"));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return jsonBeanService.getSearchItemPageList(limit, page, null);
    }

    @Override
    public Song getSongById(String songId) {
        String url = "https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg";
        var params = Map.of(
                "songmid", String.valueOf(songId),
                "platform", "yqq",
                "format", "json"
        );
        try {
            String content = httpClientService.get(url, params);
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                JsonNode songNode = resultModel.path("data").get(0);
                UrlInfo urlInfo = getUrlById(999000, songId);
                setFileSize(urlInfo, songNode);
                return jsonBeanService.getSong(songNode, urlInfo);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        String url = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_playlist_cp.fcg";
        var params = Map.of(
                "id", playlistId,
                "platform", "jqspaframe.json",
                "format", "json",
                "newsong", "1"
        );
        try {
            String content = httpClientService.get(url, params);
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getPlaylist(resultModel.path("data").path("cdlist").get(0));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        try {
            String url = "https://u.y.qq.com/cgi-bin/musicu.fcg";
            String dataJson = getDataJson(songId);

            var params = Map.of(
                    "platform", "yqq.json",
                    "format", "json",
                    "needNewCode", "0",
                    "data", dataJson
            );
            String content = httpClientService.get(url, params);
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                JsonNode dataNode = resultModel.path("req_0").path("data");
                return jsonBeanService.getUrlInfo(bitrate, dataNode);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get url by song id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        String url = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg";
        var params = Map.of(
                "songmid", String.valueOf(songId),
                "g_tk", "5381",
                "format", "json"
        );
        try {
            String content = httpClientService.get(url, params);
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getLyric(resultModel);
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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultModel = mapper.readTree(json);
        if (resultModel == null || resultModel.path("code").asInt() != 0) {
            return null;
        }
        return resultModel;
    }

    private String getDataJson(String[] songId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode paramNode = mapper.createObjectNode();
        paramNode.put("guid", "358840384")
                .put("uin", "1443481947")
                .put("loginflag", 1)
                .put("platform", "20");

        for (String mid : songId) {
            paramNode.set("songmid", mapper.createArrayNode().add(mid));
            paramNode.set("songtype", mapper.createArrayNode().add(0));
        }

        ObjectNode reqNode = mapper.createObjectNode();
        reqNode.put("module", "vkey.GetVkeyServer")
                .put("method", "CgiGetVkey")
                .set("param", paramNode);
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("req_0", reqNode);

        ObjectNode commonNode = mapper.createObjectNode();
        commonNode.put("uin", "1443481947");
        commonNode.put("format", "json");
        commonNode.put("ct", 24);
        commonNode.put("cv", 0);
        rootNode.set("common", commonNode);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    private void setFileSize(UrlInfo urlInfo, JsonNode songNode) {
        String bitStr = urlInfo.getUrl().substring(urlInfo.getUrl().lastIndexOf('/') + 1, urlInfo.getUrl().lastIndexOf('/') + 5);
        String sizePath = bitMap.get(bitStr);
        urlInfo.setSize(songNode.path("file").path(sizePath).asLong());
        urlInfo.setBitrate(Integer.parseInt(sizePath.substring(sizePath.indexOf('_') + 1, sizePath.length() - 3)));
    }
}
