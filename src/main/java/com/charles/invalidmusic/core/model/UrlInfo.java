package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlInfo {
    @JsonAlias({"id", "hash", "songId"})
    private String id;

    @JsonAlias({"url", "play_url"})
    private String url;

    @JsonAlias({"br", "bitrate"})
    private Integer bitrate;

    @JsonAlias({"size", "filesize", "filesize"})
    private Long size;

    public UrlInfo() {
    }
}
