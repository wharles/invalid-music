package com.charles.invalidmusic.core.netease;

import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.model.*;
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
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * NeteaseMusicApi
 *
 * @author charleswang
 * @since 2020/08/30
 */
@Component
public class NeteaseMusicApi implements MusicApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeteaseMusicApi.class);

    private final HttpClientService httpClientService;

    private final JsonBeanService jsonBeanService;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public NeteaseMusicApi(@Qualifier("NeteaseClientService") HttpClientService httpClientService,
                           @Qualifier("NeteaseJsonService") JsonBeanService jsonBeanService) {
        this.httpClientService = httpClientService;
        this.jsonBeanService = jsonBeanService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.NETEASE;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        String url = "https://music.163.com/weapi/v1/search/get?csrf_token=";
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("s", keyword)
                .put("type", type)
                .put("limit", limit)
                .put("total", "true")
                .put("offset", (page - 1) * limit)
                .put("csrf_token", "");
        try {
            JsonNode resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return jsonBeanService.getSearchItemPageList(limit, page, resultModel);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return jsonBeanService.getSearchItemPageList(limit, page, null);
    }

    @Override
    public Song getSongById(String songId) {
        String url = "https://music.163.com/weapi/v3/song/detail?csrf_token=";
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("c", "[{\"id\":" + songId + "}]").put("csrf_token", "");
        try {
            JsonNode resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                UrlInfo urlInfo = getUrlById(999000, songId);
                return jsonBeanService.getSong(resultModel.path("songs").get(0), urlInfo);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get music by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        String url = "https://music.163.com/weapi/v3/playlist/detail?csrf_token=";
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("id", playlistId)
                .put("n", 1000)
                .put("csrf_token", "");
        try {
            JsonNode resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return jsonBeanService.getPlaylist(resultModel);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get playlist by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        try {
            String url = "https://music.163.com/weapi/song/enhance/player/url?csrf_token=";
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("ids", Arrays.toString(songId))
                    .put("br", bitrate)
                    .put("csrf_token", "");
            JsonNode resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return jsonBeanService.getUrlInfo(bitrate, resultModel);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get url by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        try {
            String url = "https://music.163.com/weapi/song/lyric?csrf_token=";
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("id", songId)
                    .put("os", "pc")
                    .put("lv", -1)
                    .put("kv", -1)
                    .put("tv", -1)
                    .put("csrf_token", "");
            JsonNode resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return jsonBeanService.getLyric(resultModel);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get lyric by id failed, reason:", e);
        }
        return null;
    }

    private JsonNode requestJson(String url, ObjectNode objectNode) throws IOException, GeneralSecurityException, InterruptedException {
        String params = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        String content = httpClientService.post(url, params);
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        JsonNode resultModel = mapper.readTree(content);
        if (resultModel == null || resultModel.path("code").asInt() != 200) {
            return null;
        }
        return resultModel;
    }
}
