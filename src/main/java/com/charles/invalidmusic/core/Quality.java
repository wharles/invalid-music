package com.charles.invalidmusic.core;

import java.util.stream.Stream;

/**
 * Quality
 *
 * @author charleswang
 * @since 2020/10/17 2:57 下午
 */
public enum Quality {
    PQ("PQ", 128000), HQ("HQ", 192000), SQ("SQ", 320000);

    private final String name;

    private final int bitrate;

    Quality(String value, int bitrate) {
        this.name = value;
        this.bitrate = bitrate;
    }

    public String getName() {
        return name;
    }

    public int getBitrate() {
        return bitrate;
    }

    public static Quality forName(String value) {
        return Stream.of(Quality.values()).filter(q -> q.getName().equals(value)).findAny().orElse(null);
    }
}
