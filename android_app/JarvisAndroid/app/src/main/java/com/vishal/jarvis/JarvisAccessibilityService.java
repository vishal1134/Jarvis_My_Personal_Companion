package com.vishal.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
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

    public static boolean tapVisibleText(String text) {
        JarvisAccessibilityService service = activeService.get();
        if (service == null || text == null || text.trim().isEmpty()) {
            return false;
        }
        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        AccessibilityNodeInfo node = service.findNodeByText(root, text.trim().toLowerCase());
        if (node == null) {
            return false;
        }

        AccessibilityNodeInfo clickable = node;
        while (clickable != null && !clickable.isClickable()) {
            clickable = clickable.getParent();
        }
        if (clickable != null && clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true;
        }

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        return service.tap(bounds.centerX(), bounds.centerY());
    }

    public static boolean typeIntoFocusedField(String text) {
        JarvisAccessibilityService service = activeService.get();
        if (service == null || text == null) {
            return false;
        }
        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        AccessibilityNodeInfo focused = service.findFocusedEditable(root);
        if (focused == null) {
            return false;
        }

        Bundle arguments = new Bundle();
        arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
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

    private boolean tap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 80);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();
        return dispatchGesture(gesture, null, null);
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo node, String target) {
        if (node == null) {
            return null;
        }

        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        if (matches(text, target) || matches(description, target)) {
            return node;
        }

        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo match = findNodeByText(node.getChild(index), target);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findFocusedEditable(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        if (node.isFocused() && node.isEditable()) {
            return node;
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo match = findFocusedEditable(node.getChild(index));
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private boolean matches(CharSequence value, String target) {
        return value != null && value.toString().toLowerCase().contains(target);
    }
}
