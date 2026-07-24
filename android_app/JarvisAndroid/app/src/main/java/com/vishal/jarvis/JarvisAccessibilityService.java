package com.vishal.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

import java.lang.ref.WeakReference;

public class JarvisAccessibilityService extends AccessibilityService {
    private static WeakReference<JarvisAccessibilityService> activeService = new WeakReference<>(null);

    public static boolean isRunning() {
        return activeService.get() != null;
    }

    public static boolean performPhoneAction(String action) {
        JarvisAccessibilityService service = activeService.get();
        if (service == null || action == null) {
            return false;
        }

        if ("back".equals(action)) {
            return service.performGlobalAction(GLOBAL_ACTION_BACK);
        }
        if ("home".equals(action)) {
            return service.performGlobalAction(GLOBAL_ACTION_HOME);
        }
        if ("recents".equals(action)) {
            return service.performGlobalAction(GLOBAL_ACTION_RECENTS);
        }
        if ("notifications".equals(action)) {
            return service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        }
        if ("scroll_down".equals(action)) {
            return service.swipe(540f, 1650f, 540f, 650f);
        }
        if ("scroll_up".equals(action)) {
            return service.swipe(540f, 650f, 540f, 1650f);
        }
        return false;
    }

    @Override
    protected void onServiceConnected() {
        activeService = new WeakReference<>(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        activeService.clear();
        super.onDestroy();
    }

    private boolean swipe(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 450);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();
        return dispatchGesture(gesture, null, null);
    }
}
