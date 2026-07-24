from __future__ import annotations

import json
from copy import deepcopy
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any


DEFAULT_MEMORY: dict[str, Any] = {
    "identity": {
        "owner": {
            "name": "Boss",
            "title": "sir",
            "preferred_response_language": "auto",
        },
        "assistant": {
            "active_names": ["jarvis"],
            "inactive_old_names": [],
            "max_active_names": 3,
            "is_awake": True,
        },
    },
    "wifi_credentials": {},
    "daily": {
        "last_greeting_date": None,
        "last_interaction_at": None,
    },
    "frequent_commands": {},
    "conversation_log": [],
}


@dataclass
class MemoryStore:
    path: Path

    @classmethod
    def default(cls) -> "MemoryStore":
        root = Path(__file__).resolve().parents[2]
        return cls(root / "data" / "memory" / "jarvis_memory.json")

    def load(self) -> dict[str, Any]:
        if not self.path.exists():
            return deepcopy(DEFAULT_MEMORY)

        with self.path.open("r", encoding="utf-8") as file:
            stored = json.load(file)

        return _deep_merge(deepcopy(DEFAULT_MEMORY), stored)

    def save(self, memory: dict[str, Any]) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        with self.path.open("w", encoding="utf-8") as file:
            json.dump(memory, file, indent=2, ensure_ascii=False)
            file.write("\n")

    def update_identity(
        self,
        owner_name: str | None = None,
        owner_title: str | None = None,
        preferred_response_language: str | None = None,
    ) -> dict[str, Any]:
        memory = self.load()
        owner = memory["identity"]["owner"]

        if owner_name:
            owner["name"] = owner_name
        if owner_title:
            owner["title"] = owner_title
        if preferred_response_language:
            owner["preferred_response_language"] = preferred_response_language

        self.save(memory)
        return memory

    def change_assistant_name(self, new_name: str) -> dict[str, Any]:
        memory = self.load()
        assistant = memory["identity"]["assistant"]
        old_active_names = assistant["active_names"]
        old_names = assistant["inactive_old_names"]

        for old_name in old_active_names:
            if old_name != new_name and old_name not in old_names:
                old_names.append(old_name)

        assistant["active_names"] = [new_name]
        assistant["inactive_old_names"] = old_names[-20:]
        self.save(memory)
        return memory

    def add_assistant_name(self, name: str) -> tuple[dict[str, Any], bool]:
        memory = self.load()
        assistant = memory["identity"]["assistant"]
        active_names = assistant["active_names"]
        max_active_names = assistant.get("max_active_names", 3)

        if name in active_names:
            return memory, True

        if len(active_names) >= max_active_names:
            return memory, False

        active_names.append(name)
        if name in assistant["inactive_old_names"]:
            assistant["inactive_old_names"].remove(name)

        self.save(memory)
        return memory, True

    def remove_assistant_name(self, name: str) -> tuple[dict[str, Any], bool]:
        memory = self.load()
        assistant = memory["identity"]["assistant"]
        active_names = assistant["active_names"]

        if name not in active_names or len(active_names) <= 1:
            return memory, False

        active_names.remove(name)
        if name not in assistant["inactive_old_names"]:
            assistant["inactive_old_names"].append(name)

        self.save(memory)
        return memory, True

    def set_assistant_awake(self, is_awake: bool) -> dict[str, Any]:
        memory = self.load()
        memory["identity"]["assistant"]["is_awake"] = is_awake
        self.save(memory)
        return memory

    def remember_wifi_credentials(self, ssid: str, password: str) -> dict[str, Any]:
        memory = self.load()
        memory["wifi_credentials"][ssid] = {
            "ssid": ssid,
            "password": password,
        }
        self.save(memory)
        return memory

    def find_cached_command(self, source_text: str, response_language: str) -> dict[str, Any] | None:
        normalized_source = _normalize_phrase(source_text)
        memory = self.load()

        for cached in memory["frequent_commands"].values():
            for variant in cached.get("phrase_variants", []):
                if _normalize_phrase(variant) == normalized_source:
                    return {
                        "intent": cached["intent"],
                        "source_text": source_text,
                        "target": cached.get("target"),
                        "detected_language": cached.get("detected_language", "unknown"),
                        "response_language": (
                            response_language
                            if response_language != "auto"
                            else cached.get("response_language", "en")
                        ),
                        "speaker_role": "unknown",
                        "requires_confirmation": False,
                        "slots": {
                            "cache_hit": "true",
                        },
                    }

        return None

    def last_spoken_response(self) -> str | None:
        memory = self.load()
        for item in reversed(memory["conversation_log"]):
            response = item.get("spoken_response")
            command = item.get("command", {})
            if response and command.get("intent") != "repeat_last_response":
                return response
        return None

    def record_interaction(
        self,
        source_text: str,
        command: dict[str, Any],
        spoken_response: str,
        now: datetime,
    ) -> dict[str, Any]:
        memory = self.load()
        timestamp = now.isoformat(timespec="seconds")

        memory["daily"]["last_interaction_at"] = timestamp
        memory["conversation_log"].append(
            {
                "timestamp": timestamp,
                "source_text": source_text,
                "command": command,
                "spoken_response": spoken_response,
            }
        )
        memory["conversation_log"] = memory["conversation_log"][-200:]

        cache_key = _command_cache_key(command)
        if cache_key:
            cached = memory["frequent_commands"].setdefault(
                cache_key,
                {
                    "intent": command["intent"],
                    "target": command.get("target"),
                    "detected_language": command.get("detected_language", "unknown"),
                    "response_language": command.get("response_language", "en"),
                    "usage_count": 0,
                    "last_used_at": None,
                    "phrase_variants": [],
                },
            )
            cached["usage_count"] += 1
            cached["last_used_at"] = timestamp
            if source_text not in cached["phrase_variants"]:
                cached["phrase_variants"].append(source_text)
                cached["phrase_variants"] = cached["phrase_variants"][-10:]

        self.save(memory)
        return memory

    def should_greet(self, today: str) -> bool:
        memory = self.load()
        return memory["daily"].get("last_greeting_date") != today

    def mark_greeted(self, today: str) -> None:
        memory = self.load()
        memory["daily"]["last_greeting_date"] = today
        self.save(memory)


def _command_cache_key(command: dict[str, Any]) -> str | None:
    intent = command.get("intent")
    target = command.get("target")
    cacheable_intents = {
        "call_contact",
        "open_app",
        "read_notifications",
        "search_youtube",
        "open_system_settings",
        "set_flashlight",
        "query_notifications",
        "set_bluetooth_state",
        "connect_bluetooth",
    }
    if not intent or intent not in cacheable_intents:
        return None
    return f"{intent}:{target or ''}"


def _normalize_phrase(phrase: str) -> str:
    return " ".join(phrase.lower().strip().split())


def _deep_merge(base: dict[str, Any], updates: dict[str, Any]) -> dict[str, Any]:
    for key, value in updates.items():
        if isinstance(value, dict) and isinstance(base.get(key), dict):
            _deep_merge(base[key], value)
        else:
            base[key] = value
    return base
