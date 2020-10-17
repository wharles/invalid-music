package com.charles.invalidmusic.core.netease;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Quality;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NeteaseMusicApi
 *
 * @author charleswang
 * @since 2020/08/30
 */
@Component
public class NeteaseMusicApi extends MusicApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeteaseMusicApi.class);

    private final HttpClientService httpClientService;

    @Autowired
    public NeteaseMusicApi(@Qualifier("NeteaseClientService") HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.NETEASE;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        var url = "https://music.163.com/weapi/v1/search/get?csrf_token=";
        var rootNode = mapper.createObjectNode();
        rootNode.put("s", keyword)
                .put("type", type)
                .put("limit", limit)
                .put("total", "true")
                .put("offset", (page - 1) * limit)
                .put("csrf_token", "");
        try {
            var resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return getSearchItemPageList(limit, page, resultModel, "/result/songCount", "/result/songs");
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return getSearchItemPageList(limit, page);
    }

    @Override
    public Song getSongById(String songId, Quality quality) {
        var url = "https://music.163.com/weapi/v3/song/detail?csrf_token=";
        var rootNode = mapper.createObjectNode();
        rootNode.put("c", "[{\"id\":" + songId + "}]").put("csrf_token", "");
        try {
            var resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                var urlInfo = getUrlById(quality, songId).get(0);
                var song = mapper.treeToValue(resultModel.at("/songs/0"), Song.class);
                song.setUrlInfo(urlInfo);
                return song;
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get music by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        var url = "https://music.163.com/weapi/v3/playlist/detail?csrf_token=";
        var rootNode = mapper.createObjectNode();
        rootNode.put("id", playlistId)
                .put("n", 1000)
                .put("csrf_token", "");
        try {
            var resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return mapper.treeToValue(resultModel.path("playlist"), Playlist.class);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get playlist by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public List<UrlInfo> getUrlById(Quality quality, String... songIds) {
        try {
            var url = "https://music.163.com/weapi/song/enhance/player/url?csrf_token=";
            var rootNode = mapper.createObjectNode();
            rootNode.put("ids", Arrays.toString(songIds))
                    .put("br", quality.getBitrate())
                    .put("csrf_token", "");
            var resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                var urlInfos = new ArrayList<UrlInfo>();
                for (var i = 0; i < songIds.length; i++) {
                    urlInfos.add(mapper.treeToValue(resultModel.at("/data/" + i), UrlInfo.class));
                }
                return urlInfos;
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get url by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        try {
            var url = "https://music.163.com/weapi/song/lyric?csrf_token=";
            var rootNode = mapper.createObjectNode();
            rootNode.put("id", songId)
                    .put("os", "pc")
                    .put("lv", -1)
                    .put("kv", -1)
                    .put("tv", -1)
                    .put("csrf_token", "");
            var resultModel = requestJson(url, rootNode);
            if (resultModel != null) {
                return mapper.treeToValue(resultModel, Lyric.class);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            LOGGER.error("get lyric by id failed, reason:", e);
        }
        return null;
    }

    private JsonNode requestJson(String url, ObjectNode objectNode) throws IOException, GeneralSecurityException, InterruptedException {
        var params = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        var content = httpClientService.postForm(url, params);
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        var resultModel = mapper.readTree(content);
        if (resultModel == null || resultModel.path("code").asInt() != 200) {
            return null;
        }
        return resultModel;
    }
}
