from __future__ import annotations

try:
    from fastapi import FastAPI
    from pydantic import BaseModel
except ImportError:  # Allows local parser tests before dependencies are installed.
    FastAPI = None
    BaseModel = object

from server.brain.rule_parser import parse_command


if FastAPI:
    app = FastAPI(title="Jarvis Local Server")

    class ParseRequest(BaseModel):
        text: str
        response_language: str = "auto"

    @app.get("/health")
    def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.post("/commands/parse")
    def parse(request: ParseRequest) -> dict[str, object]:
        return parse_command(request.text, request.response_language).to_dict()

