package com.charles.invalidmusic.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Lyric
 *
 * @author charleswang
 * @since 2020/9/12 3:30 下午
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lyric {
    private String content;

    private String translate;

    @JsonProperty("lrc")
    public void unpackContentFromNestedLrc(JsonNode lyricNode) {
        this.content = lyricNode.path("lyric").asText();
    }

    @JsonProperty("tlyric")
    public void unpackTranslateFromNestedTlyric(JsonNode transNode) {
        this.translate = transNode.path("lyric").asText();
    }

    @JsonProperty("lyric")
    public void unpackContentFromNestedLyric(String lyric) {
        this.content = new String(Base64.getDecoder().decode(lyric.getBytes()));
    }

    @JsonProperty("trans")
    public void unpackTranslateFromNestedTrans(String trans) {
        if (!StringUtils.isEmpty(trans)) {
            this.translate = new String(Base64.getDecoder().decode(trans.getBytes()));
        }
    }
}
