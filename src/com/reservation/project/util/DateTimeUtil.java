package com.reservation.project.util;

import java.sql.Timestamp;

public class DateTimeUtil {
    public static Timestamp getTime(String time) {
        return Timestamp.valueOf(time.trim());
    }
}
