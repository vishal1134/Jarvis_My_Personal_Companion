# Jarvis SLM

Jarvis SLM is a personal Android-first bilingual voice assistant for private use.
It is designed to work without paid AI APIs, using a local laptop server for
training and heavier inference when available, while keeping core commands
available on the Android phone.

## Core Goal

- Tamil, English, and mixed Tamil-English voice interaction.
- Multiple wake names for the assistant.
- Owner-first behavior: Jarvis knows the boss, title, preferences, and language.
- Voice replies by default, with text logs stored silently for learning.
- Android actions such as calling contacts, opening apps, and later deeper
  automation through personal-only tools.
- Optional laptop brain for local SLM inference, fine-tuning, datasets, memory,
  and smart-home control.
- No OpenAI, Gemini, or paid cloud model dependency for the SLM.

## Project Layout

```text
android_app/       Android mobile application.
server/            Local laptop server, model runner, training, memory APIs.
shared/            Schemas, command definitions, and language rules shared by both.
data/              Local datasets, logs, memory exports, and model outputs.
docs/              Architecture, roadmap, Android limits, and memory design.
scripts/           Setup, training, evaluation, and sync helpers.
tests/             Command, server, and Android test areas.
```

## First Build Target

```text
You say: "Jarvis call appa"
Phone detects wake name
Phone converts speech to text
Phone/server maps the command to call_contact
Phone finds the contact alias "appa"
Phone starts the call
Jarvis replies by voice
```

## Voice Preview

On Windows, after the server is running, Jarvis can speak a response aloud:

```powershell
.\scripts\voice\speak_jarvis_response.ps1 -Text "Jarvis call appa"
```

This is an early laptop voice preview. The Android app will later speak the same
`spoken_response` using Android Text-to-Speech.
