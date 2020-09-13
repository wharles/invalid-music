package com.charles.invalidmusic.core.baidu;

import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.model.*;
import org.springframework.stereotype.Component;

@Component
public class BaiduMusicApi implements MusicApi {
    @Override
    public Platform getPlatform() {
        return Platform.BAIDU;
    }

    @Override
    public PageList<SearchItem> search(String keyword, int limit, int page, int type) {
        return null;
    }

    @Override
    public Song getSongById(String songId) {
        return null;
    }

    @Override
    public Playlist getPlaylistById(String playlistId) {
        return null;
    }

    @Override
    public UrlInfo getUrlById(int bitrate, String... songId) {
        return null;
    }

    @Override
    public Lyric getLyricById(String songId) {
        return null;
    }

}
