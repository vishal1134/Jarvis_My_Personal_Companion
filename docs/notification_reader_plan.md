# Notification Reader Plan

Jarvis can read notifications on Android, but it needs the user to manually
grant Notification Listener access.

## Android Requirement

Android does not allow a normal app to silently read notifications. The user must
enable the app in:

```text
Settings -> Notifications -> Device & app notifications -> Notification access
```

The exact screen name changes by Android version and phone brand.

## First Behavior

Command:

```text
Jarvis read notifications
```

Jarvis should:

```text
1. Check notification access.
2. If not granted, open the notification access settings screen.
3. If granted, read recent notification titles/app names aloud.
4. Avoid reading sensitive message content until owner security is added.
```

## Privacy Rule

Until owner voice recognition/security exists, Jarvis should only read safe
notification summaries:

```text
You have 2 WhatsApp notifications and 1 missed call.
```

Later, owner-only mode can read more details.

