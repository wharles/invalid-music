package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {
    @JsonAlias({"id", "disstid"})
    private String id;

    @JsonAlias({"name", "dissname"})
    private String name;

    @JsonAlias({"description", "desc"})
    private String description;

    @JsonAlias({"coverImgUrl", "logo"})
    private String coverImgUrl;

    @JsonAlias({"tracks", "songlist"})
    private List<Song> songs;
}
