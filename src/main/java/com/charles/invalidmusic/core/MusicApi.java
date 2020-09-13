package com.charles.invalidmusic.core;

import com.charles.invalidmusic.core.model.*;

/**
 * MusicApi
 */
public interface MusicApi {
    Platform getPlatform();

    PageList<SearchItem> search(String keyword, int limit, int page, int type);

    Song getSongById(String songId);

    Playlist getPlaylistById(String playlistId);

    UrlInfo getUrlById(int bitrate, String... songId);

    Lyric getLyricById(String songId);
}
