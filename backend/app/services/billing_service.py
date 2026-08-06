from __future__ import annotations

from datetime import datetime, timezone
from typing import Any

from app.core.config import get_settings
from app.database.memory_store import USER_STORE
from app.services.subscription_plans import get_plan_by_code, plan_code_for_product


class BillingService:
    def get_subscription_status(self, user_id: str) -> dict[str, Any]:
        user = USER_STORE.get_user(user_id)
        return {
            "status": "success",
            "user_id": user_id,
            "subscription_plan": user.get("subscription_plan", "free"),
            "subscription_active": bool(user.get("subscription_active", False)),
            "subscription_expires_at": user.get("subscription_expires_at"),
            "will_renew": bool(user.get("will_renew", False)),
            "billing_issue": bool(user.get("billing_issue", False)),
            "billing_period": user.get("billing_period"),
            "play_product_id": user.get("play_product_id"),
            "entitlement_source": user.get("entitlement_source"),
            "credits": int(user.get("credits", 0)),
            "video_credits": int(user.get("video_credits", 0)),
        }

    def activate_subscription(self, user_id: str, plan_code: str) -> dict[str, Any]:
        """
        Deprecated unverified activation. Kept only for admin/dev with explicit flag.
        """
        raise PermissionError(
            "Unverified plan activation is disabled. "
            "Purchase via RevenueCat; backend syncs through webhooks."
        )

    def refresh_credits_for_period(self, user_id: str, plan_code: str) -> dict[str, Any]:
        settings = get_settings()
        plan = get_plan_by_code(plan_code) or {}
        monthly = int(plan.get("monthly_credits", settings.entitlement_monthly_credits))
        video = int(plan.get("video_credits", settings.entitlement_video_credits))
        user = USER_STORE.get_user(user_id)
        return USER_STORE.upsert_user(
            user_id,
            {
                "credits": int(user.get("credits", 0)) + monthly,
                "video_credits": int(user.get("video_credits", 0)) + video,
                "last_renewed_at": datetime.now(timezone.utc).isoformat(),
            },
        )

    def sync_from_revenuecat_event(self, event: dict[str, Any]) -> dict[str, Any]:
        event_id = str(event.get("id") or event.get("event_timestamp_ms") or "")
        if event_id and not USER_STORE.mark_event_processed(event_id):
            return {"status": "duplicate", "event_id": event_id}

        event_type = str(event.get("type", "")).upper()
        app_user_id = str(event.get("app_user_id") or "")
        if not app_user_id:
            raise ValueError("Missing app_user_id in RevenueCat event")

        product_id = event.get("product_id") or event.get("new_product_id")
        period_type = event.get("period_type")
        plan_code = plan_code_for_product(str(product_id) if product_id else None, str(period_type) if period_type else None)
        expiration = event.get("expiration_at_ms")
        expires_at = None
        if isinstance(expiration, (int, float)):
            expires_at = datetime.fromtimestamp(expiration / 1000, tz=timezone.utc).isoformat()

        if event_type in {"INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION", "PRODUCT_CHANGE"}:
            USER_STORE.upsert_user(
                app_user_id,
                {
                    "subscription_plan": plan_code,
                    "subscription_active": True,
                    "subscription_expires_at": expires_at,
                    "will_renew": True,
                    "billing_issue": False,
                    "play_product_id": product_id,
                    "billing_period": "yearly" if "year" in plan_code else "monthly",
                    "entitlement_source": "revenuecat",
                    "revenuecat_customer_id": event.get("original_app_user_id") or app_user_id,
                },
            )
            self.refresh_credits_for_period(app_user_id, plan_code)
        elif event_type == "CANCELLATION":
            USER_STORE.upsert_user(
                app_user_id,
                {
                    "will_renew": False,
                    "subscription_expires_at": expires_at,
                },
            )
        elif event_type in {"EXPIRATION", "SUBSCRIPTION_PAUSED"}:
            USER_STORE.upsert_user(
                app_user_id,
                {
                    "subscription_plan": "free",
                    "subscription_active": False,
                    "will_renew": False,
                    "billing_issue": False,
                    "subscription_expires_at": expires_at,
                },
            )
        elif event_type == "BILLING_ISSUE":
            USER_STORE.upsert_user(
                app_user_id,
                {
                    "billing_issue": True,
                    "will_renew": False,
                },
            )
        else:
            return {"status": "ignored", "type": event_type}

        return {
            "status": "processed",
            "type": event_type,
            "user_id": app_user_id,
            "subscription": self.get_subscription_status(app_user_id),
        }


billing_service = BillingService()
