package com.example.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around SharedPreferences for the Settings screen. */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_ALERT_DAYS = "alert_days";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    /** Choices offered in the Settings spinner. */
    public static final int[] ALERT_DAY_OPTIONS = {1, 3, 7, 14};

    private Prefs() {
    }
    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context c) {
        return sp(c).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context c, boolean enabled) {
        sp(c).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public static int getAlertDays(Context c) {
        return sp(c).getInt(KEY_ALERT_DAYS, 3);
    }

    public static void setAlertDays(Context c, int days) {
        sp(c).edit().putInt(KEY_ALERT_DAYS, days).apply();
    }

    public static String getDefaultUnit(Context c) {
        return sp(c).getString(KEY_DEFAULT_UNIT, "g");
    }

    public static void setDefaultUnit(Context c, String unit) {
        sp(c).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
