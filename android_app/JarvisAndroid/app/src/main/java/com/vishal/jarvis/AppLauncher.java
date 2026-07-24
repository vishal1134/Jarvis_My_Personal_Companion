package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppLauncher {
    private final Context context;
    private final PackageManager packageManager;
    private final Map<String, String> packageAliases = new HashMap<>();

    public AppLauncher(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        registerDefaultAliases();
    }

    public boolean openApp(String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }

        String normalizedTarget = normalize(target);
        String packageName = packageAliases.get(normalizedTarget);
        if (packageName == null) {
            packageName = findPackageByLabel(normalizedTarget);
        }

        if (packageName == null) {
            return false;
        }

        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return false;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
        return true;
    }

    private String findPackageByLabel(String normalizedTarget) {
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            CharSequence label = packageManager.getApplicationLabel(app);
            if (label == null) {
                continue;
            }

            String normalizedLabel = normalize(label.toString());
            if (normalizedLabel.equals(normalizedTarget) || normalizedLabel.contains(normalizedTarget)) {
                return app.packageName;
            }
        }
        return null;
    }

    private void registerDefaultAliases() {
        packageAliases.put("whatsapp", "com.whatsapp");
        packageAliases.put("youtube", "com.google.android.youtube");
        packageAliases.put("chrome", "com.android.chrome");
        packageAliases.put("google chrome", "com.android.chrome");
        packageAliases.put("google", "com.google.android.googlequicksearchbox");
        packageAliases.put("gmail", "com.google.android.gm");
        packageAliases.put("mail", "com.google.android.gm");
        packageAliases.put("instagram", "com.instagram.android");
        packageAliases.put("camera", "com.android.camera");
        packageAliases.put("calculator", "com.google.android.calculator");
        packageAliases.put("calc", "com.google.android.calculator");
        packageAliases.put("maps", "com.google.android.apps.maps");
        packageAliases.put("google maps", "com.google.android.apps.maps");
        packageAliases.put("photos", "com.google.android.apps.photos");
        packageAliases.put("play store", "com.android.vending");
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.US).trim().replaceAll("\\s+", " ");
    }
}
