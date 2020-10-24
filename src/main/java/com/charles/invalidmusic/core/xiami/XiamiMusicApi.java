package com.charles.invalidmusic.core.xiami;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class XiamiMusicApi extends MusicApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(XiamiMusicApi.class);

    private final HttpClientService httpClientService;

    @Autowired
    public XiamiMusicApi(@Qualifier("XiamiClientService") HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.XIAMI;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        String url = "https://acs.m.xiami.com/h5/mtop.alimusic.search.searchservice.searchsongs/1.0/";
        var model = Map.of(
                "header", Map.of("platformId", "mac"),
                "model", Map.of(
                        "key", keyword,
                        "pagingVO", Map.of(
                                "pageSize", String.valueOf(limit),
                                "page", String.valueOf(page)),
                        "r", "mtop.alimusic.search.searchservice.searchsongs"));
        try {
            var params = Map.of("requestStr", mapper.writeValueAsString(model));
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return getSearchItemPageList(limit, page, resultModel, "/data/data/pagingVO/count", "/data/data/songs");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return getSearchItemPageList(limit, page);
    }

    @Override
    public Song getSongById(String songId, Quality quality) {
        var url = "https://acs.m.xiami.com/h5/mtop.alimusic.music.songservice.getsongdetail/1.0/";
        var model = Map.of(
                "header", Map.of("platformId", "mac"),
                "model", Map.of(
                        "songId", songId,
                        "r", "mtop.alimusic.music.songservice.getsongdetail"));
        try {
            var params = Map.of("requestStr", mapper.writeValueAsString(model));
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                var listenFilesNode = resultModel.at("/data/data/songDetail/listenFiles");
                var listenFileNode = getListenFileNode(quality, listenFilesNode);
                if (listenFileNode == null) {
                    LOGGER.error("listen files is empty.");
                    return null;
                }
                var urlInfo = mapper.treeToValue(listenFileNode, UrlInfo.class);
                urlInfo.setId(songId);
                urlInfo.setBitrate(quality.getBitrate());

                var song = mapper.treeToValue(resultModel.at("/data/data/songDetail"), Song.class);
                song.setUrlInfo(urlInfo);
                return song;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song by id failed, reason:", e);
        }
        return null;
    }

    private JsonNode getListenFileNode(Quality quality, JsonNode listenFilesNode) {
        if (listenFilesNode != null && listenFilesNode.size() > 0) {
            return StreamSupport.stream(listenFilesNode.spliterator(), false).filter(file -> switch (quality) {
                case SQ -> "s".equals(file.path("quality").asText());
                case HQ -> "h".equals(file.path("quality").asText());
                case PQ -> "l".equals(file.path("quality").asText());
            }).findAny().orElse(listenFilesNode.get(0));
        }
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        var url = "https://acs.m.xiami.com/h5/mtop.alimusic.music.list.collectservice.getcollectdetail/1.0/";
        var model = Map.of(
                "header", Map.of("platformId", "mac"),
                "model", Map.of(
                        "listId", playlistId,
                        "isFullTags", false,
                        "pagingVO", Map.of(
                                "pageSize", 1000,
                                "page", 1),
                        "r", "mtop.alimusic.music.list.collectservice.getcollectdetail"));
        try {
            var params = Map.of("requestStr", mapper.writeValueAsString(model));
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return mapper.treeToValue(resultModel.at("/data/data/collectDetail"), Playlist.class);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get playlist by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public List<UrlInfo> getUrlById(Quality quality, String... songIds) {
        var url = "https://acs.m.xiami.com/h5/mtop.alimusic.music.songservice.getsongdetail/1.0/";

        var params = Stream.of(songIds).map(songId -> {
            try {
                return Map.of("requestStr", mapper.writeValueAsString(Map.of(
                        "header", Map.of("platformId", "mac"),
                        "model", Map.of(
                                "songId", songId,
                                "r", "mtop.alimusic.music.songservice.getsongdetail"))));
            } catch (JsonProcessingException e) {
                return new HashMap<String, String>();
            }
        }).collect(Collectors.toList());
        var contents = httpClientService.getRequests(url, params);
        return contents.stream().map(content -> {
            try {
                var resultModel = checkAndGetJson(content);
                if (resultModel != null) {
                    var listenFilesNode = resultModel.at("/data/data/songDetail/listenFiles");
                    var listenFileNode = getListenFileNode(quality, listenFilesNode);
                    if (listenFileNode == null) {
                        LOGGER.error("listen files is empty.");
                        return null;
                    }
                    var urlInfo = mapper.treeToValue(listenFileNode, UrlInfo.class);
                    urlInfo.setId(resultModel.at("/data/data/songDetail/songId").asText());
                    urlInfo.setBitrate(quality.getBitrate());
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
        var url = "https://acs.m.xiami.com/h5/mtop.alimusic.music.lyricservice.getsonglyrics/1.0/";
        var model = Map.of(
                "header", Map.of("platformId", "mac"),
                "model", Map.of(
                        "songId", songId,
                        "r", "mtop.alimusic.music.lyricservice.getsonglyrics"));
        try {
            var params = Map.of("requestStr", mapper.writeValueAsString(model));
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                var lyrics = resultModel.at("/data/data/lyrics");
                var lyric = new Lyric();
                for (var ly : lyrics) {
                    if (ly.path("type").asInt() == 2) {
                        lyric.setContent(ly.path("content").asText());
                        break;
                    }
                }
                return lyric;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get lyric by id failed, reason:", e);
        }
        return null;
    }

    private JsonNode checkAndGetJson(String json) throws IOException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        var resultModel = mapper.readTree(json);
        if (resultModel == null) {
            return null;
        }
        if (!resultModel.has("data") || !resultModel.path("data").has("data")) {
            return null;
        }
        return resultModel;
    }
}
