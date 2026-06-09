from __future__ import annotations


def prepare_for_speech(text: str) -> str:
    """Normalize text before sending it to a text-to-speech engine."""
    return " ".join(text.strip().split())

