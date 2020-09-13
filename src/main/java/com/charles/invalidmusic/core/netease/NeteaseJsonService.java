package com.charles.invalidmusic.core.netease;

import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * NeteaseJsonService
 *
 * @author charleswang
 * @since 2020/9/12 1:56 下午
 */
@Component("NeteaseJsonService")
public final class NeteaseJsonService implements JsonBeanService {

    @Override
    public PageList<SearchItem> getSearchItemPageList(int limit, int page, JsonNode resultModel) {
        PageList<SearchItem> songs = new PageList<>();
        songs.setPage(page);
        songs.setLimit(limit);
        songs.setData(new ArrayList<>());
        if (resultModel != null) {
            songs.setTotal(resultModel.path("result").path("songCount").asInt());
            for (JsonNode rawSong : resultModel.path("result").path("songs")) {
                SearchItem searchItem = getSearchItem(rawSong);
                songs.getData().add(searchItem);
            }
        } else {
            songs.setTotal(0);
        }
        return songs;
    }

    private SearchItem getSearchItem(JsonNode rawSong) {
        SearchItem searchItem = new SearchItem();
        searchItem.setId(rawSong.path("id").asText());
        searchItem.setAlbumName(rawSong.path("album").path("name").asText());
        JsonNode artistsNode = rawSong.path("artists");
        List<String> list = new ArrayList<>();
        for (JsonNode artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        searchItem.setArtistName(String.join(",", list));
        searchItem.setName(rawSong.path("name").asText());
        searchItem.setSource("netease");
        return searchItem;
    }

    @Override
    public Song getSong(JsonNode songNode, UrlInfo urlInfo) {
        Song song = new Song();
        song.setId(songNode.path("id").asText());
        song.setName(songNode.path("name").asText());
        song.setAlbumName(songNode.path("al").path("name").asText());
        song.setAlbumUrl(songNode.path("al").path("picUrl").asText());
        song.setArtistName(getArtistName(songNode));
        song.setUrlInfo(urlInfo);
        return song;
    }

    @Override
    public Playlist getPlaylist(JsonNode resultModel) {
        JsonNode playlistNode = resultModel.path("playlist");
        Playlist playlist = new Playlist();
        playlist.setId(playlistNode.path("id").asText());
        playlist.setName(playlistNode.path("name").asText());
        playlist.setCoverImgUrl(playlistNode.path("coverImgUrl").asText());
        playlist.setDescription(playlistNode.path("description").asText());
        playlist.setSongs(new ArrayList<>());
        JsonNode tracksNode = playlistNode.path("tracks");
        for (JsonNode trackNode : tracksNode) {
            Song song = getSong(trackNode, null);
            playlist.getSongs().add(song);
        }
        return playlist;
    }

    private String getArtistName(JsonNode songNode) {
        JsonNode artistsNode = songNode.path("ar");
        List<String> list = new ArrayList<>();
        for (JsonNode artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        return String.join(",", list);
    }

    @Override
    public UrlInfo getUrlInfo(int bitrate, JsonNode resultModel) {
        JsonNode data = resultModel.path("data").get(0);
        UrlInfo urlInfo = new UrlInfo();
        urlInfo.setId(data.path("id").asText());
        urlInfo.setBitrate(data.path("br").asInt());
        urlInfo.setSize(data.path("size").asLong());
        urlInfo.setUrl(data.path("url").asText());

        return urlInfo;
    }

    @Override
    public Lyric getLyric(JsonNode resultModel) {
        Lyric lyric = new Lyric();
        lyric.setContent(resultModel.path("lrc").path("lyric").asText());
        lyric.setTranslate(resultModel.path("tlyric").path("lyric").asText());
        return lyric;
    }
}
