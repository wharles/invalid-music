package com.charles.invalidmusic.core;

import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * MusicApi
 */
public abstract class MusicApi {

    protected final ObjectMapper mapper = new ObjectMapper();

    public abstract Platform getPlatform();

    public abstract PageList<SearchItem> search(String keyword, int limit, int page, int type);

    public abstract Song getSongById(String songId, Quality quality);

    public abstract Playlist getPlaylistById(String playlistId);

    public abstract List<UrlInfo> getUrlById(Quality quality, String... songId);

    public abstract Lyric getLyricById(String songId);

    protected PageList<SearchItem> getSearchItemPageList(int limit, int page, JsonNode dataNode, String totalPath, String songPath)
            throws JsonProcessingException {
        var songs = getSearchItemPageList(limit, page);
        if (dataNode != null) {
            songs.setTotal(dataNode.at(totalPath).asInt());
            for (var songNode : dataNode.at(songPath)) {
                var searchItem = mapper.treeToValue(songNode, SearchItem.class);
                searchItem.setSource(getPlatform().getValue());
                songs.getData().add(searchItem);
            }
        }
        return songs;
    }

    protected PageList<SearchItem> getSearchItemPageList(int limit, int page) {
        var songs = new PageList<SearchItem>();
        songs.setPage(page);
        songs.setLimit(limit);
        songs.setData(new ArrayList<>());
        return songs;
    }
}
