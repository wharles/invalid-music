package com.charles.invalidmusic.core.netease;

import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.util.EncryptionUtil;
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

    private static final String SECRET_KEY = "TA3YiYCfY2dDJQgg";

    private static final String ENC_SEC_KEY = "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210";

    private static final String NONCE = "0CoJUm6Qyw8W8jud";

    private static final String IV = "0102030405060708";

    public NeteaseClientService(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String[] getHeaders() {
        return new String[] {
            "Referer", REFERER, "Cookie", COOKIE, "User-Agent", USERAGENT
        } ;
    }

    @Override
    public String postForm(String url, String json) throws IOException, GeneralSecurityException, InterruptedException {
        String params = EncryptionUtil.encrypt(json, NONCE, IV);
        params = EncryptionUtil.encrypt(params, SECRET_KEY, IV);

        var parameters = Map.of("params", params, "encSecKey", ENC_SEC_KEY);
        return super.postForm(url, buildHttpQuery(parameters));
    }
}
