package com.charles.invalidmusic.controller;

import com.charles.invalidmusic.common.BaseException;
import com.charles.invalidmusic.common.Response;
import com.charles.invalidmusic.common.ResponseList;
import com.charles.invalidmusic.core.MusicApi;
import com.charles.invalidmusic.core.MusicFactory;
import com.charles.invalidmusic.core.Platform;
import com.charles.invalidmusic.core.Quality;
import com.charles.invalidmusic.core.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * MusicController
 *
 * @author charleswang
 * @since 2020/8/30 3:13 下午
 */
@Api("音乐查询接口")
@RequestMapping("/api/music")
@Controller
public class MusicController {

    private final MusicFactory musicFactory;

    private MusicApi musicApi;

    @Autowired
    public MusicController(MusicFactory musicFactory) {
        this.musicFactory = musicFactory;
        musicApi = MusicFactory.factory(Platform.NETEASE);
    }

    @ApiOperation("根据关键字分页查询音乐接口")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "分页大小", defaultValue = "20"),
            @ApiImplicitParam(name = "pageIndex", value = "分页页码", defaultValue = "1"),
            @ApiImplicitParam(name = "keyword", value = "查询关键字", required = true),
            @ApiImplicitParam(name = "platform", value = "查询平台", defaultValue = "NETEASE")
    })
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseList<SearchItem> search(@RequestParam @Nullable Integer pageSize,
                                           @RequestParam @Nullable Integer pageIndex,
                                           @RequestParam @Nullable String keyword,
                                           @RequestParam @Nullable Platform platform) throws BaseException {
        if (pageIndex == null || pageIndex < 0) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize < 0 || pageSize > 1000) {
            pageSize = 20;
        }
        if (StringUtils.isEmpty(keyword)) {
            throw new BaseException("The params of keyword can not be null.");
        }
        musicApi = MusicFactory.factory(Optional.ofNullable(platform).orElse(Platform.NETEASE));
        var itemPageList = musicApi.search(keyword, pageSize, pageIndex, SearchType.SONG);
        return new ResponseList<>(itemPageList.getTotal(), itemPageList.getLimit(), itemPageList.getPage(), itemPageList.getData());
    }

    @ApiOperation("根据ID查询对应歌曲的歌词接口")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "songId", value = "歌曲ID", required = true),
            @ApiImplicitParam(name = "platform", value = "查询平台", defaultValue = "NETEASE")
    })
    @RequestMapping(value = "/lyric", method = RequestMethod.GET)
    public Response<Lyric> lyric(@RequestParam @Nullable String songId,
                                 @RequestParam @Nullable Platform platform) throws BaseException {
        if (StringUtils.isEmpty(songId)) {
            throw new BaseException("The params of songId can not be null.");
        }
        musicApi = MusicFactory.factory(Optional.ofNullable(platform).orElse(Platform.NETEASE));
        var lyric = musicApi.getLyricById(songId);
        return new Response<>(lyric);
    }

    @ApiOperation("根据ID查询歌曲详情接口")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "songId", value = "歌曲ID", required = true),
            @ApiImplicitParam(name = "platform", value = "查询平台", defaultValue = "NETEASE"),
            @ApiImplicitParam(name = "quality", value = "歌曲质量", defaultValue = "HQ")
    })
    @RequestMapping(value = "/song/{songId}", method = RequestMethod.GET)
    public Response<Song> song(@PathVariable @Nullable String songId,
                               @RequestParam @Nullable Platform platform,
                               @RequestParam @Nullable Quality quality) throws BaseException {
        if (StringUtils.isEmpty(songId)) {
            throw new BaseException("The params of songId can not be null.");
        }
        musicApi = MusicFactory.factory(Optional.ofNullable(platform).orElse(Platform.NETEASE));
        var song = musicApi.getSongById(songId, Optional.ofNullable(quality).orElse(Quality.HQ));
        return new Response<>(song);
    }

    @ApiOperation("根据ID查询歌曲URL播放地址接口")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "songId", value = "歌曲ID", required = true),
            @ApiImplicitParam(name = "platform", value = "查询平台", defaultValue = "NETEASE"),
            @ApiImplicitParam(name = "quality", value = "歌曲质量", defaultValue = "HQ")
    })
    @RequestMapping(value = "/url", method = RequestMethod.GET)
    public Response<List<UrlInfo>> url(@RequestParam @Nullable String songId,
                                       @RequestParam @Nullable Platform platform,
                                       @RequestParam @Nullable Quality quality) throws BaseException {
        if (StringUtils.isEmpty(songId)) {
            throw new BaseException("The params of songId can not be null.");
        }
        musicApi = MusicFactory.factory(Optional.ofNullable(platform).orElse(Platform.NETEASE));
        var urlInfoList = musicApi.getUrlById(Optional.ofNullable(quality).orElse(Quality.HQ), songId);
        return new Response<>(urlInfoList);
    }

    @ApiOperation("根据播放列表ID查询播放列表接口")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "playlistId", value = "播放列表ID", required = true),
            @ApiImplicitParam(name = "platform", value = "查询平台", defaultValue = "NETEASE")
    })
    @RequestMapping(value = "/playlist/{playlistId}", method = RequestMethod.GET)
    public Response<Playlist> playlist(@PathVariable String playlistId,
                                       @RequestParam @Nullable Platform platform) throws BaseException {
        if (StringUtils.isEmpty(playlistId)) {
            throw new BaseException("The params of playlistId can not be null.");
        }
        musicApi = MusicFactory.factory(Optional.ofNullable(platform).orElse(Platform.NETEASE));
        return new Response<>(musicApi.getPlaylistById(playlistId));
    }
}
