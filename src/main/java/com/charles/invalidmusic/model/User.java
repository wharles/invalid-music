package com.charles.invalidmusic.model;

import lombok.Data;
import lombok.ToString;

/**
 * User
 *
 * @author charleswang
 * @since 2020/9/26 12:37 下午
 */
@Data
@ToString(exclude = {"password"})
public class User {
    private String id;

    private String username;

    private String password;

    private String role;
}
