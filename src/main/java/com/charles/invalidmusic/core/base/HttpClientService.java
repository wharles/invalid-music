package com.charles.invalidmusic.core.base;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * OkHttpClientService
 *
 * @author charleswang
 * @since 2020/9/13 2:26 下午
 */
public abstract class HttpClientService {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    @Autowired
    public HttpClientService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * Referer
     *
     * @return Referer
     */
    public abstract String getReferer();

    /**
     * Cookie
     *
     * @return Cookie
     */
    public abstract String getCookie();

    /**
     * UserAgent
     *
     * @return UserAgent
     */
    public abstract String getUserAgent();

    /**
     * 抽象方法，POST使用json作为body场景使用
     *
     * @param url    请求的URL
     * @param params json格式参数
     * @return 返回的json内容
     * @throws IOException              json操作IO异常
     * @throws GeneralSecurityException 常用加密操作异常
     */
    public String request(String url, String params) throws IOException, GeneralSecurityException {
        return request(url, RequestBody.create(JSON, params));
    }

    /**
     * GET 请求
     *
     * @param url HttpUrl
     * @return 返回body字符串
     * @throws IOException IO异常
     */
    public String request(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", getReferer())
                .addHeader("Cookie", getCookie())
                .header("User-Agent", getUserAgent())
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().string();
        }
        return null;
    }

    /**
     * POST 请求
     *
     * @param url    API URL
     * @param params POST请求参数
     * @return 返回json字符串
     * @throws IOException IO异常
     */
    public String request(String url, RequestBody params) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", getReferer())
                .addHeader("Cookie", getCookie())
                .header("User-Agent", getUserAgent())
                .post(params)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().string();
        }
        return null;
    }
}
