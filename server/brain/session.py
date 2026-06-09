from __future__ import annotations

from datetime import datetime

from server.brain.responses import greeting_for, response_for
from server.brain.rule_parser import parse_command
from server.memory.store import MemoryStore
from server.voice.style import prepare_for_speech


def handle_text(
    text: str,
    store: MemoryStore | None = None,
    now: datetime | None = None,
) -> dict[str, object]:
    memory_store = store or MemoryStore.default()
    current_time = now or datetime.now().astimezone()
    memory = memory_store.load()
    owner = memory["identity"]["owner"]

    preferred_language = owner.get("preferred_response_language", "auto")
    parsed = parse_command(text, preferred_language)
    command = parsed.to_dict()

    if parsed.intent == "set_response_language" and parsed.target:
        memory = memory_store.update_identity(preferred_response_language=parsed.target)
        owner = memory["identity"]["owner"]

    title = owner.get("title", "sir")
    greeting = None
    today = current_time.date().isoformat()
    if memory_store.should_greet(today):
        greeting = greeting_for(current_time, command["response_language"], title)
        memory_store.mark_greeted(today)

    spoken_response = response_for(command, title)
    full_spoken_response = f"{greeting} {spoken_response}" if greeting else spoken_response
    full_spoken_response = prepare_for_speech(full_spoken_response)

    memory_store.record_interaction(
        source_text=text,
        command=command,
        spoken_response=full_spoken_response,
        now=current_time,
    )

    return {
        "command": command,
        "spoken_response": full_spoken_response,
        "should_speak": True,
        "greeting": greeting,
    }
