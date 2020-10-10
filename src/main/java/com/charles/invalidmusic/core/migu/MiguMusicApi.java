package com.charles.invalidmusic.core.migu;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Platform;
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

/**
 * MiguMusicApi
 *
 * @author charleswang
 * @since 2020/10/10 9:42 下午
 */
@Component
public class MiguMusicApi extends MusicApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiguMusicApi.class);

    private final HttpClientService httpClientService;

    @Autowired
    public MiguMusicApi(@Qualifier("MiguClientService") HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public Platform getPlatform() {
        return Platform.MIGU;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        String url = "http://m.music.migu.cn/migu/remoting/scr_search_tag";
        var params = Map.of(
                "type", "2",
                "keyword", keyword,
                "rows", String.valueOf(limit),
                "pgc", String.valueOf(page)
        );
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                return getSearchItemPageList(limit, page, resultModel, "/pgt", "/musics");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("search music failed, reason:", e);
        }
        return getSearchItemPageList(limit, page);
    }

    @Override
    public Song getSongById(String songId) {
        var url = "http://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/listen-url";
        var params = Map.of("songId", songId,
                "netType", "01",
                "resourceType", "E",
                "toneFlag", "SQ",
                "dataType", "2");
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                UrlInfo urlInfo = mapper.treeToValue(resultModel.at("/data"), UrlInfo.class);
                urlInfo.setId(songId);
                urlInfo.setBitrate(999000);

                Song song = mapper.treeToValue(resultModel.at("/data/songItem"), Song.class);
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
        var url = "http://m.music.migu.cn/migu/remoting/playlist_query_tag";
        var params = Map.of("playListId", playlistId,
                "onLine", "1",
                "queryChannel", "0",
                "createUserId", "migu",
                "contentCountMin", "5");
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                var playlist = mapper.treeToValue(resultModel.at("/playlist/0"), Playlist.class);
                var contentCount = resultModel.at("/playlist/0/contentCount").asText();
                var songs = getPlaylistContent(playlistId, contentCount);
                playlist.setSongs(songs);
                return playlist;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get playlist by id failed, reason:", e);
        }
        return null;
    }

    private List<Song> getPlaylistContent(String playlistId, String contentCount) {
        var url = "https://m.music.migu.cn/migu/remoting/playlistcontents_query_tag";
        var params = Map.of("playListId", playlistId,
                "playListType", "2",
                "contentCount", contentCount);
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                var contentListNode = resultModel.at("/contentList");
                var songs = new ArrayList<Song>();
                for (var contentNode : contentListNode) {
                    songs.add(mapper.treeToValue(contentNode, Song.class));
                }
                return songs;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get playlist content by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        var url = "http://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/listen-url";
        String toneFlag;
        if (bitrate == 12800) {
            toneFlag = "PQ";
        } else if (bitrate == 32000) {
            toneFlag = "HQ";
        } else {
            toneFlag = "SQ";
        }
        var params = Map.of("songId", songId[0],
                "netType", "01",
                "resourceType", "E",
                "toneFlag", toneFlag,
                "dataType", "2");
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                UrlInfo urlInfo = mapper.treeToValue(resultModel.at("/data"), UrlInfo.class);
                urlInfo.setId(songId[0]);
                urlInfo.setBitrate(bitrate);
                return urlInfo;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get song url by id failed, reason:", e);
        }
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        var url = "http://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/listen-url";
        var params = Map.of("songId", songId,
                "netType", "01",
                "resourceType", "E",
                "toneFlag", "SQ",
                "dataType", "2");
        try {
            var content = httpClientService.get(url, params);
            var resultModel = checkAndGetJson(content);
            if (resultModel != null) {
                String lycUrl = resultModel.at("/data/songItem/lrcUrl").asText();
                String s = httpClientService.get(lycUrl);
                Lyric lyric = new Lyric();
                lyric.setContent(s);
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
        if (resultModel.path("success").asBoolean() != Boolean.TRUE && !"000000".equals(resultModel.path("code").asText())) {
            return null;
        }
        return resultModel;
    }
}
