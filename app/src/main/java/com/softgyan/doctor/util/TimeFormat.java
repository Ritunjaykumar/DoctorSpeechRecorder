package com.softgyan.doctor.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeFormat {
    public static String getTimeAgo(Long duration) {
        Date now = new Date();
        long second = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minute = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long day = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if (second < 60) {
            return "just now";
        } else if (minute == 1) {
            return "a minute ago";
        } else if (minute < 60) {
            return minute + " minutes ago";
        } else if (hours == 1) {
            return "a hour ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (day == 1) {
            return " a day ago";
        } else {
            return day + " days ago";
        }
    }


}
