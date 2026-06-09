# Manual Test

If Python is not installed globally, Codex can still run the dependency-free dev
server using its bundled Python runtime.

## Test Endpoints

Health:

```text
GET http://127.0.0.1:8000/health
```

Handle command:

```text
POST http://127.0.0.1:8000/commands/handle
```

Body:

```json
{
  "text": "Jarvis call appa"
}
```

Expected response includes:

```json
{
  "spoken_response": "Good morning, sir. Calling appa, sir.",
  "should_speak": true
}
```

Read memory:

```text
GET http://127.0.0.1:8000/memory
```

## Proper Laptop Setup Later

Install Python 3.11 or newer from:

```text
https://www.python.org/downloads/
```

During installation, enable:

```text
Add python.exe to PATH
```

