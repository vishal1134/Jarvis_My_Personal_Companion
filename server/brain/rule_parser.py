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
    "thira",
    "sollu",
    "sollunga",
    "enna",
    "ena",
    "neram",
    "thethi",
    "indru",
    "udhavi",
    "niruthu",
    "podu",
    "venum",
    "mudi",
    "mudiyum",
    "irukiya",
    "irukingala",
    "vandhu",
    "password",
    "connect",
    "yaaru",
    "message",
    "messaged",
    "pannirukaa",
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

    wifi_connection = _extract_wifi_connection(cleaned)
    if wifi_connection:
        ssid, password = wifi_connection
        slots = {}
        if password:
            slots["password"] = password
        return ParsedCommand(
            intent="connect_wifi",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=ssid,
            slots=slots,
        )

    notification_query = _extract_notification_query(cleaned)
    if notification_query:
        return ParsedCommand(
            intent="query_notifications",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=notification_query,
        )

    bluetooth_connection = _extract_bluetooth_connection(cleaned)
    if bluetooth_connection:
        return ParsedCommand(
            intent="connect_bluetooth",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=bluetooth_connection,
        )

    simple_intent = _extract_simple_local_intent(cleaned)
    if simple_intent:
        intent, target = simple_intent
        return ParsedCommand(
            intent=intent,
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=target,
        )

    wifi_state = _extract_wifi_state(cleaned)
    if wifi_state:
        return ParsedCommand(
            intent="set_wifi_state",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=wifi_state,
        )

    bluetooth_state = _extract_bluetooth_state(cleaned)
    if bluetooth_state:
        return ParsedCommand(
            intent="set_bluetooth_state",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=bluetooth_state,
        )

    system_setting = _extract_system_setting(cleaned)
    if system_setting:
        return ParsedCommand(
            intent="open_system_settings",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=system_setting,
        )

    flashlight_state = _extract_flashlight_state(cleaned)
    if flashlight_state:
        return ParsedCommand(
            intent="set_flashlight",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
            target=flashlight_state,
        )

    if _is_sleep_assistant_command(cleaned):
        return ParsedCommand(
            intent="sleep_assistant",
            source_text=source,
            detected_language=detected_language,
            response_language=final_response_language,
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

    if "ku phone pannu" in cleaned:
        target = cleaned.split("ku phone pannu", 1)[0].strip()
        return target or None

    for keyword in ("call ", "call pannu", "phone pannu", "koopidu", "phone podu"):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _is_read_notifications_command(text: str, wake_names: list[str] | None = None) -> bool:
    cleaned = _remove_wake_name(text, wake_names)
    tokens = set(cleaned.split())
    return (
        "read notifications" in cleaned
        or "notification read" in cleaned
        or "notifications read" in cleaned
        or "notification padi" in cleaned
        or "notifications padi" in cleaned
        or "notification sollu" in cleaned
        or "notifications sollu" in cleaned
        or "notification enna" in cleaned
        or "notifications enna" in cleaned
        or "notification ena" in cleaned
        or "notifications ena" in cleaned
        or ("notification" in tokens and {"enna", "ena", "sollu", "padi"} & tokens)
        or ("notifications" in tokens and {"enna", "ena", "sollu", "padi"} & tokens)
    )


def _extract_notification_query(text: str) -> str | None:
    tokens = text.split()
    apps = ("whatsapp", "instagram", "telegram", "gmail", "messages", "phone", "sms")
    app = next((item for item in apps if item in tokens), None)
    if not app:
        return None

    query_words = {"yaaru", "who", "whom", "message", "messaged", "pannirukaa", "panniruka", "notification"}
    if query_words & set(tokens):
        return app

    return None


def _extract_app_target(text: str, wake_names: list[str] | None = None) -> str | None:
    cleaned = _remove_wake_name(text, wake_names)

    for phrase in ("open pannu", "thira", "thirakku"):
        if phrase in cleaned:
            target = cleaned.split(phrase, 1)[0].strip()
            return target or None

    for keyword in ("open ",):
        if keyword in cleaned:
            before, after = cleaned.split(keyword, 1)
            target = after.strip() if after.strip() else before.strip()
            return target or None

    return None


def _extract_youtube_search_query(text: str, wake_names: list[str] | None = None) -> str | None:
    cleaned = _remove_wake_name(text, wake_names)
    cleaned = cleaned.replace("you tube", "youtube")

    if cleaned.startswith("youtube search "):
        return cleaned.removeprefix("youtube search ").strip() or None

    if cleaned.startswith("youtube la ") and " search " in cleaned:
        query = cleaned.removeprefix("youtube la ").split(" search ", 1)[0].strip()
        return query or None

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

    if cleaned.startswith("youtube la ") and " search" in cleaned:
        query = cleaned.removeprefix("youtube la ").split(" search", 1)[0].strip()
        return query or None

    if cleaned.startswith("youtube ") and " search pannu" in cleaned:
        query = cleaned.removeprefix("youtube ").split(" search pannu", 1)[0].strip()
        return query or None

    if cleaned.startswith("youtube la ") and " thedu" in cleaned:
        query = cleaned.removeprefix("youtube la ").split(" thedu", 1)[0].strip()
        return query or None

    if " youtube la " in cleaned and " thedu" in cleaned:
        query = cleaned.split(" youtube la ", 1)[1].split(" thedu", 1)[0].strip()
        return query or None

    return None


def _extract_simple_local_intent(text: str) -> tuple[str, str | None] | None:
    tokens = set(text.split())
    wake_phrases = {
        "",
        "wake up",
        "are you there",
        "hello",
        "hi",
        "online",
        "status",
        "listen",
        "start listening",
        "irukiya",
        "irukingala",
        "kelu",
        "ready ah",
    }
    if text in wake_phrases:
        return "wake_assistant", None

    if {"enna", "ena", "mudiyum"} & tokens and {"panna", "panra", "panva"} & tokens:
        return "list_commands", None

    if text in {
        "help",
        "what can you do",
        "list commands",
        "show commands",
        "commands",
        "udhavi",
        "enna panna mudiyum",
        "enna ellam panna mudiyum",
        "commands sollu",
        "enna panva",
    }:
        return "list_commands", None

    if text in {
        "repeat",
        "repeat that",
        "say that again",
        "again",
        "repeat last response",
        "marubadi sollu",
        "meendum sollu",
        "innoru thadava sollu",
        "thirumba sollu",
    }:
        return "repeat_last_response", None

    if text in {
        "stop",
        "cancel",
        "be quiet",
        "silent",
        "stop speaking",
        "niruthu",
        "pesatha",
        "amaidhi",
        "silent ah iru",
    }:
        return "stop_speaking", None

    if (
        text in {"time", "what time is it", "tell time", "current time", "neram enna", "neram ena", "time sollu", "mani enna", "mani ena"}
        or ("time" in tokens and {"enna", "ena", "sollu"} & tokens)
        or ("neram" in tokens and {"enna", "ena", "sollu"} & tokens)
        or ("mani" in tokens and {"enna", "ena", "sollu"} & tokens)
    ):
        return "get_time", None

    if text in {
        "date",
        "today date",
        "what is the date",
        "current date",
        "date sollu",
        "indru date enna",
        "thethi enna",
        "thethi ena",
        "innaiku date enna",
        "innaiku date ena",
    }:
        return "get_date", None

    if ("date" in tokens and {"enna", "ena", "sollu"} & tokens) or (
        "thethi" in tokens and {"enna", "ena", "sollu"} & tokens
    ):
        return "get_date", None

    return None


def _extract_wifi_state(text: str) -> str | None:
    tokens = set(text.split())
    if "wifi" not in tokens and "wi-fi" not in tokens:
        return None

    if {"on", "enable", "start", "podu"} & tokens or "on pannu" in text:
        return "on"

    if {"off", "disable", "stop", "niruthu"} & tokens or "off pannu" in text:
        return "off"

    return None


def _extract_bluetooth_state(text: str) -> str | None:
    tokens = set(text.split())
    if "bluetooth" not in tokens:
        return None

    if {"on", "enable", "start", "podu"} & tokens or "on pannu" in text:
        return "on"

    if {"off", "disable", "stop", "niruthu"} & tokens or "off pannu" in text:
        return "off"

    return None


def _extract_bluetooth_connection(text: str) -> str | None:
    if "bluetooth" not in text:
        return None

    if "connect" not in text and "connect pannu" not in text and "pair" not in text:
        return None

    cleaned = text.replace("bluetooth", "").strip()
    if cleaned.startswith("connect to "):
        cleaned = cleaned.removeprefix("connect to ").strip()
    if cleaned.startswith("connect "):
        cleaned = cleaned.removeprefix("connect ").strip()
    if " connect pannu" in cleaned:
        cleaned = cleaned.split(" connect pannu", 1)[0].strip()
    if " connect" in cleaned:
        cleaned = cleaned.split(" connect", 1)[0].strip()
    cleaned = cleaned.replace("device", "").strip()
    return cleaned or None


def _extract_wifi_connection(text: str) -> tuple[str, str | None] | None:
    if "wifi" not in text and "wi-fi" not in text:
        return None

    if "connect" not in text and "connect pannu" not in text and "join" not in text:
        return None

    password = None
    ssid_part = text

    for marker in (" wifi password vandhu ", " password vandhu ", " password is ", " password "):
        if marker in text:
            ssid_part, password_part = text.split(marker, 1)
            password = _normalize_spoken_password(password_part)
            break

    ssid = _extract_wifi_ssid(ssid_part)
    if not ssid:
        return None

    return ssid, password


def _extract_wifi_ssid(text: str) -> str | None:
    cleaned = text.replace("wi-fi", "wifi").strip()

    if cleaned.startswith("connect to "):
        cleaned = cleaned.removeprefix("connect to ").strip()

    if cleaned.startswith("connect "):
        cleaned = cleaned.removeprefix("connect ").strip()

    if " connect pannu" in cleaned:
        cleaned = cleaned.split(" connect pannu", 1)[0].strip()

    if " connect" in cleaned:
        cleaned = cleaned.split(" connect", 1)[0].strip()

    cleaned = cleaned.replace("wifi", "").replace("network", "").strip()
    return cleaned or None


def _normalize_spoken_password(text: str) -> str:
    cleaned = " ".join(text.lower().strip().split())
    capital_letters = _extract_capital_letters(cleaned)

    for phrase in ("with ", ""):
        for letter in capital_letters:
            cleaned = cleaned.replace(f"{phrase}{letter} capital letter", "")
            cleaned = cleaned.replace(f"{phrase}{letter} capital", "")
            cleaned = cleaned.replace(f"{phrase}capital {letter}", "")

    replacements = {
        " at the rate ": "@",
        " at rate ": "@",
        " at ": "@",
        " dot ": ".",
        " full stop ": ".",
        " underscore ": "_",
        " dash ": "-",
        " hyphen ": "-",
        " hash ": "#",
        " hashtag ": "#",
        " dollar ": "$",
        " star ": "*",
        " space ": " ",
    }
    padded = f" {cleaned.strip()} "
    for spoken, symbol in replacements.items():
        padded = padded.replace(spoken, symbol)

    password = padded.strip().replace(" ", "")
    for letter in capital_letters:
        password = _capitalize_first_letter(password, letter)
    return password


def _extract_capital_letters(text: str) -> list[str]:
    letters: list[str] = []
    words = text.split()
    for index, word in enumerate(words):
        if word == "capital" and index + 1 < len(words) and len(words[index + 1]) == 1:
            letters.append(words[index + 1])
        if len(word) == 1 and index + 1 < len(words) and words[index + 1] == "capital":
            letters.append(word)
        if len(word) == 1 and index + 2 < len(words) and words[index + 1] == "capital" and words[index + 2] == "letter":
            letters.append(word)
    return letters


def _capitalize_first_letter(value: str, letter: str) -> str:
    index = value.lower().find(letter.lower())
    if index < 0:
        return value
    return value[:index] + value[index].upper() + value[index + 1 :]


def _is_sleep_assistant_command(text: str) -> bool:
    return text in {
        "turn off",
        "go offline",
        "sleep",
        "shutdown",
        "shut down",
        "off aagu",
        "thoongu",
        "rest eduthu",
    }


def _extract_system_setting(text: str) -> str | None:
    tokens = set(text.split())
    if text in {"settings", "open settings", "phone settings", "settings thira", "settings open pannu"}:
        return "settings"

    if text in {
        "wifi",
        "wi-fi",
        "open wifi",
        "wifi settings",
        "connect wifi",
        "connect to wifi",
        "wifi thira",
        "wifi open pannu",
        "wifi settings thira",
    }:
        return "wifi"
    if "wifi" in tokens and {"open", "thira", "pannu", "settings", "connect"} & tokens:
        return "wifi"

    if text in {
        "bluetooth",
        "open bluetooth",
        "bluetooth settings",
        "bluetooth thira",
        "bluetooth open pannu",
        "bluetooth settings thira",
    }:
        return "bluetooth"
    if "bluetooth" in tokens and {"open", "thira", "pannu", "settings"} & tokens:
        return "bluetooth"

    if text in {
        "notification settings",
        "open notification settings",
        "notification settings thira",
        "notification settings open pannu",
    }:
        return "notifications"
    if {"notification", "notifications"} & tokens and {"settings", "open", "thira"} & tokens:
        return "notifications"

    if text in {
        "accessibility settings",
        "open accessibility settings",
        "accessibility settings thira",
        "accessibility open pannu",
    }:
        return "accessibility"
    if "accessibility" in tokens and {"settings", "open", "thira", "pannu"} & tokens:
        return "accessibility"

    return None


def _extract_flashlight_state(text: str) -> str | None:
    tokens = set(text.split())
    on_phrases = {
        "turn on flashlight",
        "flashlight on",
        "torch on",
        "turn on torch",
        "light on",
        "torch on pannu",
        "flashlight on pannu",
        "torch podu",
        "light podu",
    }
    off_phrases = {
        "turn off flashlight",
        "flashlight off",
        "torch off",
        "turn off torch",
        "light off",
        "torch off pannu",
        "flashlight off pannu",
        "torch niruthu",
        "light niruthu",
    }

    if text in on_phrases:
        return "on"
    if text in off_phrases:
        return "off"
    if {"torch", "flashlight", "light"} & tokens and {"on", "podu"} & tokens:
        return "on"
    if {"torch", "flashlight", "light"} & tokens and {"off", "niruthu"} & tokens:
        return "off"
    return None


def _extract_name_command(text: str) -> tuple[str, str] | None:
    for phrase in ("change your name to ", "rename yourself to ", "your name is now "):
        if phrase in text:
            target = text.split(phrase, 1)[1].strip()
            return ("change_assistant_name", target) if target else None

    for phrase in ("un peyar ", "unga peyar "):
        if text.startswith(phrase) and text.endswith("nu vechiko"):
            target = text.removeprefix(phrase).removesuffix("nu vechiko").strip()
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
        if text == wake_name:
            return ""
        if text.startswith(wake_name + " "):
            return text[len(wake_name) + 1 :].strip()
        if text.endswith(" " + wake_name):
            return text[: -(len(wake_name) + 1)].strip()
    return text
