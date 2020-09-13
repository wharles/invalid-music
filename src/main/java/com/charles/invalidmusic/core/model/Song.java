package com.charles.invalidmusic.core.model;

import lombok.Data;

/**
 * Song
 *
 * @author charleswang
 * @since 2020/9/12 2:23 下午
 */
@Data
public class Song {
    public String id;
    public String name;
    public String albumName;
    public String artistName;
    public String albumUrl;
    public UrlInfo urlInfo;
}
