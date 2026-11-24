package com.headspa.redis.service;

/**
 * 同一位芳療師、同一天、同一時段已經有預約（已取消的不算）時拋出，
 * Controller 會轉成 HTTP 409 Conflict。
 */
public class TimeSlotConflictException extends RuntimeException {

    public TimeSlotConflictException(String date, String time) {
        super("這位芳療師 " + date + " " + time + " 已有預約，請選擇其他時段");
    }
}
