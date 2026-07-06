package com.reservation.project.util;

import java.util.Random;

/**
 * 预订号生成工具类
 * 该类用于生成包含当前时间的唯一预订号
 */
public class ReservationNoUtil {

    /**
     * 生成唯一的预约编号
     * @return 返回格式为"RES" + 时间戳 + 随机数的预约编号
     */
    public static String ReservationNo() {
        return "RES" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
        // 获取当前系统时间的毫秒数（long类型）
        // 格式化字符串，确保数字部分为3位，不足补0
        // 生成0-999之间的随机整数
    }
}
