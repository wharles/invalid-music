package com.charles.invalidmusic.service;

import com.charles.invalidmusic.common.BaseException;
import com.charles.invalidmusic.core.model.Song;
import com.charles.invalidmusic.dao.FavoriteMapper;
import com.charles.invalidmusic.dao.UserMapper;
import com.charles.invalidmusic.model.Favorite;
import com.charles.invalidmusic.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * FavoriteService
 *
 * @author charleswang
 * @since 2020/10/6 10:56 上午
 */
@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final UserMapper userMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, UserMapper userMapper) {
        this.favoriteMapper = favoriteMapper;
        this.userMapper = userMapper;
    }

    public void addFavoriteSong(String username, Song song, String source) throws JsonProcessingException, BaseException {
        var user = userMapper.findByUsername(username);
        var favorites = favoriteMapper.getFavoriteSong(user.getId());
        if (favorites != null) {
            var songIds = favorites.stream().map(Favorite::getSongId).collect(Collectors.toList());
            if (songIds.contains(song.getId())) {
                throw new BaseException("favorite song exists");
            }
        }
        var favorite = new Favorite();
        favorite.setId(UUID.randomUUID().toString());
        favorite.setUserId(user.getId());
        favorite.setSongId(song.getId());
        favorite.setSource(source);
        favorite.setContext(new ObjectMapper().writeValueAsString(song));
        var result = favoriteMapper.addFavoriteSong(favorite);
        if (result != 1) {
            throw new BaseException("add favorite song failed");
        }
    }

    public void deleteFavoriteSong(String username, String songId) throws BaseException {
        var user = userMapper.findByUsername(username);
        var result = favoriteMapper.deleteFavoriteSong(songId, user.getId());
        if (result != 1) {
            throw new BaseException("delete favorite song failed");
        }
    }

    public List<Song> getFavoriteSong(String username) {
        var user = userMapper.findByUsername(username);
        var favorites = favoriteMapper.getFavoriteSong(user.getId());
        if (favorites != null) {
            return favorites.stream().map(favorite -> {
                try {
                    return new ObjectMapper().readValue(favorite.getContext(), Song.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
