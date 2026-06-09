from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class ParsedCommand:
    intent: str
    source_text: str
    detected_language: str
    response_language: str
    target: str | None = None
    speaker_role: str = "unknown"
    requires_confirmation: bool = False
    slots: dict[str, str] = field(default_factory=dict)

    def to_dict(self) -> dict[str, object]:
        return {
            "intent": self.intent,
            "source_text": self.source_text,
            "target": self.target,
            "detected_language": self.detected_language,
            "response_language": self.response_language,
            "speaker_role": self.speaker_role,
            "requires_confirmation": self.requires_confirmation,
            "slots": self.slots,
        }


TAMIL_HINTS = (
    "pannu",
    "pannunga",
    "ku",
    "la",
    "pesu",
    "koopidu",
    "thirakku",
)


def detect_language(text: str) -> str:
    normalized = text.lower()
    has_tamil_hint = any(hint in normalized for hint in TAMIL_HINTS) or any(
        _is_tamil_character(ch) for ch in normalized
    )
    has_english_hint = any(ch.isascii() and ch.isalpha() for ch in normalized)

    if has_tamil_hint and has_english_hint:
        return "ta-en"
    if has_tamil_hint:
        return "ta"
    if has_english_hint:
        return "en"
    return "unknown"


def _is_tamil_character(character: str) -> bool:
    return "\u0b80" <= character <= "\u0bff"


def parse_command(
    text: str,
    response_language: str = "auto",
    wake_names: list[str] | None = None,
) -> ParsedCommand:
    source = text.strip()
    normalized = " ".join(source.lower().split())
    cleaned = _remove_wake_name(normalized, wake_names)
    detected_language = detect_language(normalized)
    final_response_language = (
        detected_language if response_language == "auto" and detected_language != "unknown" else response_language
    )
    if final_response_language == "unknown":
        final_response_language = "en"

    name_command = _extract_name_command(cleaned)
    if name_command:
        intent, target = name_command
        return ParsedCommand(
            intent=intent,
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=target,
        )

    if "reply in tamil" in normalized or "tamil la reply" in normalized:
        return ParsedCommand(
            intent="set_response_language",
            source_text=source,
            detected_language=detected_language,
            response_language="ta",
            target="ta",
        )

    if "reply in english" in normalized or "english la reply" in normalized:
        return ParsedCommand(
            intent="set_response_language",
            source_text=source,
            detected_language=detected_language,
            response_language="en",
            target="en",
        )

    if _is_read_notifications_command(normalized, wake_names):
        return ParsedCommand(
            intent="read_notifications",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
        )

    youtube_query = _extract_youtube_search_query(normalized, wake_names)
    if youtube_query:
        return ParsedCommand(
            intent="search_youtube",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=youtube_query,
        )

    call_target = _extract_call_target(normalized, wake_names)
    if call_target:
        return ParsedCommand(
            intent="call_contact",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=call_target,
        )

    app_target = _extract_app_target(normalized, wake_names)
    if app_target:
        return ParsedCommand(
            intent="open_app",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=app_target,
        )

    return ParsedCommand(
        intent="unknown",
        source_text=source,
        detected_language=detected_language,
        response_language=final_response_language,
        requires_confirmation=True,
    )


def _extract_call_target(text: str, wake_names: list[str] | None = None) -> str | None:
    cleaned = _remove_wake_name(text, wake_names)

    if "ku call pannu" in cleaned:
        target = cleaned.split("ku call pannu", 1)[0].strip()
        return target or None

    for keyword in ("call ", "call pannu", "koopidu"):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _is_read_notifications_command(text: str, wake_names: list[str] | None = None) -> bool:
    cleaned = _remove_wake_name(text, wake_names)
    return (
        "read notifications" in cleaned
        or "notification read" in cleaned
        or "notifications read" in cleaned
        or "notification padi" in cleaned
        or "notifications padi" in cleaned
    )


def _extract_app_target(text: str, wake_names: list[str] | None = None) -> str | None:
    cleaned = _remove_wake_name(text, wake_names)

    if "open pannu" in cleaned:
        target = cleaned.split("open pannu", 1)[0].strip()
        return target or None

    for keyword in ("open ", "thirakku"):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _extract_youtube_search_query(text: str, wake_names: list[str] | None = None) -> str | None:
    cleaned = _remove_wake_name(text, wake_names)

    if cleaned.startswith("youtube search "):
        return cleaned.removeprefix("youtube search ").strip() or None

    if " search in youtube" in cleaned:
        return cleaned.split(" search in youtube", 1)[0].strip() or None

    if cleaned.startswith("search ") and " in youtube" in cleaned:
        query = cleaned.removeprefix("search ").split(" in youtube", 1)[0].strip()
        return query or None

    if " youtube la " in cleaned and " search pannu" in cleaned:
        query = cleaned.split(" youtube la ", 1)[1].split(" search pannu", 1)[0].strip()
        return query or None

    if cleaned.startswith("youtube la ") and " search pannu" in cleaned:
        query = cleaned.removeprefix("youtube la ").split(" search pannu", 1)[0].strip()
        return query or None

    return None


def _extract_name_command(text: str) -> tuple[str, str] | None:
    for phrase in ("change your name to ", "rename yourself to ", "your name is now "):
        if phrase in text:
            target = text.split(phrase, 1)[1].strip()
            return ("change_assistant_name", target) if target else None

    if text.startswith("add ") and " as your name" in text:
        target = text.removeprefix("add ").split(" as your name", 1)[0].strip()
        return ("add_assistant_name", target) if target else None

    if text.startswith("remove "):
        target = text.removeprefix("remove ").strip()
        return ("remove_assistant_name", target) if target else None

    return None


def _remove_wake_name(text: str, wake_names: list[str] | None = None) -> str:
    names = wake_names or ["jarvis"]
    for wake_name in names:
        wake_name = wake_name.lower().strip()
        if text.startswith(wake_name + " "):
            return text[len(wake_name) + 1 :].strip()
    return text
