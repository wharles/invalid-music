package com.charles.invalidmusic.core.kugou;

import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * KugouJsonService
 *
 * @author charleswang
 * @since 2020/9/20 4:49 下午
 */
@Component("KugouJsonService")
public class KugouJsonService implements JsonBeanService {
    @Override
    public PageList<SearchItem> getSearchItemPageList(int limit, int page, JsonNode dataNode) {
        PageList<SearchItem> songs = new PageList<>();
        songs.setPage(page);
        songs.setLimit(limit);
        songs.setData(new ArrayList<>());
        if (dataNode != null) {
            songs.setTotal(dataNode.path("total").asInt());
            JsonNode songsNode = dataNode.path("info");
            for (JsonNode songNode : songsNode) {
                SearchItem searchItem = getSearchItem(songNode);
                songs.getData().add(searchItem);
            }
        } else {
            songs.setTotal(0);
        }
        return songs;
    }

    private SearchItem getSearchItem(JsonNode songNode) {
        SearchItem searchItem = new SearchItem();
        searchItem.setId(songNode.path("hash").asText());
        searchItem.setAlbumName(songNode.path("album_name").asText());
        searchItem.setArtistName(songNode.path("singername").asText());
        searchItem.setName(songNode.path("songname").asText());
        searchItem.setSource("kougou");
        return searchItem;
    }

    @Override
    public Song getSong(JsonNode songNode, UrlInfo urlInfo) {
        return null;
    }

    @Override
    public Playlist getPlaylist(JsonNode resultModel) {
        return null;
    }

    @Override
    public UrlInfo getUrlInfo(int bitrate, JsonNode dataNode) {
        return null;
    }

    @Override
    public Lyric getLyric(JsonNode resultModel) {
        return null;
    }
}
