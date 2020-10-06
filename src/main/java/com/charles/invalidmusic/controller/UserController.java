package com.charles.invalidmusic.controller;

import com.charles.invalidmusic.common.BaseException;
import com.charles.invalidmusic.common.ErrorInfo;
import com.charles.invalidmusic.common.Response;
import com.charles.invalidmusic.config.JwtTokenUtil;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.model.Song;
import com.charles.invalidmusic.service.FavoriteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController
 *
 * @author charleswang
 * @since 2020/9/26 4:17 下午
 */
@Api("用户行为接口")
@RequestMapping("/api/user")
@Controller
public class UserController {

    private final FavoriteService favoriteService;

    @Autowired
    public UserController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @ApiOperation("收藏喜欢的歌曲")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "source", value = "歌曲来源", defaultValue = "netease")
    })
    @RequestMapping(value = "/favorite", method = RequestMethod.POST)
    public ErrorInfo favorite(@RequestBody @ApiParam(name = "song", value = "歌曲对象json", required = true) Song song,
                              @RequestParam String source,
                              @RequestHeader("Authorization") String token) throws BaseException {
        if (StringUtils.isEmpty(song.getId())) {
            throw new BaseException("The params of songId can not be null.");
        }
        if (Platform.forValue(source) == null) {
            throw new BaseException("The params of source is invalid.");
        }
        var username = JwtTokenUtil.getUsername(token.replace(JwtTokenUtil.TOKEN_PREFIX, ""));
        if (StringUtils.isEmpty(username)) {
            throw new BaseException("The authorization token is invalid.");
        }
        try {
            favoriteService.addFavoriteSong(username, song, source);
        } catch (JsonProcessingException e) {
            throw new BaseException(e.getMessage());
        }
        return new ErrorInfo();
    }

    @ApiOperation("取消收藏喜欢的歌曲")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "songId", value = "歌曲ID", required = true)
    })
    @RequestMapping(value = "/unfavorite/{songId}", method = RequestMethod.DELETE)
    public ErrorInfo unfavorite(@PathVariable String songId,
                                @RequestHeader("Authorization") String token) throws BaseException {
        if (StringUtils.isEmpty(songId)) {
            throw new BaseException("The params of songId can not be null.");
        }
        var username = JwtTokenUtil.getUsername(token.replace(JwtTokenUtil.TOKEN_PREFIX, ""));
        if (StringUtils.isEmpty(username)) {
            throw new BaseException("The authorization token is invalid.");
        }
        favoriteService.deleteFavoriteSong(username, songId);
        return new ErrorInfo();
    }

    @ApiOperation("查询收藏喜欢的歌曲")
    @ResponseBody
    @RequestMapping(value = "/favorites", method = RequestMethod.GET)
    public Response<List<Song>> favorites(@RequestHeader("Authorization") String token) throws BaseException {
        var username = JwtTokenUtil.getUsername(token.replace(JwtTokenUtil.TOKEN_PREFIX, ""));
        if (StringUtils.isEmpty(username)) {
            throw new BaseException("The authorization token is invalid.");
        }
        var songs = favoriteService.getFavoriteSong(username);
        return new Response<>(songs);
    }
}
