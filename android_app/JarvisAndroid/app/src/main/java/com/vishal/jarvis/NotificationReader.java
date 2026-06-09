package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class NotificationReader {
    private final Context context;

    public NotificationReader(Context context) {
        this.context = context;
    }

    public boolean hasNotificationAccess() {
        String enabledListeners = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );

        return enabledListeners != null
                && enabledListeners.toLowerCase(Locale.US).contains(context.getPackageName().toLowerCase(Locale.US));
    }

    public void openNotificationAccessSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public String summarizeNotifications(String title) {
        if (!hasNotificationAccess()) {
            return "Please enable notification access for Jarvis, " + title + ".";
        }

        StatusBarNotification[] notifications = JarvisNotificationListener.getCurrentNotifications();
        if (notifications.length == 0) {
            return "You have no active notifications, " + title + ".";
        }

        Map<String, Integer> countsByApp = new LinkedHashMap<>();
        int total = 0;
        for (StatusBarNotification notification : notifications) {
            if (context.getPackageName().equals(notification.getPackageName())) {
                continue;
            }

            String appName = appNameFor(notification.getPackageName());
            countsByApp.put(appName, countsByApp.getOrDefault(appName, 0) + 1);
            total++;
        }

        if (total == 0) {
            return "You have no active notifications, " + title + ".";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("You have ").append(total);
        summary.append(total == 1 ? " active notification" : " active notifications");
        summary.append(", ").append(title).append(". ");

        int shown = 0;
        for (Map.Entry<String, Integer> entry : countsByApp.entrySet()) {
            if (shown >= 3) {
                break;
            }
            if (shown > 0) {
                summary.append(", ");
            }
            summary.append(entry.getValue()).append(" from ").append(entry.getKey());
            shown++;
        }
        summary.append(".");
        return summary.toString();
    }

    private String appNameFor(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(info);
            return label == null ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException exception) {
            return packageName;
        }
    }
}

