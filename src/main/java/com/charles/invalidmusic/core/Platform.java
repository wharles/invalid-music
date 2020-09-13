package com.charles.invalidmusic.core;

import java.util.stream.Stream;

/**
 * Platform
 *
 * @author charleswang
 * @since 2020/8/30 12:46 下午
 */
public enum Platform {
    NETEASE("netease"), TENCENT("tencent"), XIAMI("xiami"), KUGOU("kugou"), BAIDU("BAIDU");

    private String value;

    Platform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Platform forValue(String value) {
        return Stream.of(Platform.values()).filter(p -> p.getValue().equals(value)).findAny().orElse(null);
    }
}
