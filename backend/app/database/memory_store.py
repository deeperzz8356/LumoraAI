from __future__ import annotations

from copy import deepcopy
from datetime import datetime, timezone
from threading import Lock
from typing import Any


class MemoryUserStore:
    """Thread-safe in-memory user store used when Firestore is not configured."""

    def __init__(self) -> None:
        self._lock = Lock()
        self._users: dict[str, dict[str, Any]] = {}
        self._processed_events: set[str] = set()

    def get_user(self, user_id: str) -> dict[str, Any]:
        with self._lock:
            return deepcopy(self._users.get(user_id, {"user_id": user_id, "credits": 0}))

    def upsert_user(self, user_id: str, fields: dict[str, Any]) -> dict[str, Any]:
        with self._lock:
            current = self._users.get(user_id, {"user_id": user_id, "credits": 0})
            current.update(fields)
            current["updated_at"] = datetime.now(timezone.utc).isoformat()
            self._users[user_id] = current
            return deepcopy(current)

    def mark_event_processed(self, event_id: str) -> bool:
        """Return True if this is the first time we see the event (not a duplicate)."""
        with self._lock:
            if event_id in self._processed_events:
                return False
            self._processed_events.add(event_id)
            return True


USER_STORE = MemoryUserStore()
