package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchItem {
    @JsonAlias({"id", "mid", "hash", "songId"})
    public String id;

    @JsonAlias({"name", "songname", "songName"})
    public String name;

    @JsonAlias({"album_name", "albumName"})
    public String albumName;

    @JsonAlias({"singername", "singerName", "artistName"})
    public String artistName;

    public String source;

    @JsonProperty("album")
    public void unpackAlbumNameFromNestedAlbum(JsonNode albumNode) {
        this.albumName = albumNode.path("name").asText();
    }

    @JsonProperty("artists")
    public void unpackAlbumNameFromNestedArtists(JsonNode artistsNode) {
        var list = new ArrayList<String>();
        for (var artistNode : artistsNode) {
            list.add(artistNode.path("name").asText());
        }
        this.artistName = String.join(",", list);
    }

    @JsonProperty("singer")
    public void unpackAlbumNameFromNestedSinger(JsonNode singerNode) {
        var list = new ArrayList<String>();
        for (var artistNode : singerNode) {
            list.add(artistNode.path("name").asText());
        }
        this.artistName = String.join(",", list);
    }
}
