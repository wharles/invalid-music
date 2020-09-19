package com.charles.invalidmusic.core.tencent;

import com.charles.invalidmusic.core.base.HttpClientService;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

/**
 * TencentHttpClientService
 *
 * @author charleswang
 * @since 2020/9/12 6:19 下午
 */
@Component("TencentClientService")
public class TencentClientService extends HttpClientService {

    private static final String USERAGENT = "QQ%E9%9F%B3%E4%B9%90/54409 CFNetwork/901.1 Darwin/17.6.0 (x86_64)";

    private static final String COOKIE = "pgv_pvi=22038528; pgv_si=s3156287488; pgv_pvid=5535248600; yplayer_open=1; ts_last=y.qq.com/portal/player.html; ts_uid=4847550686; yq_index=0; qqmusic_fromtag=66; player_exist=1";

    private static final String REFERER = "http://y.qq.com";

    public TencentClientService(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getReferer() {
        return REFERER;
    }

    @Override
    public String getCookie() {
        return COOKIE;
    }

    @Override
    public String getUserAgent() {
        return USERAGENT;
    }
}
