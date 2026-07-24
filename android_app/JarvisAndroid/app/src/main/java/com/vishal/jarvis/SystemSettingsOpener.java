package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class SystemSettingsOpener {
    private static final String ACTION_NOTIFICATION_SETTINGS = "android.settings.NOTIFICATION_SETTINGS";

    private final Context context;

    public SystemSettingsOpener(Context context) {
        this.context = context;
    }

    public void open(String target) {
        Intent intent = new Intent(actionFor(target));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (RuntimeException exception) {
            Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallbackIntent);
        }
    }

    private String actionFor(String target) {
        if ("wifi".equals(target)) {
            return Settings.ACTION_WIFI_SETTINGS;
        }
        if ("bluetooth".equals(target)) {
            return Settings.ACTION_BLUETOOTH_SETTINGS;
        }
        if ("notifications".equals(target)) {
            return ACTION_NOTIFICATION_SETTINGS;
        }
        if ("accessibility".equals(target)) {
            return Settings.ACTION_ACCESSIBILITY_SETTINGS;
        }
        return Settings.ACTION_SETTINGS;
    }
}
