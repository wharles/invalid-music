package com.charles.invalidmusic.model;

import lombok.Data;

/**
 * Favorite
 *
 * @author charleswang
 * @since 2020/9/26 4:50 下午
 */
@Data
public class Favorite {
    private String id;

    private String userId;

    private String songId;

    private String source;

    private String context;
}
