# Jarvis Android

This is the first Java Android MVP for Jarvis.

## Current Feature

```text
Type command
  -> send to laptop Jarvis server
  -> receive spoken_response
  -> speak using Android TextToSpeech
```

This text box is only for early testing. The final Jarvis flow will be
voice-to-voice with actions.

The current app also includes a push-to-talk Mic button:

```text
Tap Mic
  -> speak command
  -> recognized text is sent to Jarvis
  -> Jarvis speaks
  -> Android performs the action
```

The app includes a Jarvis voice selector. Android does not always label voices
as male or female, so choose the male-sounding installed voice for Jarvis. Later
we will add separate profiles:

```text
Jarvis = male voice
Friday = female voice
```

## Current Actions

The app now attempts to open installed apps for commands like:

```text
Jarvis open whatsapp
Jarvis open calculator
```

It also attempts to call contacts:

```text
Jarvis call appa
appa ku call pannu
```

The first time, Android will ask for Contacts and Phone permissions. Allow them,
then send the command again.

Notification reading is planned next. It requires Android Notification Listener
access, which must be enabled manually in phone settings.

Current notification command:

```text
Jarvis read notifications
```

The first time, Jarvis opens Android's notification access screen. Enable Jarvis
there, return to the app, then run the command again.

## Setup

1. Install Android Studio.
2. Open this folder:

```text
C:\Users\acer\Documents\jarvis_slm\android_app\JarvisAndroid
```

3. Let Android Studio sync Gradle.
4. Start the laptop server:

```powershell
cd C:\Users\acer\Documents\jarvis_slm
python -m uvicorn server.api.main:app --reload --host 0.0.0.0 --port 8000
```

5. Find your laptop Wi-Fi IP:

```powershell
ipconfig
```

6. In the app, set server URL:

```text
http://YOUR_LAPTOP_IP:8000
```

Example:

```text
http://192.168.1.10:8000
```

Do not use `127.0.0.1` on the phone. On Android, that points to the phone
itself, not your laptop.
