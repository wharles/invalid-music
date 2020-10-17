package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;

/**
 * Song
 *
 * @author charleswang
 * @since 2020/9/12 2:23 下午
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Song {
    @JsonAlias({"id", "mid", "hash", "songId"})
    public String id;

    @JsonAlias({"name", "song_name", "songName", "contentName"})
    public String name;

    @JsonAlias({"album_name"})
    public String albumName;

    @JsonAlias({"author_name", "singerName", "artistName"})
    public String artistName;

    @JsonAlias({"img", "albumLogo"})
    public String albumUrl;

    public UrlInfo urlInfo;

    @JsonProperty("al")
    public void unpackAlFromNestedAl(JsonNode albumNode) {
        this.albumName = albumNode.path("name").asText();
        this.albumUrl = albumNode.path("picUrl").asText();
    }

    @JsonProperty("album")
    public void unpackAlbumFromNestedAlbum(JsonNode albumNode) {
        if (!albumNode.isTextual()) {
            this.albumName = albumNode.path("name").asText();
            var albumId = albumNode.path("mid").asText();
            this.albumUrl = "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + albumId + ".jpg?max_age=2592000";
        } else {
            this.albumName = albumNode.asText();
        }
    }

    @JsonProperty("singer")
    public void unpackArtistNameFromNestedSinger(JsonNode singerNode) {
        if (!singerNode.isTextual()) {
            this.artistName = combineJsonArray(singerNode);
        }
    }

    @JsonProperty("ar")
    public void unpackArtistNameFromNestedAr(JsonNode artistsNode) {
        this.artistName = combineJsonArray(artistsNode);
    }

    @JsonProperty("artists")
    public void unpackArtistNameFromNestedArtists(JsonNode artistsNode) {
        this.artistName = combineJsonArray(artistsNode);
    }

    @JsonProperty("albumImgs")
    public void unpackImgFromNestedalbumImgs(JsonNode albumImgsNode) {
        for (JsonNode albumImgNode : albumImgsNode) {
            if ("02".equals(albumImgNode.path("imgSizeType").asText())) {
                this.albumUrl = albumImgNode.path("img").asText();
            }
        }
    }

    private String combineJsonArray(JsonNode jsonNode) {
        var list = new ArrayList<String>();
        for (var node : jsonNode) {
            list.add(node.path("name").asText());
        }
        return String.join(",", list);
    }
}
