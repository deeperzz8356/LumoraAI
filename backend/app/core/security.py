from __future__ import annotations

from typing import Annotated

from fastapi import Depends, Header, HTTPException


async def get_current_user(
    authorization: Annotated[str | None, Header()] = None,
    x_user_id: Annotated[str | None, Header()] = None,
) -> str:
    """
    Resolve the authenticated user id.

    Prefer Firebase Bearer token (Authorization). For local/dev, fall back to
    x-user-id only when a Bearer token is present (token format validated lightly).
    """
    if authorization and authorization.lower().startswith("bearer "):
        token = authorization.split(" ", 1)[1].strip()
        if not token:
            raise HTTPException(status_code=401, detail="Missing bearer token")
        # Full Firebase Admin verification can be plugged in here.
        # Until Admin SDK is configured, require x-user-id with the token.
        if x_user_id:
            return x_user_id
        # Anonymous token path — refuse unverified activate without user id.
        raise HTTPException(
            status_code=401,
            detail="Authenticated user id required (x-user-id with Bearer token)",
        )

    raise HTTPException(status_code=401, detail="Authorization Bearer token required")


CurrentUser = Annotated[str, Depends(get_current_user)]
