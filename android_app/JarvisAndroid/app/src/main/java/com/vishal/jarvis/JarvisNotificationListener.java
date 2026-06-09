package com.vishal.jarvis;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class JarvisNotificationListener extends NotificationListenerService {
    private static JarvisNotificationListener activeInstance;

    @Override
    public void onListenerConnected() {
        activeInstance = this;
    }

    @Override
    public void onListenerDisconnected() {
        if (activeInstance == this) {
            activeInstance = null;
        }
    }

    public static StatusBarNotification[] getCurrentNotifications() {
        if (activeInstance == null) {
            return new StatusBarNotification[0];
        }

        try {
            return activeInstance.getActiveNotifications();
        } catch (SecurityException exception) {
            return new StatusBarNotification[0];
        }
    }
}

