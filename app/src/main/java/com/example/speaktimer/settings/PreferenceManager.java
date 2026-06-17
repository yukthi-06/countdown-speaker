package com.example.speaktimer.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "SpeakTimerPreferences";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_DEFAULT_SPEAK_ENABLED = "default_speak_enabled";
    private static final String KEY_DEFAULT_SPEAKING_INTERVAL = "default_speaking_interval";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isDefaultSpeakEnabled() {
        return prefs.getBoolean(KEY_DEFAULT_SPEAK_ENABLED, true);
    }

    public void setDefaultSpeakEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEFAULT_SPEAK_ENABLED, enabled).apply();
    }

    public int getDefaultSpeakingInterval() {
        return prefs.getInt(KEY_DEFAULT_SPEAKING_INTERVAL, 5); // default: 5 seconds
    }

    public void setDefaultSpeakingInterval(int intervalSeconds) {
        prefs.edit().putInt(KEY_DEFAULT_SPEAKING_INTERVAL, intervalSeconds).apply();
    }
}
