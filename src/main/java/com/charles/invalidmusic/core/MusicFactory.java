package com.charles.invalidmusic.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MusicFactory
 *
 * @author charleswang
 * @since 2020/08/30
 */
@Service
public class MusicFactory {

    private static final Map<Platform, MusicApi> serviceCache = new HashMap<>();

    @Autowired
    public MusicFactory(List<MusicApi> services) {
        for (MusicApi service : services) {
            serviceCache.put(service.getPlatform(), service);
        }
    }

    public static MusicApi factory(Platform platform) {
        MusicApi service = serviceCache.get(platform);
        if (service == null) {
            throw new RuntimeException("Unknown platform service: " + platform);
        }
        return service;
    }
}
