package com.charles.invalidmusic.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionHandler
 *
 * @author charleswang
 * @since 2020/9/12 4:48 下午
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo jsonErrorHandler(HttpServletRequest req, BaseException e) throws Exception {
        ErrorInfo r = new ErrorInfo();
        r.setErrorMessage(e.getMessage());
        r.setErrorCode(ErrorInfo.ERROR);
        return r;
    }
}
