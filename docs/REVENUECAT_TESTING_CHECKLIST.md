# RevenueCat Integration — Testing Checklist

Use Test Store API key `test_GEIOhUGfQCKpWthxIRpKWHgSbRY` (debug builds).

## Dashboard prerequisites

See [REVENUECAT_DASHBOARD_SETUP.md](./REVENUECAT_DASHBOARD_SETUP.md) and complete:

1. Products `monthly` + `yearly`
2. Entitlement `MK Tech Media tech`
3. Offering `default` (current)
4. Paywall attached to offering
5. Customer Center enabled
6. Webhook → `/api/v1/webhooks/revenuecat`

## App checks

1. Launch debug APK → Logcat shows `LumoraApplication` RevenueCat configure + DEBUG logs
2. Sign in → `AuthViewModel` / `LumoraApplication` logs RC login with Firebase UID
3. Open **Subscription** → monthly/yearly chips load packages (or paywall)
4. Tap **View Paywall** → RevenueCat Paywall UI appears
5. Purchase monthly/yearly (Test Store) → entitlement becomes active
6. Status chip shows **Active**; Profile plan shows **MK Tech Media tech**
7. **Restore purchases** on a fresh install restores entitlement
8. Settings → **Customer Center** opens manage UI
9. Settings → **Subscription & Billing** navigates to subscription screen
10. Sign out → RevenueCat `logOutWith` completes; next login re-links UID

## Backend checks

```bash
cd backend
pip install -r requirements.txt
PYTHONPATH=. pytest tests/test_billing_webhooks.py -q
```

Simulate webhook:

```bash
curl -X POST http://localhost:8000/api/v1/webhooks/revenuecat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $REVENUECAT_WEBHOOK_SECRET" \
  -d '{"event":{"id":"evt1","type":"INITIAL_PURCHASE","app_user_id":"firebaseUid","product_id":"yearly","period_type":"ANNUAL"}}'
```

## Before Play release

- Replace `goog_YOUR_PRODUCTION_ANDROID_KEY` in `app/build.gradle.kts` release `buildConfigField`
- Never ship the Test Store key in release
- Verify Play products imported into RevenueCat
- Point RTDN/webhook at production backend URL
