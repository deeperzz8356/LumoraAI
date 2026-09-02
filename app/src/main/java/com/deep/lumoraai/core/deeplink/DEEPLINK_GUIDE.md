# Deep Linking System Guide

Complete guide to the LumoraAI notification deep linking system.

## Overview

The deep linking system allows notifications to navigate users directly to relevant content in the app. Deep links use a custom URI scheme (`lumora://`) to route to specific screens.

## Supported Deep Links

### Task Notifications

Navigate to a specific task in the queue:

```
lumora://task/{taskId}
```

**Examples:**
- `lumora://task/task-123`
- `lumora://task/ai-generation-001`

**Navigation Behavior:** Opens the Queue screen with the specified task highlighted

---

### Feature Announcements

Navigate to a feature or feature details:

```
lumora://feature/{featureId}
```

**Examples:**
- `lumora://feature/real-time-editing`
- `lumora://feature/new-ai-models`

**Navigation Behavior:** Opens the CreateHub screen with feature highlighted

---

### Results

Navigate to a specific generation result:

```
lumora://result/{resultId}
```

**Examples:**
- `lumora://result/result-456`
- `lumora://result/video-generation-789`

**Navigation Behavior:** Opens the Result screen with the specified result loaded

---

### Notifications

Open the notifications screen:

```
lumora://notifications
```

**Navigation Behavior:** Opens the full Notifications screen

---

### Profile

Open the user profile:

```
lumora://profile
```

**Navigation Behavior:** Opens the Profile screen

---

### Settings

Open app settings:

```
lumora://settings
```

**Navigation Behavior:** Opens the Settings screen

---

### Help & Support

Open help/support section:

```
lumora://help
```

**Navigation Behavior:** Opens the Help & Support screen

---

### Home

Navigate to home:

```
lumora://home
```

**Navigation Behavior:** Opens the Home screen

---

## How It Works

### 1. Notification Creation

When creating a notification, include an `action_url` parameter:

```python
# Backend example
from app.core.deeplink import NotificationDeepLinkBuilder

notification = SendNotificationRequest(
    user_id="user-123",
    title="✅ Task Complete",
    message="Your video generation is ready!",
    notification_type="TASK_COMPLETION",
    priority="HIGH",
    action_url="lumora://task/task-123"  # Include deep link
)
```

### 2. Notification Delivery

OneSignal delivers the notification with the deep link included in the payload.

### 3. User Taps Notification

When the user taps the notification:
1. Android opens `DeepLinkActivity`
2. Intent contains the deep link URI
3. `DeepLinkHandler` parses and processes the URI
4. App navigates to the appropriate screen

### 4. Navigation

The `DeepLinkHandler` uses Navigation Compose to route to the correct destination:

```kotlin
DeepLinkHandler.handleDeepLink(uri, navController)
```

---

## Integration Examples

### Backend - Sending Task Completion Notification

```python
import requests
from app.core.deeplink import NotificationDeepLinkBuilder

# Build deep link
action_url = NotificationDeepLinkBuilder.build_task_notification_deeplink("task-123")

# Create notification
payload = {
    "user_id": "user-123",
    "title": "✅ Generation Complete",
    "message": "Your AI video is ready to download!",
    "notification_type": "TASK_COMPLETION",
    "priority": "HIGH",
    "action_url": action_url,  # lumora://task/task-123
    "data": {
        "task_id": "task-123",
        "estimated_size_mb": "256"
    }
}

response = requests.post(
    "http://localhost:8000/api/v1/notifications/send",
    json=payload
)
```

### Android - Handling Deep Link

```kotlin
// In your notification handler
val actionUrl = notification.actionUrl  // "lumora://task/task-123"
val uri = Uri.parse(actionUrl)

// Handle the deep link
DeepLinkHandler.handleDeepLink(uri, navController)
```

### Building Deep Links in Code

```kotlin
import com.deep.lumoraai.core.deeplink.DeepLinkDestination

// Create deep link from destination
val destination = DeepLinkDestination.Task("task-123")
val deepLink = destination.toUri()  // "lumora://task/task-123"

// Or use the builder helper
val deepLink = NotificationDeepLinkBuilder.buildTaskNotificationDeepLink("task-123")
```

---

## Android Manifest Configuration

The `AndroidManifest.xml` includes intent filters for all deep link schemes:

```xml
<activity
    android:name=".DeepLinkActivity"
    android:exported="true">
    
    <!-- Handle lumora://task/{id} -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="lumora" android:host="task" />
    </intent-filter>
    
    <!-- Handle lumora://feature/{id} -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="lumora" android:host="feature" />
    </intent-filter>
    
    <!-- ... more intent filters ... -->
</activity>
```

---

## Testing Deep Links

### Test via ADB

```bash
# Test task deep link
adb shell am start -a android.intent.action.VIEW -d "lumora://task/test-123" com.deep.lumoraai

# Test feature deep link
adb shell am start -a android.intent.action.VIEW -d "lumora://feature/new-feature" com.deep.lumoraai

# Test notifications deep link
adb shell am start -a android.intent.action.VIEW -d "lumora://notifications" com.deep.lumoraai
```

### Test via Chrome

```
# Create a test HTML file with deep link
<a href="lumora://task/test-123">Open Task</a>

# Or type in address bar (may not work on all devices):
lumora://task/test-123
```

### Test via Terminal

```bash
# Using adb
adb shell am start -W -a android.intent.action.VIEW \
  -d "lumora://task/test-123" \
  com.deep.lumoraai
```

---

## Debugging

### Enable Logging

Deep link handling includes comprehensive logging:

```kotlin
// Set log level
OneSignalManager.enableVerboseLogging()
```

### View Logs

```bash
# Watch logs in real-time
adb logcat | grep "DeepLinkHandler"

# Or search in Android Studio Logcat:
# Filter: "DeepLinkHandler" or "DeepLinkActivity"
```

### Troubleshooting

**Issue:** Deep link not opening app
- Check Android version (requires API 16+)
- Verify intent filters in AndroidManifest.xml
- Ensure activity is exported (`android:exported="true"`)

**Issue:** Wrong screen opening
- Check URI format matches exactly
- Verify host parameter is correct
- Check NavGraph for navigation routes

**Issue:** Parameters not passed
- Use URLEncoder/URLDecoder for special characters
- Verify path segments are correctly formatted
- Check navigation argument types match

---

## Best Practices

1. **Always validate URIs** - Check for null and malformed URIs
2. **Handle navigation errors** - Gracefully fallback to home screen
3. **Log deep link handling** - Enable debug logging for troubleshooting
4. **Test thoroughly** - Test on multiple Android versions
5. **Use proper URI encoding** - Encode special characters in IDs
6. **Provide fallback** - Include a default action URL (e.g., notifications screen)
7. **Keep URIs short** - Longer URIs may be truncated by some systems

---

## API Integration

### Notification Request with Deep Link

```json
{
  "user_id": "user-123",
  "title": "✅ Task Complete",
  "message": "Your generation is ready!",
  "notification_type": "TASK_COMPLETION",
  "priority": "HIGH",
  "action_url": "lumora://task/task-123",
  "data": {
    "task_id": "task-123"
  }
}
```

### Response

```json
{
  "status": "success",
  "message": "Notification sent successfully",
  "notification_id": "notif_123",
  "user_id": "user-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Future Enhancements

- [ ] Support for web deep links (https://)
- [ ] App link verification for enhanced security
- [ ] Deeplink analytics tracking
- [ ] Custom parameters in deep links
- [ ] Deep link shortcuts for home screen
- [ ] Deeplink support in QR codes

---

## Related Documentation

- [Notification System Guide](./NOTIFICATION_SYSTEM.md)
- [Backend Notification API](../../backend/NOTIFICATION_API.md)
- [Android Intent Filters Documentation](https://developer.android.com/guide/components/intents-filters)
- [Navigation Compose Documentation](https://developer.android.com/jetpack/compose/navigation)
