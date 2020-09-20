package com.charles.invalidmusic.core.kugou;

import com.charles.invalidmusic.core.base.HttpClientService;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

/**
 * KugouClientService
 *
 * @author charleswang
 * @since 2020/9/20 4:28 下午
 */
@Component("KugouClientService")
public class KugouClientService extends HttpClientService {

    private static final String USERAGENT = "IPhone-8990-searchSong";

    private static final String UNI_USERAGENT = "iOS11.4-Phone8990-1009-0-WiFi";

    public KugouClientService(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String[] getHeaders() {
        return new String[]{
                "User-Agent", USERAGENT,
                "UNI-UserAgent", UNI_USERAGENT
        };
    }
}
