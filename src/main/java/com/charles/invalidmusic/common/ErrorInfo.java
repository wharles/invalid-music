package com.charles.invalidmusic.common;

import lombok.Data;

/**
 * ErrorInfo
 *
 * @author charleswang
 * @since 2020/9/12 4:44 下午
 */

@Data
public class ErrorInfo {
    public static final Integer OK = 0;
    public static final Integer ERROR = 100;

    private Integer errorCode;
    private String errorMessage;

    public ErrorInfo() {
    }
}
