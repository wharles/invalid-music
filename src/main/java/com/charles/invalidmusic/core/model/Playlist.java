package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {
    @JsonAlias({"id", "disstid", "playListId", "listId"})
    private String id;

    @JsonAlias({"name", "dissname", "playListName", "collectName"})
    private String name;

    @JsonAlias({"description", "desc", "summary", "cleanDesc"})
    private String description;

    @JsonAlias({"coverImgUrl", "logo", "image", "collectLogo"})
    private String coverImgUrl;

    @JsonAlias({"tracks", "songlist", "songs"})
    private List<Song> songs;
}
