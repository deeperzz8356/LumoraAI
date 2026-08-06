# Lumora AI Backend

FastAPI backend with RevenueCat webhook sync for subscription entitlements.

## Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --app-dir .
```

## Env

- `REVENUECAT_WEBHOOK_SECRET` — shared secret for webhook Authorization header
- `REVENUECAT_ENTITLEMENT_ID` — default `MK Tech Media tech`
- `USE_MEMORY_STORE` — `true` by default (no Firestore required for local)

## Endpoints

- `GET /api/v1/subscriptions/plans`
- `GET /api/v1/subscriptions/status` (requires Bearer + x-user-id)
- `POST /api/v1/subscriptions/activate` → **410 Gone** (use RevenueCat)
- `POST /api/v1/webhooks/revenuecat`
