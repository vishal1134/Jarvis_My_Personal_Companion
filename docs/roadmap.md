# Roadmap

## Phase 1: Clean Foundation

- Create project structure.
- Define shared command schema.
- Define memory design.
- Define local SLM plan.

## Phase 2: Android MVP

- Wake-name settings.
- Voice reply using Android Text-to-Speech.
- Basic command parser.
- Call contact action.
- Open app action.
- First greeting of the day.
- Owner name and title memory.
- Laptop voice preview script. Done for MVP support.

## Phase 3: Local Server

- FastAPI server. Done for MVP.
- Health check from Android app. Done for MVP.
- Command understanding endpoint. Done for MVP.
- Local memory API. Done for MVP.
- Daily greeting and spoken-response generation. Done for MVP.
- Dataset upload endpoint.

## Phase 4: Learning

- Store commands and responses.
- Build frequent command cache.
- Add correction learning.
- Add bilingual command examples.

## Phase 5: Local Model

- Run a small open-source model locally.
- Fine-tune or train intent parser locally.
- Evaluate model before deployment.
- Sync fast command mappings to phone.

## Phase 6: Smart Home

- Home Assistant or MQTT integration.
- Device registry.
- Routines such as night mode and movie mode.

## Phase 7: Deeper Android Automation

- Notification listener.
- Accessibility service for personal automation.
- Shizuku/ADB bridge if needed.
- Root-only features only if a real blocker appears.
