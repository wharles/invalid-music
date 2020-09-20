package com.charles.invalidmusic.core.kugou;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

@Component
public class KugouMusicApi implements MusicApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(KugouMusicApi.class);

    private final HttpClientService httpClientService;

    private final JsonBeanService jsonBeanService;

    @Autowired
    public KugouMusicApi(@Qualifier("KugouClientService") HttpClientService httpClientService,
                           @Qualifier("KugouJsonService") JsonBeanService jsonBeanService) {
        this.httpClientService = httpClientService;
        this.jsonBeanService = jsonBeanService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.KUGOU;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        String url = "http://mobilecdn.kugou.com/api/v3/search/song";
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
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return jsonBeanService.getSearchItemPageList(limit, page, resultModel.path("data"));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return jsonBeanService.getSearchItemPageList(limit, page, null);
    }

    @Override
    public Song getSongById(String songId) {
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        return null;
    }

    private JsonNode checkAndGetJson(String json) throws IOException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        var mapper = new ObjectMapper();
        var resultModel = mapper.readTree(json);
        if (resultModel == null || resultModel.path("errcode").asInt() != 0) {
            return null;
        }
        return resultModel;
    }
}
