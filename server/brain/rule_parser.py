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


def parse_command(text: str, response_language: str = "auto") -> ParsedCommand:
    source = text.strip()
    normalized = " ".join(source.lower().split())
    detected_language = detect_language(normalized)
    final_response_language = (
        detected_language if response_language == "auto" and detected_language != "unknown" else response_language
    )
    if final_response_language == "unknown":
        final_response_language = "en"

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

    call_target = _extract_call_target(normalized)
    if call_target:
        return ParsedCommand(
            intent="call_contact",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=call_target,
        )

    app_target = _extract_app_target(normalized)
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


def _extract_call_target(text: str) -> str | None:
    cleaned = _remove_wake_name(text)

    if "ku call pannu" in cleaned:
        target = cleaned.split("ku call pannu", 1)[0].strip()
        return target or None

    for keyword in ("call ", "call pannu", "koopidu"):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _extract_app_target(text: str) -> str | None:
    cleaned = _remove_wake_name(text)

    if "open pannu" in cleaned:
        target = cleaned.split("open pannu", 1)[0].strip()
        return target or None

    for keyword in ("open ", "thirakku"):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _remove_wake_name(text: str) -> str:
    for wake_name in ("jarvis",):
        if text.startswith(wake_name + " "):
            return text[len(wake_name) + 1 :].strip()
    return text
