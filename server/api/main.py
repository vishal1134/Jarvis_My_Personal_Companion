from __future__ import annotations

try:
    from fastapi import FastAPI
    from pydantic import BaseModel
except ImportError:  # Allows local parser tests before dependencies are installed.
    FastAPI = None
    BaseModel = object

from server.brain.rule_parser import parse_command
from server.brain.session import handle_text
from server.memory.store import MemoryStore


if FastAPI:
    app = FastAPI(title="Jarvis Local Server")

    class ParseRequest(BaseModel):
        text: str
        response_language: str = "auto"

    class HandleRequest(BaseModel):
        text: str

    class IdentityRequest(BaseModel):
        owner_name: str | None = None
        owner_title: str | None = None
        preferred_response_language: str | None = None

    @app.get("/health")
    def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.post("/commands/parse")
    def parse(request: ParseRequest) -> dict[str, object]:
        return parse_command(request.text, request.response_language).to_dict()

    @app.post("/commands/handle")
    def handle(request: HandleRequest) -> dict[str, object]:
        return handle_text(request.text)

    @app.get("/memory")
    def read_memory() -> dict[str, object]:
        return MemoryStore.default().load()

    @app.post("/memory/identity")
    def update_identity(request: IdentityRequest) -> dict[str, object]:
        memory = MemoryStore.default().update_identity(
            owner_name=request.owner_name,
            owner_title=request.owner_title,
            preferred_response_language=request.preferred_response_language,
        )
        return memory["identity"]
