package com.jd.ecommerce.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) ((total + pageSize - 1) / pageSize);
    }
}
