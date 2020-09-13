package com.charles.invalidmusic.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Response
 *
 * @author charleswang
 * @since 2020/9/12 5:18 下午
 */
@Setter
@Getter
public class Response<T> extends ErrorInfo {
    private T data;

    public Response(T data) {
        this.data = data;
        this.setErrorCode(ErrorInfo.OK);
    }

    public Response() {
    }
}
