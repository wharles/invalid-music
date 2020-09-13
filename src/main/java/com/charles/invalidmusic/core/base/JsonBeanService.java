package com.charles.invalidmusic.core.base;

import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * JsonBeanService
 *
 * @author charleswang
 * @since 2020/9/13 7:07 下午
 */
public interface JsonBeanService {
    PageList<SearchItem> getSearchItemPageList(int limit, int page, JsonNode dataNode);

    Song getSong(JsonNode songNode, UrlInfo urlInfo);

    Playlist getPlaylist(JsonNode resultModel);

    UrlInfo getUrlInfo(int bitrate, JsonNode dataNode);

    Lyric getLyric(JsonNode resultModel);
}
