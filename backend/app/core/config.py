from __future__ import annotations

import os
from functools import lru_cache


class Settings:
    revenuecat_webhook_secret: str = os.getenv("REVENUECAT_WEBHOOK_SECRET", "")
    revenuecat_entitlement_id: str = os.getenv(
        "REVENUECAT_ENTITLEMENT_ID", "MK Tech Media tech"
    )
    # Monthly credit grants when entitlement is active
    entitlement_monthly_credits: int = int(os.getenv("ENTITLEMENT_MONTHLY_CREDITS", "250"))
    entitlement_video_credits: int = int(os.getenv("ENTITLEMENT_VIDEO_CREDITS", "10"))
    # Demo in-memory store when Firestore is not configured
    use_memory_store: bool = os.getenv("USE_MEMORY_STORE", "true").lower() == "true"


@lru_cache
def get_settings() -> Settings:
    return Settings()
