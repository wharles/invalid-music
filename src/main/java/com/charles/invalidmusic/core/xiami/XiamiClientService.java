package com.charles.invalidmusic.core.xiami;

import com.charles.invalidmusic.core.base.HttpClientService;
import com.charles.invalidmusic.core.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XiamiClientService
 *
 * @author charleswang
 * @since 2020/10/17 9:54 上午
 */
@Component("XiamiClientService")
public class XiamiClientService extends HttpClientService {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) XIAMI-MUSIC/3.1.1 Chrome/56.0.2924.87 Electron/1.6.11 Safari/537.36";

    private static final String ACCEPT = "application/json";

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String ACCEPT_LANGUAGE = "zh-CN";

    private static final String APP_KEY = "12574478";

    private static final String ORIGIN_URL = "https://acs.m.xiami.com/h5/mtop.alimusic.recommend.songservice.getdailysongs/1.0/?appKey=12574478&t=1560663823000&dataType=json&data=%7B%22requestStr%22%3A%22%7B%5C%22header%5C%22%3A%7B%5C%22platformId%5C%22%3A%5C%22mac%5C%22%7D%2C%5C%22model%5C%22%3A%5B%5D%7D%22%7D&api=mtop.alimusic.recommend.songservice.getdailysongs&v=1.0&type=originaljson&sign=22ad1377ee193f3e2772c17c6192b17c";

    private final HttpClient httpClient;

    private String cookie = "_m_h5_tk=15d3402511a022796d88b249f83fb968_1511163656929; _m_h5_tk_enc=b6b3e64d81dae577fc314b5c5692df3c";

    public String getCookie() {
        return cookie;
    }

    @Autowired
    public XiamiClientService(HttpClient httpClient) {
        super(httpClient);
        this.httpClient = httpClient;
    }

    @Override
    public String[] getHeaders() {
        return new String[]{
                "Cookie", getCookie(),
                "User-Agent", USER_AGENT,
                "Accept", ACCEPT,
                "Content-type", CONTENT_TYPE,
                "Accept-Language", ACCEPT_LANGUAGE
        };
    }

    @Override
    public String get(String url, Map<String, String> params) throws IOException, InterruptedException {
        return super.get(url, buildTokenParams(params));
    }

    @Override
    public List<String> getRequests(String baseUrl, List<Map<String, String>> paramsList) {
        var newParamsList = paramsList.stream().map(params -> {
            try {
                return buildTokenParams(params);
            } catch (IOException | InterruptedException e) {
                return null;
            }
        }).collect(Collectors.toList());
        return super.getRequests(baseUrl, newParamsList);
    }

    private Map<String, String> buildTokenParams(Map<String, String> params) throws InterruptedException, IOException {
        var request = HttpRequest.newBuilder().GET().uri(URI.create(ORIGIN_URL)).headers(getHeaders()).build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 10 == 20 && response.body() != null) {
            var httpHeaders = response.headers();
            var cookies = httpHeaders.map().get("set-cookie");
            cookies = cookies.stream().map(c -> c.split(";")[0]).collect(Collectors.toList());
            this.cookie = String.join(";", cookies);

            var token = cookies.stream().filter(c -> c.contains("_m_h5_tk=")).findAny().orElse("").split("=")[1].split("_")[0];
            var ts = System.currentTimeMillis();

            var mapper = new ObjectMapper();
            var dataJson = mapper.writeValueAsString(params);
            var sign = EncryptionUtil.generateMD5(String.format("%s&%s&%s&%s", token, ts, APP_KEY, dataJson));

            return Map.of("appKey", APP_KEY,
                    "t", String.valueOf(ts),
                    "dataType", "json",
                    "data", dataJson,
                    "v", "1.0",
                    "type", "originaljson",
                    "sign", sign);
        }
        return Map.of();
    }
}
