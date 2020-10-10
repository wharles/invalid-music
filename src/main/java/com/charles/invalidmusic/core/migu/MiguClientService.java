package com.charles.invalidmusic.core.migu;

import com.charles.invalidmusic.core.base.HttpClientService;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

/**
 * MiguClientService
 *
 * @author charleswang
 * @since 2020/10/10 9:43 下午
 */
@Component("MiguClientService")
public class MiguClientService extends HttpClientService {

    private static final String USERAGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 8_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12F70 Safari/600.1.4";

    private static final String REFERER = "http://music.migu.cn/v3/music/player/audio";

    private static final String CHANNEL = "0146951";

    private static final String UID = "1234";

    public MiguClientService(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String[] getHeaders() {
        return new String[]{
                "User-Agent", USERAGENT,
                "Referer", REFERER,
                "channel", CHANNEL,
                "uid", UID
        };
    }
}
