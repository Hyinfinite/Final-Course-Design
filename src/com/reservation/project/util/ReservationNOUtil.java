package com.reservation.project.util;

import java.time.format.DateTimeFormatter;

public class ReservationNOUtil {
    public static String ReservationNO() {
        return "Reservation" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
