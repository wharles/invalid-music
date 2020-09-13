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
import okhttp3.HttpUrl;
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

    private static final Map<String, String> bitMap = new HashMap<>() {{
        put("M800", "size_320mp3");
        put("C600", "size_192aac");
        put("M500", "size_128mp3");
        put("C400", "size_96aac");
        put("C200", "size_48aac");
        put("C100", "size_24aac");
    }};

    @Override
    public Platform getPlatform() {
        return Platform.TENCENT;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        String url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp";
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        httpBuilder.addQueryParameter("format", "json")
                .addQueryParameter("type", String.valueOf(type))
                .addQueryParameter("n", String.valueOf(limit))
                .addQueryParameter("w", keyword)
                .addQueryParameter("p", String.valueOf(page))
                .addQueryParameter("new_json", "1")
                .addQueryParameter("cr", "1")
                .addQueryParameter("lossless", "1")
                .addQueryParameter("aggr", "1");
        try {
            String content = httpClientService.request(httpBuilder.build());
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getSearchItemPageList(limit, page, resultModel.path("data").path("song"));
            }
        } catch (IOException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return jsonBeanService.getSearchItemPageList(limit, page, null);
    }

    @Override
    public Song getSongById(String songId) {
        String url = "https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg";
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        httpBuilder.addQueryParameter("songmid", String.valueOf(songId))
                .addQueryParameter("platform", "yqq")
                .addQueryParameter("format", "json");
        try {
            String content = httpClientService.request(httpBuilder.build());
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                JsonNode songNode = resultModel.path("data").get(0);
                UrlInfo urlInfo = getUrlById(999000, songId);
                setFileSize(urlInfo, songNode);
                return jsonBeanService.getSong(songNode, urlInfo);
            }
        } catch (IOException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        String url = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_playlist_cp.fcg";
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        httpBuilder.addQueryParameter("id", playlistId)
                .addQueryParameter("platform", "jqspaframe.json")
                .addQueryParameter("format", "json")
                .addQueryParameter("newsong", "1");
        try {
            String content = httpClientService.request(httpBuilder.build());
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getPlaylist(resultModel.path("data").path("cdlist").get(0));
            }
        } catch (IOException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        try {
            String url = "https://u.y.qq.com/cgi-bin/musicu.fcg";
            String dataJson = getDataJson(songId);

            HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            httpBuilder.addQueryParameter("platform", "yqq.json")
                    .addQueryParameter("format", "json")
                    .addQueryParameter("needNewCode", "0")
                    .addQueryParameter("data", dataJson);

            String content = httpClientService.request(httpBuilder.build());
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                JsonNode dataNode = resultModel.path("req_0").path("data");
                return jsonBeanService.getUrlInfo(bitrate, dataNode);
            }
        } catch (IOException e) {
            LOGGER.error("get url by song id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        String url = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg";
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        httpBuilder.addQueryParameter("songmid", String.valueOf(songId))
                .addQueryParameter("g_tk", "5381")
                .addQueryParameter("format", "json");
        try {
            String content = httpClientService.request(httpBuilder.build());
            JsonNode resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getLyric(resultModel);
            }
        } catch (IOException e) {
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
