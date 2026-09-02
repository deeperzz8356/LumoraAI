# OneSignal Setup Guide

## Prerequisites

Before integrating OneSignal push notifications, you need to:

1. Create a OneSignal account at https://onesignal.com
2. Create a new application in the OneSignal dashboard
3. Get your OneSignal App ID

## Configuration Steps

### 1. Update OneSignal App ID

In `OneSignalManager.kt`, replace the placeholder with your actual OneSignal App ID:

```kotlin
private const val ONESIGNAL_APP_ID = "your-actual-onesignal-app-id"
```

Also update it in `NotificationConfig.kt`:

```kotlin
const val ONESIGNAL_APP_ID = "your-actual-onesignal-app-id"
```

### 2. Enable Notification Permission (Android 13+)

The app already requests `POST_NOTIFICATIONS` permission in `AndroidManifest.xml`. OneSignal will request runtime permission when needed.

### 3. Set Up Firebase Cloud Messaging (Optional but Recommended)

OneSignal works best with FCM. To set up:

1. Enable FCM in Firebase Console
2. Add FCM server key to OneSignal Dashboard
3. This is optional - OneSignal will use its own channel if FCM is not available

## Usage Examples

### Initialize OneSignal

OneSignal is automatically initialized in `LumoraApplication.onCreate()` through dependency injection.

### Set External User ID (After Login)

```kotlin
// In your authentication/login code
oneSignalManager.setExternalUserId(userId)
```

### Clear External User ID (On Logout)

```kotlin
// In your logout code
oneSignalManager.clearExternalUserId()
```

### Add Tags for Segmentation

```kotlin
// Add custom tags to segment users
oneSignalManager.addTags(mapOf(
    "user_type" to "premium",
    "language" to "en",
    "plan" to "pro"
))
```

### Get Subscription ID

```kotlin
val subscriptionId = oneSignalManager.getSubscriptionId()
// Use this ID to send targeted notifications from your backend
```

### Control Notification Subscriptions

```kotlin
// Allow user to opt-out of notifications
oneSignalManager.setNotificationSubscription(false)

// Re-enable notifications
oneSignalManager.setNotificationSubscription(true)
```

## Notification Channels

The app creates three notification channels:

1. **High Priority** (Urgent)
   - Channel ID: `lumora_high_priority`
   - Used for: Task completion alerts, critical notifications

2. **Medium Priority** (Important)
   - Channel ID: `lumora_medium_priority`
   - Used for: Feature announcements, important updates

3. **Low Priority** (Informational)
   - Channel ID: `lumora_low_priority`
   - Used for: Tips, general information

### Managing Notification Channels

Channels are created automatically in `NotificationChannelManager.kt`.

Users can customize channel settings in:
- Android Settings → Apps → LumoraAI → Notifications

## Sending Notifications from Backend

### Using OneSignal REST API

Endpoint: `POST https://onesignal.com/api/v1/notifications`

Example payload for targeted user:

```json
{
  "app_id": "your-onesignal-app-id",
  "include_external_user_ids": ["user-123"],
  "headings": {"en": "Task Complete"},
  "contents": {"en": "Your AI generation is ready!"},
  "data": {
    "type": "TASK_COMPLETION",
    "priority": "HIGH",
    "actionUrl": "lumora://task/task-123"
  }
}
```

### Using OneSignal Dashboard

1. Navigate to "Campaigns" → "Create a Campaign"
2. Choose "Push Notification"
3. Set up targeting, content, and scheduling
4. Preview and send

## Handling Notifications in App

Notifications are automatically handled by:

1. `OneSignalManager` - Receives clicks and foreground events
2. `NotificationManager` - Stores notifications in Room database
3. Deep link handler (Task #9) - Routes to appropriate screens

## Testing

### Test Sending to Device

1. Get your subscription ID:
```kotlin
val subId = oneSignalManager.getSubscriptionId()
```

2. Use OneSignal dashboard to send test notification to your device using the subscription ID

### Monitor Notifications

Check logs using:
```bash
adb logcat | grep OneSignalManager
adb logcat | grep NotificationManager
```

## Common Issues

### Notifications not showing

1. Check if notifications are enabled in system settings
2. Verify OneSignal App ID is correct
3. Check if user has subscribed (use `getSubscriptionId()`)
4. View logs in OneSignal dashboard

### Deep links not working

See Task #9: Create notification deep linking system

### Segmentation not working

Ensure tags are set before sending targeted campaigns:
```kotlin
oneSignalManager.addTags(mapOf("your_tag" to "value"))
```

## Next Steps

- Task #4: Create notification manager service with handlers
- Task #5: Build user preferences/settings for notifications
- Task #6: Create notification channels by priority
- Task #7: Design notification UI screens and composables
- Task #8: Set up backend notification API endpoints
- Task #9: Create notification deep linking system
- Task #10: Add sound and vibration customization
