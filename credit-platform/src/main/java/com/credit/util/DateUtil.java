package com.credit.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_TIME = "HH:mm:ss";

    private DateUtil() {
    }

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern(PATTERN_DATE));
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DateTimeFormatter.ofPattern(PATTERN_DATETIME));
    }

    /**
     * 格式化日期
     */
    public static String format(Date date) {
        return date == null ? "" : DateUtil.format(date, PATTERN_DATETIME);
    }

    /**
     * 解析日期
     */
    public static LocalDate parseDate(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(PATTERN_DATE));
    }

    /**
     * 解析日期时间
     */
    public static LocalDateTime parseDateTime(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(PATTERN_DATETIME));
    }

    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDateStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

}
