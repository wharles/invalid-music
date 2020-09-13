package com.charles.invalidmusic.core.tencent;

import com.charles.invalidmusic.core.base.JsonBeanService;
import com.charles.invalidmusic.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * TencentJsonService
 *
 * @author charleswang
 * @since 2020/9/12 7:06 下午
 */
@Component("TencentJsonService")
public class TencentJsonService implements JsonBeanService {

    @Override
    public PageList<SearchItem> getSearchItemPageList(int limit, int page, JsonNode dataNode) {
        PageList<SearchItem> songs = new PageList<>();
        songs.setPage(page);
        songs.setLimit(limit);
        songs.setData(new ArrayList<>());
        if (dataNode != null) {
            songs.setTotal(dataNode.path("totalnum").asInt());
            JsonNode songsNode = dataNode.path("list");
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
        searchItem.setId(songNode.path("mid").asText());
        searchItem.setAlbumName(songNode.path("album").path("name").asText());
        JsonNode artistsNode = songNode.path("singer");
        List<String> list = new ArrayList<>();
        for (JsonNode artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        searchItem.setArtistName(String.join(",", list));
        searchItem.setName(songNode.path("name").asText());
        searchItem.setSource("tencent");
        return searchItem;
    }

    @Override
    public Playlist getPlaylist(JsonNode playlistNode) {
        Playlist playlist = new Playlist();
        playlist.setId(playlistNode.path("disstid").asText());
        playlist.setName(playlistNode.path("dissname").asText());
        playlist.setCoverImgUrl(playlistNode.path("logo").asText());
        playlist.setDescription(playlistNode.path("desc").asText());
        playlist.setSongs(new ArrayList<>());
        JsonNode tracksNode = playlistNode.path("songlist");
        for (JsonNode trackNode : tracksNode) {
            Song song = getSong(trackNode, null);
            playlist.getSongs().add(song);
        }
        return playlist;
    }

    @Override
    public Song getSong(JsonNode songNode, UrlInfo urlInfo) {
        Song song = new Song();
        song.setId(songNode.path("mid").asText());
        song.setName(songNode.path("name").asText());
        song.setAlbumName(songNode.path("album").path("name").asText());
        song.setArtistName(getArtistName(songNode));
        song.setUrlInfo(urlInfo);

        String albumId = songNode.path("album").path("mid").asText();
        String albumUrl = "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + albumId + ".jpg?max_age=2592000";
        song.setAlbumUrl(albumUrl);
        return song;
    }

    private String getArtistName(JsonNode songNode) {
        JsonNode artistsNode = songNode.path("singer");
        List<String> list = new ArrayList<>();
        for (JsonNode artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        return String.join(",", list);
    }

    @Override
    public UrlInfo getUrlInfo(int bitrate, JsonNode dataNode) {
        JsonNode infoNode = dataNode.path("midurlinfo").get(0);
        String sip = dataNode.path("sip").get(0).asText();
        String purl = infoNode.path("purl").asText();
        UrlInfo urlInfo = new UrlInfo();
        urlInfo.setUrl(sip + purl);
        urlInfo.setId(infoNode.path("songmid").asText());
        urlInfo.setBitrate(bitrate);
        return urlInfo;
    }

    @Override
    public Lyric getLyric(JsonNode resultModel) {
        Lyric lyric = new Lyric();
        String lrc = resultModel.path("lyric").asText();
        String trans = resultModel.path("trans").asText();
        lyric.setContent(new String(Base64.getDecoder().decode(lrc.getBytes())));
        if (!StringUtils.isEmpty(trans)) {
            lyric.setTranslate(new String(Base64.getDecoder().decode(trans.getBytes())));
        }
        return lyric;
    }
}
