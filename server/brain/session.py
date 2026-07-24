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
    segments = _split_multi_command(text)
    if len(segments) > 1:
        results = [_handle_single_text(segment, memory_store, current_time) for segment in segments]
        spoken_parts = [str(result["spoken_response"]) for result in results if result.get("spoken_response")]
        return {
            "command": results[0]["command"],
            "commands": [result["command"] for result in results],
            "spoken_response": " ".join(spoken_parts),
            "spoken_responses": spoken_parts,
            "should_speak": any(bool(result.get("should_speak")) for result in results),
            "greeting": results[0].get("greeting"),
        }

    return _handle_single_text(text, memory_store, current_time)


def _handle_single_text(
    text: str,
    memory_store: MemoryStore,
    current_time: datetime,
) -> dict[str, object]:
    memory = memory_store.load()
    owner = memory["identity"]["owner"]
    assistant = memory["identity"]["assistant"]

    preferred_language = owner.get("preferred_response_language", "auto")
    wake_names = assistant.get("active_names", ["jarvis"])
    wake_name_used = _used_active_name(text, wake_names)
    old_name = _used_inactive_old_name(text, assistant.get("inactive_old_names", []), wake_names)

    if not assistant.get("is_awake", True) and not wake_name_used:
        return {
            "command": {
                "intent": "ignored_while_asleep",
                "source_text": text,
                "target": None,
                "detected_language": "unknown",
                "response_language": preferred_language if preferred_language != "auto" else "en",
                "speaker_role": "unknown",
                "requires_confirmation": False,
                "slots": {},
            },
            "spoken_response": "",
            "should_speak": False,
            "greeting": None,
        }

    cached_command = None
    if not old_name:
        cached_command = memory_store.find_cached_command(text, preferred_language)

    if old_name:
        command = {
            "intent": "old_assistant_name_used",
            "source_text": text,
            "target": old_name,
            "detected_language": "en",
            "response_language": preferred_language if preferred_language != "auto" else "en",
            "speaker_role": "unknown",
            "requires_confirmation": False,
            "slots": {
                "active_name": wake_names[0] if wake_names else "jarvis",
            },
        }
        parsed = None
    elif cached_command:
        command = cached_command
        parsed = None
    else:
        parsed = parse_command(text, preferred_language, wake_names=wake_names)
        command = parsed.to_dict()

    if parsed and parsed.intent == "set_response_language" and parsed.target:
        memory = memory_store.update_identity(preferred_response_language=parsed.target)
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]

    if parsed and parsed.intent == "change_assistant_name" and parsed.target:
        memory = memory_store.change_assistant_name(_normalize_name(parsed.target))
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]

    if parsed and parsed.intent == "add_assistant_name" and parsed.target:
        memory, added = memory_store.add_assistant_name(_normalize_name(parsed.target))
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]
        command["slots"]["name_update_success"] = str(added).lower()

    if parsed and parsed.intent == "remove_assistant_name" and parsed.target:
        memory, removed = memory_store.remove_assistant_name(_normalize_name(parsed.target))
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]
        command["slots"]["name_update_success"] = str(removed).lower()

    if command["intent"] == "wake_assistant":
        memory = memory_store.set_assistant_awake(True)
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]

    if command["intent"] == "sleep_assistant":
        memory = memory_store.set_assistant_awake(False)
        owner = memory["identity"]["owner"]
        assistant = memory["identity"]["assistant"]

    if command["intent"] == "connect_wifi" and command.get("target") and command.get("slots", {}).get("password"):
        memory_store.remember_wifi_credentials(str(command["target"]), str(command["slots"]["password"]))

    if command["intent"] == "repeat_last_response":
        command["slots"]["repeat_text"] = memory_store.last_spoken_response() or ""

    if command["intent"] == "get_time":
        command["slots"]["current_time"] = current_time.strftime("%I:%M %p").lstrip("0")

    if command["intent"] == "get_date":
        command["slots"]["current_date"] = current_time.strftime("%A, %d %B %Y")

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


def _used_inactive_old_name(text: str, old_names: list[str], active_names: list[str]) -> str | None:
    normalized = " ".join(text.lower().strip().split())
    active = {name.lower().strip() for name in active_names}
    for old_name in old_names:
        old = old_name.lower().strip()
        if old in active:
            continue
        if normalized == old or normalized.startswith(old + " "):
            return old_name
    return None


def _split_multi_command(text: str) -> list[str]:
    normalized = " ".join(text.strip().split())
    lowered = normalized.lower()
    separators = (" and then ", " then ", " and ", " appuram ", " apram ", " athukapram ")
    for separator in separators:
        if separator not in lowered:
            continue

        parts: list[str] = []
        remaining_original = normalized
        remaining_lowered = lowered
        while separator in remaining_lowered:
            index = remaining_lowered.index(separator)
            parts.append(remaining_original[:index].strip())
            remaining_original = remaining_original[index + len(separator) :].strip()
            remaining_lowered = remaining_lowered[index + len(separator) :].strip()
        parts.append(remaining_original.strip())
        return [part for part in parts if part]

    return [normalized]


def _used_active_name(text: str, active_names: list[str]) -> str | None:
    normalized = " ".join(text.lower().strip().split())
    for active_name in active_names:
        name = active_name.lower().strip()
        if normalized == name or normalized.startswith(name + " ") or normalized.endswith(" " + name):
            return active_name
    return None


def _normalize_name(name: str) -> str:
    return " ".join(name.lower().strip().split())
