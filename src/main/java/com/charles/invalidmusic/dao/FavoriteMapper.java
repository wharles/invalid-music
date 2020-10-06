package com.charles.invalidmusic.dao;

import com.charles.invalidmusic.model.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * FavoriteMapper
 *
 * @author charleswang
 * @since 2020/9/26 4:52 下午
 */
@Mapper
public interface FavoriteMapper {
    @Insert("INSERT INTO favorite(id, userId, songId, source, context) VALUES(#{favorite.id}, #{favorite.userId}, #{favorite.songId}, #{favorite.source}, #{favorite.context})")
    int addFavoriteSong(@Param("favorite") Favorite favorite);

    @Delete("DELETE FROM favorite WHERE songId = #{songId} and userId = #{userId}")
    int deleteFavoriteSong(@Param("songId") String songId, @Param("userId") String userId);

    @Select("SELECT * from favorite WHERE userId = #{userId}")
    List<Favorite> getFavoriteSong(@Param("userId") String userId);
}
