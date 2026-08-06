from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, Header, HTTPException, Request

from app.core.config import get_settings
from app.services.subscription_service import sync_revenuecat_event

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/revenuecat")
async def revenuecat_webhook(
    request: Request,
    authorization: str | None = Header(default=None),
) -> dict[str, Any]:
    settings = get_settings()
    secret = settings.revenuecat_webhook_secret
    if secret:
        expected = secret if secret.startswith("Bearer ") else f"Bearer {secret}"
        if authorization != expected and authorization != secret:
            raise HTTPException(status_code=401, detail="Invalid webhook authorization")

    payload = await request.json()
    event = payload.get("event") if isinstance(payload.get("event"), dict) else payload
    if not isinstance(event, dict):
        raise HTTPException(status_code=400, detail="Invalid RevenueCat payload")

    try:
        result = await sync_revenuecat_event(event)
        logger.info("RevenueCat webhook processed: %s", result.get("status"))
        return result
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:  # noqa: BLE001
        logger.exception("RevenueCat webhook failed")
        raise HTTPException(status_code=500, detail="Webhook processing failed") from exc
