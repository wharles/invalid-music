package com.charles.invalidmusic.core.model;

import lombok.Data;

@Data
public class UrlInfo {
    private String id;
    private String url;
    private Integer bitrate;
    private Long size;
}
