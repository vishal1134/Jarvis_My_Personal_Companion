# Tech Stack

## Rules For This Project

- No OpenAI API.
- No Gemini API.
- Prefer free and local tools.
- Phone must work for core commands without laptop.
- Laptop can provide stronger model inference and training when available.

## Android App

Primary stack:

- Kotlin
- Jetpack Compose
- Room database
- Jetpack Security for encrypted local storage
- Android Text-to-Speech for first voice replies
- Android contacts, call, app-launch, notification, and accessibility APIs

Voice options:

- Android speech recognition for first prototype
- Vosk or Whisper.cpp for offline speech-to-text later
- Android Text-to-Speech first
- Piper TTS later for better local voice

Wake name options:

- Porcupine, if its free tier fits personal use
- openWakeWord, if we want a more open local route
- Simple push-to-talk fallback during early development

## Laptop Server

Primary stack:

- Python
- FastAPI
- SQLite first
- PyTorch
- Hugging Face Transformers
- PEFT / LoRA for local fine-tuning
- llama.cpp or Ollama for local inference

Why SQLite first:

- Simple.
- Local.
- Easy backup.
- Enough for one-person use.

## Model Plan

Recommended first path:

```text
Rules + command cache
  -> lightweight intent parser
  -> small open-source instruct model
  -> local LoRA fine-tuning
```

Candidate model families:

- Qwen2.5 0.5B or 1.5B
- TinyLlama 1.1B
- Other small open models after checking license and laptop performance

## Smart Home

Recommended:

- Home Assistant
- MQTT
- ESP32 for custom devices

## Data Formats

- JSONL for training examples.
- JSON Schema for phone/server command contracts.
- SQLite for memory.
- Encrypted storage for Wi-Fi passwords and private tokens.

