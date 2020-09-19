package com.charles.invalidmusic.core.base;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OkHttpClientService
 *
 * @author charleswang
 * @since 2020/9/13 2:26 下午
 */
public abstract class HttpClientService {

    private static final String FORM = "application/x-www-form-urlencoded";

    private static final String JSON = "application/json;charset=UTF-8";

    private static final String GZIP = "application/gzip";

    private final HttpClient httpClient;


    @Autowired
    public HttpClientService(HttpClient httpClient) {
        this.httpClient = httpClient;
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
     * POST使用form作为body场景使用
     *
     * @param url    请求的URL
     * @param params form格式参数
     * @return 返回的json内容
     * @throws IOException              json操作IO异常
     * @throws GeneralSecurityException 常用加密操作异常
     * @throws InterruptedException     运行中断异常
     */
    public String postForm(String url, String params) throws IOException, GeneralSecurityException, InterruptedException {
        return post(url, params, FORM);
    }

    /**
     * POST使用json作为body场景使用
     *
     * @param url    请求的URL
     * @param params json格式参数
     * @return 返回的json内容
     * @throws IOException          json操作IO异常
     * @throws InterruptedException 运行中断异常
     */
    public String postJson(String url, String params) throws IOException, InterruptedException {
        return post(url, params, JSON);
    }

    private String post(String url, String params, String contentType) throws IOException, InterruptedException {
        var requestBody = HttpRequest.BodyPublishers.ofString(params);
        var postRequest = HttpRequest.newBuilder()
                .header("Referer", getReferer())
                .header("Cookie", getCookie())
                .header("User-Agent", getUserAgent())
                .header("Content-Type", contentType)
                .header("Accept-Encoding", GZIP)
                .POST(requestBody).uri(URI.create(url)).build();
        var response = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 10 == 20 && response.body() != null) {
            return response.body();
        }

        return null;
    }

    /**
     * 构建请求参数
     *
     * @param params 参数map
     * @return 参数字符串
     */
    protected String buildHttpQuery(Map<String, String> params) {
        return params.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * GET 请求
     *
     * @param url HttpUrl
     * @return 返回body字符串
     * @throws IOException          IO异常
     * @throws InterruptedException 运行中断异常
     */
    public String get(String url) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Referer", getReferer())
                .header("Cookie", getCookie())
                .header("User-Agent", getUserAgent())
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 10 == 20 && response.body() != null) {
            return response.body();
        }
        return null;
    }

    /**
     * GET 请求
     *
     * @param params map格式请求参数
     * @param url    HttpUrl
     * @return 返回body字符串
     * @throws IOException          IO异常
     * @throws InterruptedException 运行中断异常
     */
    public String get(String url, Map<String, String> params) throws IOException, InterruptedException {
        var urlWithParams = url + "?" + buildHttpQuery(params);
        return this.get(urlWithParams);
    }
}
