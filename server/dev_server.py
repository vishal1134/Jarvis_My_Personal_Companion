from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from server.brain.session import handle_text
from server.memory.store import MemoryStore


class JarvisDevHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        if self.path == "/health":
            self._send_json({"status": "ok"})
            return

        if self.path == "/memory":
            self._send_json(MemoryStore.default().load())
            return

        self._send_json({"error": "not_found"}, status=404)

    def do_POST(self) -> None:
        if self.path != "/commands/handle":
            self._send_json({"error": "not_found"}, status=404)
            return

        body = self.rfile.read(int(self.headers.get("Content-Length", "0")))
        try:
            payload = json.loads(body.decode("utf-8"))
            text = payload["text"]
        except (json.JSONDecodeError, KeyError):
            self._send_json({"error": "Expected JSON body with text field."}, status=400)
            return

        self._send_json(handle_text(text))

    def log_message(self, format: str, *args: object) -> None:
        return

    def _send_json(self, payload: dict[str, object], status: int = 200) -> None:
        body = json.dumps(payload, ensure_ascii=False, indent=2).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def run() -> None:
    server = ThreadingHTTPServer(("127.0.0.1", 8000), JarvisDevHandler)
    print("Jarvis dev server running at http://127.0.0.1:8000")
    print("Press Ctrl+C to stop.")
    server.serve_forever()


if __name__ == "__main__":
    run()

