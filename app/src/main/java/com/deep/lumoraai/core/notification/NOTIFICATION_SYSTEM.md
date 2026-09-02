# Notification System Documentation

## Overview

The LumoraAI notification system provides a complete solution for managing and displaying notifications throughout the app. It includes:

- **Local Notification Storage** - Room database for persistent notifications
- **Real-time Updates** - Unread count badge on bell icon
- **Rich Notifications** - Support for different types and priorities
- **Tap-to-Action** - Notifications trigger navigation and actions
- **OneSignal Integration** - Ready for push notifications
- **Demo System** - Easy testing with demo notification triggers

## Architecture

### Core Components

#### NotificationManager
Singleton service that manages all notification operations:
```kotlin
// Send a task completion notification
notificationManager.sendTaskCompletionNotification(
    title = "Image Generated",
    message = "Your AI image is ready!",
    actionUrl = "com.deep.lumoraai://result"
)

// Send an engagement notification
notificationManager.sendEngagementNotification(
    title = "Daily Reminder",
    message = "You have pending creations"
)

// Send feature announcement
notificationManager.sendFeatureAnnouncement(
    title = "New Feature Available",
    message = "Try our new batch processing"
)

// Send custom notification
notificationManager.sendCustomNotification(
    title = "Custom Title",
    message = "Custom message",
    type = "MY_TYPE",
    priority = "HIGH"
)
```

#### NotificationViewModel
Provides UI-layer access to notifications:
```kotlin
val notifications: StateFlow<List<NotificationEntity>> // All notifications
val unreadCount: StateFlow<Int> // Unread count for badge

// Methods
markAsRead(notificationId)
markAllAsRead()
deleteNotification(notificationId)
clearAllNotifications()
getNotificationsByType(type)
getUnreadNotifications()
```

#### NotificationEntity (Room Database)
Represents a single notification:
```kotlin
data class NotificationEntity(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "TASK_COMPLETION", "ENGAGEMENT", "FEATURE_ANNOUNCEMENT"
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val imageUrl: String?,
    val actionUrl: String?, // Deep link
    val isRead: Boolean,
    val createdAt: Long,
    val oneSignalId: String?
)
```

### UI Components

#### AppToolbar
Displays the bell icon with unread count badge:
```kotlin
AppToolbar(
    title = "Home",
    unreadCount = 3,
    onNotificationClick = { navigateToNotifications() }
)
```

#### NotificationCard
Individual notification display with actions:
- Type badge (color-coded by priority)
- Unread indicator
- Delete button
- Mark as read action
- Formatted timestamp
- Tap to take action

#### NotificationsScreen
Full notifications list with:
- Notification list with lazy loading
- Header showing unread count
- Clear all button
- Empty state
- Demo trigger panel (debug mode)

#### DemoNotificationTrigger
Debug panel for testing notifications:
- Job Complete button
- Engagement notification
- Feature announcement
- Error notification
- Demo batch
- Workflow simulation

## Usage

### Basic Usage

Inject NotificationManager into your ViewModel or composable:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    fun onJobComplete() {
        viewModelScope.launch {
            notificationManager.sendTaskCompletionNotification(
                title = "Job Complete",
                message = "Your creation is ready",
                actionUrl = "com.deep.lumoraai://result"
            )
        }
    }
}
```

### Showing Notifications on Home Screen

The home screen automatically displays an unread count badge on the bell icon. Clicking it navigates to the notifications list.

```kotlin
HomeScreen(
    unreadCount = 5,
    onNotificationClick = { navigateToNotifications() }
)
```

### Testing with Demo Triggers

The demo trigger panel appears at the top of the notifications screen in debug mode. Click any button to trigger that notification type.

```kotlin
DemoNotificationTrigger(
    notificationManager = notificationManager,
    scope = coroutineScope
)
```

## Notification Types

### TASK_COMPLETION
High priority notification for completed tasks:
- Used for: Job completion, processing finished
- Priority: HIGH
- Badge color: Error (red)

### ENGAGEMENT
Medium priority for engagement and reminders:
- Used for: Reminders, pending items, engagement
- Priority: MEDIUM
- Badge color: Tertiary (blue-ish)

### FEATURE_ANNOUNCEMENT
Medium priority for feature updates:
- Used for: New features, updates
- Priority: MEDIUM
- Badge color: Tertiary

### CUSTOM
Flexible type for any other notification:
- Used for: Promotions, special events
- Priority: Customizable
- Badge color: Secondary

## Deep Linking

Notifications support action URLs for deep linking:

```kotlin
// When user taps the notification, the action URL is provided
notification.actionUrl // e.g., "com.deep.lumoraai://result"

// In NotificationsRoute:
onNotificationTap = { notification ->
    viewModel.markAsRead(notification.id)
    notification.actionUrl?.let { actionUrl ->
        // Handle navigation
        navController.navigate(actionUrl)
    }
}
```

## Database Schema

Notifications are stored in Room database with:
- Full-text search on title and message
- Indexed queries by type, priority, read status
- Automatic cleanup of old notifications (older than 30 days)
- Transaction support for bulk operations

```kotlin
// Get all notifications (ordered by newest first)
notificationManager.getAllNotifications()

// Get only unread
notificationManager.getUnreadNotifications()

// Get by type
notificationManager.getNotificationsByType("TASK_COMPLETION")

// Get unread count
notificationManager.getUnreadCount()
```

## OneSignal Integration

The system is pre-configured for OneSignal push notifications:

1. **Device Registration**: Automatic on app launch
2. **User Tagging**: Tag users for segmentation
3. **Push Handling**: Received pushes create local notifications
4. **Status Tracking**: OneSignal notification IDs stored for analytics

See `OneSignalManager.kt` for configuration.

## Features

### Automatic Cleanup
- Notifications older than 30 days are automatically deleted
- Keeps database lean and performant

### Real-time Updates
- Unread count badge updates instantly
- LazyColumn recomposes when notifications change
- Flows ensure reactive UI

### User Preferences
- Can be extended to support DND (do not disturb)
- Sound/vibration preferences
- Frequency controls

## Testing

### Manual Testing
1. Open app and navigate to home
2. Click bell icon (currently 0 unread)
3. Go to notifications screen
4. Use demo trigger buttons to create notifications
5. See notifications appear in list with badge count
6. Mark as read, delete, or clear all

### Simulation Scenarios
- **triggerJobCompletion()** - Simulates completed job
- **triggerEngagementNotification()** - Simulates reminder
- **triggerFeatureAnnouncement()** - Simulates new feature
- **triggerErrorNotification()** - Simulates error
- **triggerDemoBatch()** - Simulates multiple notifications
- **simulateJobWorkflow()** - Simulates job progress steps

## Production Deployment

### Before Release
1. ✅ Remove or disable `DemoNotificationTrigger` component
2. ✅ Set up OneSignal production credentials
3. ✅ Test deep linking with real app links
4. ✅ Implement notification preferences UI
5. ✅ Set up backend notification API endpoints
6. ✅ Test with production OneSignal campaigns

### BuildConfig Flavors
Consider using build flavors to disable demo triggers:

```kotlin
if (BuildConfig.DEBUG) {
    // Show demo triggers only in debug builds
    DemoNotificationTrigger(...)
}
```

## File Structure

```
core/notification/
├── NotificationManager.kt          # Core service
├── NotificationViewModel.kt        # ViewModel for global access
├── OneSignalManager.kt             # OneSignal integration
├── NotificationDemo.kt             # Demo helpers
└── NOTIFICATION_SYSTEM.md          # This file

feature/notifications/
├── NotificationsViewModel.kt       # Screen ViewModel
├── NotificationsRoute.kt           # Route composition
├── NotificationsScreen.kt          # Screen UI
├── components/
│   ├── NotificationCard.kt         # Individual card
│   └── DemoNotificationTrigger.kt  # Demo panel

data/local/room/
├── entity/
│   └── NotificationEntity.kt       # Database model
└── dao/
    └── NotificationDao.kt          # Database access
```

## Future Enhancements

- [ ] Notification categories with custom actions
- [ ] Sound and vibration settings
- [ ] Do Not Disturb scheduling
- [ ] Notification history export
- [ ] Analytics dashboard
- [ ] A/B testing support
- [ ] Smart notification grouping
- [ ] Swipe actions (archive, snooze)

## Troubleshooting

### Notifications not showing
1. Check NotificationManager is injected properly
2. Verify Room database is initialized
3. Check DAO queries in Room database

### Badge not updating
1. Verify CoroutineScope is active
2. Check Flow collectors are observing
3. Ensure markAsRead/deleteNotification calls complete

### OneSignal not working
1. Verify App ID in OneSignalManager
2. Check device registration status
3. Review OneSignal dashboard for delivery status

## Questions or Issues?

Refer to:
- NotificationManager.kt for API
- NotificationDemo.kt for examples
- DemoNotificationTrigger.kt for UI patterns
