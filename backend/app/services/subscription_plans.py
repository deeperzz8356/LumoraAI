from __future__ import annotations

from typing import Any


PLAN_CATALOG: list[dict[str, Any]] = [
    {
        "code": "free",
        "name": "Free",
        "price_usd": 0.0,
        "monthly_credits": 0,
        "signup_bonus_credits": 1,
        "video_credits": 0,
        "features": [
            "1 free image generation on signup",
            "Basic templates",
            "Community support",
        ],
        "is_popular": False,
        "billing_period": None,
        "play_product_id": None,
    },
    {
        "code": "mk_tech_monthly",
        "name": "MK Tech Media — Monthly",
        "price_usd": 0.0,  # Localized prices come from RevenueCat / Play
        "monthly_credits": 250,
        "signup_bonus_credits": 0,
        "video_credits": 10,
        "features": [
            "Full MK Tech Media tech entitlement",
            "250 image credits per month",
            "10 video generations per month",
        ],
        "is_popular": True,
        "billing_period": "monthly",
        "play_product_id": "monthly",
        "revenuecat_package": "$rc_monthly",
        "entitlement_id": "MK Tech Media tech",
    },
    {
        "code": "mk_tech_yearly",
        "name": "MK Tech Media — Yearly",
        "price_usd": 0.0,
        "monthly_credits": 250,
        "signup_bonus_credits": 0,
        "video_credits": 10,
        "features": [
            "Full MK Tech Media tech entitlement",
            "250 image credits per month",
            "10 video generations per month",
            "Best value annual billing",
        ],
        "is_popular": False,
        "billing_period": "yearly",
        "play_product_id": "yearly",
        "revenuecat_package": "$rc_annual",
        "entitlement_id": "MK Tech Media tech",
    },
]


def get_plan_catalog() -> dict[str, Any]:
    return {
        "status": "success",
        "currency": "USD",
        "entitlement_id": "MK Tech Media tech",
        "plans": PLAN_CATALOG,
    }


def get_plan_by_code(code: str) -> dict[str, Any] | None:
    return next((plan for plan in PLAN_CATALOG if plan["code"] == code), None)


def plan_code_for_product(product_id: str | None, period_type: str | None = None) -> str:
    product = (product_id or "").lower()
    period = (period_type or "").lower()
    if "year" in product or "annual" in product or period in {"annual", "yearly"}:
        return "mk_tech_yearly"
    if "month" in product or period == "monthly":
        return "mk_tech_monthly"
    return "mk_tech_monthly"
