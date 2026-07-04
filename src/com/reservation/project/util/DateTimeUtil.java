package com.reservation.project.util;

import java.sql.Timestamp;

/**
 * 日期时间工具类
 * 提供日期时间相关的实用方法
 */
public class DateTimeUtil {
    /**
     * 将字符串转换为Timestamp时间戳
     * @param time 表示时间的字符串，格式应为JDBC timestamp格式
     * @return Timestamp对象，表示输入字符串对应的时间
     */
    public static Timestamp getTime(String time) {
        // 去除字符串两端的空白字符后转换为Timestamp对象
        return Timestamp.valueOf(time.trim());
    }
}
