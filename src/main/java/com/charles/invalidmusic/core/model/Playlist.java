package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {
    @JsonAlias({"id", "disstid", "playListId"})
    private String id;

    @JsonAlias({"name", "dissname", "playListName"})
    private String name;

    @JsonAlias({"description", "desc", "summary"})
    private String description;

    @JsonAlias({"coverImgUrl", "logo", "image"})
    private String coverImgUrl;

    @JsonAlias({"tracks", "songlist"})
    private List<Song> songs;
}
