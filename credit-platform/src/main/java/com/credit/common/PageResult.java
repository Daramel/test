package com.credit.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;
    private List<T> records;
    private Long current;
    private Long size;
    private Long pages;

    public static <T> PageResult<T> of(Long total, List<T> records, Long current, Long size) {
        return new PageResult<>(total, records, current, size, (total + size - 1) / size);
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records, 1L, (long) records.size(), 1L);
    }

}
