package com.charles.invalidmusic.dao;

import com.charles.invalidmusic.model.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

/**
 * UserMapper
 *
 * @author charleswang
 * @since 2020/9/26 12:40 下午
 */
@Mapper
public interface UserMapper {
    @Insert("INSERT INTO account(id, username, password, role) VALUES(#{user.id}, #{user.username}, #{user.password}, #{user.role})")
    int createUser(@Param("user") User user);

    @Delete("DELETE FROM account WHERE username = #{username}")
    int deleteUser(@Param("username") String username);

    @Update("UPDATE account SET password = #{password} WHERE username = #{username}")
    int modifyUser(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM account WHERE username = #{username}")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.VARCHAR, id = true),
            @Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
            @Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
            @Result(column = "role", property = "role", jdbcType = JdbcType.VARCHAR)
    })
    User findByUsername(@Param("username") String username);
}
