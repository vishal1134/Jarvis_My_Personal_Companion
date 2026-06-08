# Architecture

## Product Shape

Jarvis is a mobile-first hybrid assistant.

```text
Android phone = ears, voice, quick commands, phone actions
Laptop server = local SLM brain, training, memory, smart-home bridge
Home devices = later controlled through Home Assistant, MQTT, or custom APIs
```

The phone must remain useful when the laptop is off or Wi-Fi is unavailable.
The laptop adds intelligence and training power when reachable.

## High-Level Flow

```text
Wake name detected
  -> record command
  -> speech-to-text
  -> language/style detection
  -> command understanding
  -> action routing
  -> execute action
  -> voice response
  -> memory/log update
```

## Android App Modules

```text
android_app/
  app/             Android entry point and Compose UI.
  core/            Dependency wiring, app lifecycle, common utilities.
  voice/           Wake names, speech-to-text, text-to-speech, speaker profile.
  actions/         Calls, contacts, app launching, Wi-Fi requests, device actions.
  memory/          Local encrypted memory and command cache.
  security/        Owner/trusted speaker checks and permission rules.
  ui/              Settings, memory editor, command review screen.
  integrations/    Laptop server, Tasker/Shizuku/Home Assistant bridges.
```

## Laptop Server Modules

```text
server/
  api/             FastAPI routes used by the Android app and dashboard.
  brain/           Local SLM inference, command parsing, response planning.
  training/        Dataset validation, fine-tuning, evaluation, export.
  memory/          Larger memory store and semantic search.
  datasets/        Dataset format adapters and examples.
  models/          Local model registry and runtime configuration.
  evaluations/     Command accuracy and regression tests.
  integrations/    Home Assistant, MQTT, and future automation bridges.
```

## Shared Contract

Phone and server communicate using structured commands, not raw free-form text
after understanding.

```json
{
  "intent": "call_contact",
  "target": "appa",
  "detected_language": "ta-en",
  "response_language": "ta",
  "speaker_role": "owner",
  "requires_confirmation": false
}
```

## Local-First Principle

No paid cloud model is required for the SLM. The project can use open-source
models, local inference, and local fine-tuning on the laptop.

