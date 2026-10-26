package com.example.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Helpers for the optional yyyy-MM-dd expiry date on pantry items. */
public final class ExpiryUtils {

    public enum State { NONE, OK, SOON, EXPIRED }

    private static final String FORMAT = "yyyy-MM-dd";
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private ExpiryUtils() {
    }

    private static SimpleDateFormat formatter() {
        SimpleDateFormat f = new SimpleDateFormat(FORMAT, Locale.US);
        f.setLenient(false);
        return f;
    }

    public static String format(Calendar cal) {
        return formatter().format(cal.getTime());
    }

    /** Parses a stored date, returning null for empty or malformed input. */
    public static Date parse(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            return formatter().parse(date.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static Calendar startOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** Whole days from today until the date (negative if it has passed), or null if no valid date. */
    public static Integer daysUntil(String date) {
        Date d = parse(date);
        if (d == null) {
            return null;
        }
        Calendar target = Calendar.getInstance();
        target.setTime(d);
        startOfDay(target);
        Calendar today = startOfDay(Calendar.getInstance());
        return (int) Math.round((target.getTimeInMillis() - today.getTimeInMillis()) / (double) DAY_MS);
    }

    public static State state(String date, int thresholdDays) {
        Integer days = daysUntil(date);
        if (days == null) {
            return State.NONE;
        }
        if (days < 0) {
            return State.EXPIRED;
        }
        return days <= thresholdDays ? State.SOON : State.OK;
    }
}
