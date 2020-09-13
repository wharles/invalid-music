package com.charles.invalidmusic.core.model;

import lombok.Data;

import java.util.List;

@Data
public class PageList<T> {
    private int limit;
    private int page;
    private int total;
    private List<T> data;
}
