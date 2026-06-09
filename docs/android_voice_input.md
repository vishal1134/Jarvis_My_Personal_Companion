# Android Voice Input

The current Android MVP supports push-to-talk voice commands.

## Flow

```text
Tap Mic
  -> Android listens once
  -> SpeechRecognizer converts speech to text
  -> App sends text to Jarvis server
  -> Jarvis speaks response
  -> Android performs action
```

## Permission

Android will ask for:

```text
RECORD_AUDIO
```

Allow it, then tap Mic again.

## Test Commands

```text
Jarvis open calculator
Jarvis open whatsapp
Jarvis call appa
appa ku call pannu
Jarvis read notifications
```

## Current Limitation

This is not always-listening wake-word mode yet. It is push-to-talk. Wake-name
and always-listening service come later.
