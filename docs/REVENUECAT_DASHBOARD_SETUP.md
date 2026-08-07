# RevenueCat Dashboard Setup — LumoraAI

Complete these steps in the [RevenueCat Dashboard](https://app.revenuecat.com) before testing purchases.

## 1. Products (Test Store)

Create two Test Store subscriptions:

| Product identifier | Billing period | Package type |
|---|---|---|
| `monthly` | Monthly | `$rc_monthly` |
| `yearly` | Annual | `$rc_annual` |

For production, create matching products in Google Play Console (`com.deep.lumoraai`) and import them into RevenueCat.

## 2. Entitlement

Create one entitlement with identifier **exactly**:

```
MK Tech Media tech
```

Attach both `monthly` and `yearly` products to this entitlement.

App check:

```kotlin
customerInfo.entitlements["MK Tech Media tech"]?.isActive == true
```

## 3. Offering

- Create offering `default` and mark it as **Current**
- Add packages: Monthly → `monthly`, Annual → `yearly`

## 4. Paywall

1. RevenueCat → **Paywalls** → Create from template
2. Attach to `default` offering
3. Design monthly + annual package cards
4. Publish paywall

## 5. Customer Center

1. RevenueCat → **Customer Center** → Configure
2. Enable: Restore purchases, Manage subscription, Cancellation feedback
3. Optional: promotional offers for cancellation path

## 6. Webhook

Set webhook URL:

```
POST https://lumoraai-backend-rlcy.onrender.com/api/v1/webhooks/revenuecat
```

Authorize with a shared secret stored as `REVENUECAT_WEBHOOK_SECRET` on the backend.

## 7. API Keys

| Build | Key |
|---|---|
| Debug / Test Store | `test_GEIOhUGfQCKpWthxIRpKWHgSbRY` (configured in app) |
| Release / Google Play | Replace `goog_YOUR_PRODUCTION_ANDROID_KEY` in `app/build.gradle.kts` |

Never ship a release build with the Test Store key.
