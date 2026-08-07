from __future__ import annotations

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from app.core.security import CurrentUser
from app.services.subscription_plans import get_plan_catalog
from app.services.subscription_service import activate_subscription, get_subscription_status

router = APIRouter()


class SubscribeRequest(BaseModel):
    planCode: str


@router.get("")
@router.get("/status")
async def subscription_status_route(user_id: CurrentUser):
    return await get_subscription_status(user_id)


@router.get("/plans")
async def subscription_plans_route():
    return get_plan_catalog()


@router.post("/activate")
async def activate_subscription_route(body: SubscribeRequest, user_id: CurrentUser):
    """
    Deprecated: unverified client activation is disabled.
    Purchases must go through RevenueCat; server state updates via webhooks.
    """
    raise HTTPException(
        status_code=410,
        detail=(
            "POST /subscriptions/activate is retired. "
            "Use RevenueCat purchases; entitlements sync via /api/v1/webhooks/revenuecat."
        ),
    )


@router.post("/sync")
async def sync_subscription_route(user_id: CurrentUser):
    """Client-triggered status refresh after a purchase."""
    return await get_subscription_status(user_id)
