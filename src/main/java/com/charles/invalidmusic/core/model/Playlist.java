package com.charles.invalidmusic.core.model;

import lombok.Data;

import java.util.List;

@Data
public class Playlist {
    private String id;
    private String name;
    private String description;
    private String coverImgUrl;
    private List<Song> songs;
}
