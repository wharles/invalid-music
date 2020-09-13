package com.charles.invalidmusic.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ResponseList
 *
 * @author charleswang
 * @since 2020/9/12 5:18 下午
 */
@Getter
@Setter
public class ResponseList<T> extends ErrorInfo {
    private Integer totalRecords;
    private Integer pageSize;
    private Integer pageIndex;
    private List<T> data;

    public ResponseList(Integer totalRecords, Integer pageSize, Integer pageIndex, List<T> data) {
        this.totalRecords = totalRecords;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
        this.data = data;
        this.setErrorCode(ErrorInfo.OK);
    }

    public ResponseList() {
    }
}
