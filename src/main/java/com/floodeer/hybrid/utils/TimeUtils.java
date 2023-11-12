package com.floodeer.hybrid.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_DAY = "yyyy-MM-dd";

    public static String now() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String time() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String when(long paramLong) {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return localSimpleDateFormat.format(paramLong);
    }

    public static String date() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String formatScoreboard(int s) {
        Date date = new Date(s * 1000L);
        return new SimpleDateFormat("mm:ss").format(date);
    }


    public enum TimeUnit {
        FIT, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS
    }

    public static boolean elapsed(long paramLong1, long paramLong2) {
        return System.currentTimeMillis() - paramLong1 > paramLong2;
    }

    public static String toString(long paramLong) {
        return convertString(paramLong, 1, TimeUnit.FIT);
    }

    public static String convertString(long paramLong, int paramInt, TimeUnit paramTimeUnit) {
        if (paramLong == -1L) {
            return "Permanente";
        }
        if (paramTimeUnit == TimeUnit.FIT) {
            if (paramLong < 60000L) {
                paramTimeUnit = TimeUnit.SECONDS;
            } else if (paramLong < 3600000L) {
                paramTimeUnit = TimeUnit.MINUTES;
            } else if (paramLong < 86400000L) {
                paramTimeUnit = TimeUnit.HOURS;
            } else {
                paramTimeUnit = TimeUnit.DAYS;
            }
        }
        if (paramTimeUnit == TimeUnit.DAYS) {
            return MathUtils.trim(paramInt, paramLong / 8.64E7D) + " dias";
        }
        if (paramTimeUnit == TimeUnit.HOURS) {
            return MathUtils.trim(paramInt, paramLong / 3600000.0D) + " horas";
        }
        if (paramTimeUnit == TimeUnit.MINUTES) {
            return MathUtils.trim(paramInt, paramLong / 60000.0D) + " minutos";
        }
        if (paramTimeUnit == TimeUnit.SECONDS) {
            return MathUtils.trim(paramInt, paramLong / 1000.0D) + " segundos";
        }
        return MathUtils.trim(paramInt, paramLong) + " milissegundos";
    }
}