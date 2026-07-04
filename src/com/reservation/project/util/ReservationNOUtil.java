package com.reservation.project.util;

import java.time.format.DateTimeFormatter;

/**
 * 预订号生成工具类
 * 该类用于生成包含当前时间的唯一预订号
 */
public class ReservationNOUtil {
    /**
     * 生成预订号的方法
     * @return 返回一个以"Reservation"开头，后跟当前时间的字符串，格式为yyyyMMddHHmmss
     * 例如：Reservation20230825143045
     */
    public static String ReservationNO() {
        // 拼接"Reservation"字符串和格式化后的当前时间
        return "Reservation" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
