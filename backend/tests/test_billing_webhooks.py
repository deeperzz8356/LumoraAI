from __future__ import annotations

from app.services.billing_service import BillingService


def test_initial_purchase_grants_credits():
    service = BillingService()
    result = service.sync_from_revenuecat_event(
        {
            "id": "evt_test_1",
            "type": "INITIAL_PURCHASE",
            "app_user_id": "user_abc",
            "product_id": "yearly",
            "period_type": "ANNUAL",
            "expiration_at_ms": 1893456000000,
        }
    )
    assert result["status"] == "processed"
    status = service.get_subscription_status("user_abc")
    assert status["subscription_active"] is True
    assert status["subscription_plan"] == "mk_tech_yearly"
    assert status["credits"] >= 250


def test_duplicate_event_is_idempotent():
    service = BillingService()
    event = {
        "id": "evt_dup",
        "type": "RENEWAL",
        "app_user_id": "user_dup",
        "product_id": "monthly",
        "period_type": "NORMAL",
    }
    first = service.sync_from_revenuecat_event(event)
    second = service.sync_from_revenuecat_event(event)
    assert first["status"] == "processed"
    assert second["status"] == "duplicate"


def test_expiration_downgrades_to_free():
    service = BillingService()
    service.sync_from_revenuecat_event(
        {
            "id": "evt_buy",
            "type": "INITIAL_PURCHASE",
            "app_user_id": "user_exp",
            "product_id": "monthly",
        }
    )
    result = service.sync_from_revenuecat_event(
        {
            "id": "evt_exp",
            "type": "EXPIRATION",
            "app_user_id": "user_exp",
            "product_id": "monthly",
        }
    )
    assert result["status"] == "processed"
    status = service.get_subscription_status("user_exp")
    assert status["subscription_plan"] == "free"
    assert status["subscription_active"] is False


def test_activate_is_disabled():
    service = BillingService()
    try:
        service.activate_subscription("user", "mk_tech_monthly")
        assert False, "expected PermissionError"
    except PermissionError:
        pass
