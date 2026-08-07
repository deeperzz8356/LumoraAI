from __future__ import annotations

from typing import Any

from app.services.billing_service import billing_service


async def get_subscription_status(user_id: str) -> dict[str, Any]:
    return billing_service.get_subscription_status(user_id)


async def activate_subscription(user_id: str, plan_code: str) -> dict[str, Any]:
    return billing_service.activate_subscription(user_id, plan_code)


async def sync_revenuecat_event(event: dict[str, Any]) -> dict[str, Any]:
    return billing_service.sync_from_revenuecat_event(event)
