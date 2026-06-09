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

