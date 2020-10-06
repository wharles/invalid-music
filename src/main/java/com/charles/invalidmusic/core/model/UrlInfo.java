package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlInfo {
    @JsonAlias({"id", "hash"})
    private String id;

    @JsonAlias({"url", "play_url"})
    private String url;

    @JsonAlias({"br", "bitrate"})
    private Integer bitrate;

    @JsonAlias({"size", "filesize"})
    private Long size;

    public UrlInfo() {
    }

    public UrlInfo(int bitrate, JsonNode dataNode) {
        var infoNode = dataNode.path("midurlinfo").get(0);
        var sip = dataNode.path("sip").get(0).asText();
        var purl = infoNode.path("purl").asText();
        this.url = sip + purl;
        this.id = infoNode.path("songmid").asText();
        this.bitrate = bitrate;
    }
}
