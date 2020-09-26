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
    @JsonAlias({"id", "mid"})
    public String id;

    @JsonAlias({"name"})
    public String name;

    public String albumName;

    public String artistName;

    public String albumUrl;

    public UrlInfo urlInfo;

    @JsonProperty("al")
    public void unpackAlFromNestedAl(JsonNode albumNode) {
        this.albumName = albumNode.path("name").asText();
        this.albumUrl = albumNode.path("picUrl").asText();
    }

    @JsonProperty("album")
    public void unpackAlbumFromNestedAlbum(JsonNode albumNode) {
        this.albumName = albumNode.path("name").asText();
        var albumId = albumNode.path("mid").asText();
        this.albumUrl = "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + albumId + ".jpg?max_age=2592000";
    }

    @JsonProperty("singer")
    public void unpackArtistNameFromNestedSinger(JsonNode singerNode) {
        var list = new ArrayList<String>();
        for (var artistNode : singerNode) {
            list.add(artistNode.path("name").asText());
        }
        this.artistName = String.join(",", list);
    }

    @JsonProperty("ar")
    public void unpackArtistNameFromNestedAr(JsonNode artistsNode) {
        var list = new ArrayList<String>();
        for (var artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        this.artistName = String.join(",", list);
    }
}
