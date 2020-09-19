package com.charles.invalidmusic.core.netease;

import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.netease.util.EncryptionUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * NeteaseClientService
 *
 * @author charleswang
 * @since 2020/9/6 12:50 下午
 */
@Component("NeteaseClientService")
public class NeteaseClientService extends HttpClientService {

    private static final String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.157 Safari/537.36";

    private static final String COOKIE = "os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true";

    private static final String REFERER = "http://music.163.com/";

    public NeteaseClientService(HttpClient httpClient) {
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

    @Override
    public String postForm(String url, String json) throws IOException, GeneralSecurityException, InterruptedException {
        String params = EncryptionUtil.encrypt(json, EncryptionUtil.NONCE);
        params = EncryptionUtil.encrypt(params, EncryptionUtil.SECRET_KEY);

        var parameters = Map.of("params", params, "encSecKey", EncryptionUtil.ENC_SEC_KEY);
        return super.postForm(url, buildHttpQuery(parameters));
    }
}
