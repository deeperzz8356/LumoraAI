# Sound and Vibration Customization Guide

Complete guide to configuring sound and vibration feedback for the LumoraAI notification system.

## Overview

The notification system provides comprehensive sound and vibration customization, allowing users to personalize their notification experience.

## Components

### 1. SoundAndVibrationManager

Central manager for all sound and vibration operations.

**Features:**
- Play notification sounds from SoundPool
- Fallback to system notification sound
- Vibration with custom patterns
- Per-priority configuration
- User preference integration

### 2. HapticFeedback

Predefined vibration patterns for consistent feedback.

**Patterns Available:**
- TAP_LIGHT - Simple interactions
- TAP_MEDIUM - Standard interactions
- TAP_HEAVY - Important interactions
- DOUBLE_TAP - Success feedback
- TRIPLE_TAP - Error/warning
- NOTIFICATION - Notification alerts
- SUCCESS - Task completion
- ERROR - Error states
- And many more...

## Sound Configuration

### Supported Sound Types

```
SOUND_NONE           # No sound
SOUND_DEFAULT        # Default notification sound
SOUND_ALERT          # Alert/warning sound
SOUND_NOTIFICATION   # Standard notification sound
```

### Enabling/Disabling Sounds

#### From Settings Screen

```kotlin
// In NotificationSettingsScreen
SwitchSetting(
    label = "Sound",
    description = "Play notification sound",
    isChecked = prefs.soundEnabled,
    onCheckedChange = { viewModel.toggleSound(it) }
)
```

#### Programmatically

```kotlin
// Enable sound
soundAndVibrationManager.setSoundEnabled(true)

// Disable sound
soundAndVibrationManager.setSoundEnabled(false)

// Set specific sound type
soundAndVibrationManager.setNotificationSound(NotificationConfig.SOUND_DEFAULT)
```

### Playing Sounds

#### Basic Usage

```kotlin
// Play default notification sound
soundAndVibrationManager.playNotificationSound()

// Play with specific priority
soundAndVibrationManager.playNotificationSound(
    priority = NotificationConfig.NOTIFICATION_PRIORITY_HIGH
)

// Play specific sound type
soundAndVibrationManager.playNotificationSound(
    soundType = NotificationConfig.SOUND_ALERT
)
```

#### Integration with Notifications

```kotlin
// When notification is received
val priority = notification.priority
val soundType = notification.soundType ?: NotificationConfig.SOUND_DEFAULT

soundAndVibrationManager.playNotificationSound(
    priority = priority,
    soundType = soundType
)
```

## Vibration Configuration

### Vibration Patterns

Each pattern is defined as a `LongArray` in milliseconds:
- Index 0: Initial delay (usually 0)
- Index 1: Vibrate duration
- Index 2: Wait duration
- Index 3: Vibrate duration
- And so on...

### Built-in Patterns

```kotlin
// Light vibration (30ms)
HapticFeedback.TAP_LIGHT

// Medium vibration (50ms)
HapticFeedback.TAP_MEDIUM

// Heavy vibration (100ms)
HapticFeedback.TAP_HEAVY

// Double tap pattern
HapticFeedback.DOUBLE_TAP
// [0ms (delay), 30ms (vib), 50ms (wait), 30ms (vib)]

// Success pattern
HapticFeedback.SUCCESS
// [0ms, 50ms, 50ms, 50ms, 50ms, 100ms]

// Error pattern
HapticFeedback.ERROR
// [0ms, 100ms, 100ms, 100ms, 100ms, 200ms]
```

### Enabling/Disabling Vibration

#### From Settings Screen

```kotlin
SwitchSetting(
    label = "Vibration",
    description = "Vibrate for notifications",
    isChecked = prefs.vibrationEnabled,
    onCheckedChange = { viewModel.toggleVibration(it) }
)
```

#### Programmatically

```kotlin
// Enable vibration
soundAndVibrationManager.setVibrationEnabled(true)

// Disable vibration
soundAndVibrationManager.setVibrationEnabled(false)
```

### Playing Vibrations

#### Basic Usage

```kotlin
// Vibrate based on priority
soundAndVibrationManager.vibrateForNotification(
    priority = NotificationConfig.NOTIFICATION_PRIORITY_HIGH
)

// Custom pattern
soundAndVibrationManager.vibrate(HapticFeedback.DOUBLE_TAP)

// Single vibration
soundAndVibrationManager.vibrateOnce(durationMs = 100)
```

#### Get Pattern by Priority

```kotlin
val pattern = HapticFeedback.getPatternForPriority(
    NotificationConfig.NOTIFICATION_PRIORITY_HIGH
)
// Returns: HapticFeedback.ERROR

soundAndVibrationManager.vibrate(pattern)
```

#### Get Pattern by Type

```kotlin
val pattern = HapticFeedback.getPatternForType(
    NotificationConfig.NOTIFICATION_TYPE_TASK_COMPLETION
)
// Returns: HapticFeedback.SUCCESS

soundAndVibrationManager.vibrate(pattern)
```

### Custom Vibration Patterns

Create your own vibration patterns:

```kotlin
// Custom pattern: 100ms vibrate, 50ms wait, 75ms vibrate
val customPattern = longArrayOf(
    0,      // No initial delay
    100,    // Vibrate for 100ms
    50,     // Wait 50ms
    75      // Vibrate for 75ms
)

soundAndVibrationManager.vibrate(customPattern)
```

## Combined Sound & Vibration

### Play Both Together

```kotlin
soundAndVibrationManager.playNotificationFeedback(
    priority = NotificationConfig.NOTIFICATION_PRIORITY_HIGH,
    soundType = NotificationConfig.SOUND_DEFAULT
)
```

This will:
1. Play notification sound
2. Vibrate with pattern matching priority

## Android Manifest Permissions

Required permissions are already added:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

## API Level Compatibility

The system handles different Android versions:

```kotlin
// Android 12 (S) and above
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Use VibrationEffect (modern API)
    val effect = VibrationEffect.createWaveform(pattern)
    vibrator.vibrate(effect)
} else {
    // Fallback for older versions
    vibrator.vibrate(pattern, -1)
}
```

## Best Practices

### 1. Respect User Preferences

Always check user preferences before playing sounds/vibrations:

```kotlin
// ✅ Good - checks preferences
soundAndVibrationManager.playNotificationSound()

// ❌ Bad - ignores user settings
RingtoneManager.getRingtone(...).play()
```

### 2. Use Appropriate Patterns

Match patterns to notification importance:

```kotlin
// HIGH priority = ERROR pattern (strong feedback)
if (priority == HIGH) {
    vibrate(HapticFeedback.ERROR)
}

// LOW priority = TAP_LIGHT (subtle feedback)
if (priority == LOW) {
    vibrate(HapticFeedback.TAP_LIGHT)
}
```

### 3. Don't Overuse

Avoid too many notifications with sound/vibration:

```kotlin
// Good: Allow user to configure frequency
settings.max_notifications_per_day = 20

// Bad: Too many notifications
// Sends 100+ notifications per day
```

### 4. Test on Real Devices

Different devices have different vibrator capabilities:

```kotlin
// Check if device has vibrator
if (soundAndVibrationManager.hasVibrator()) {
    soundAndVibrationManager.vibrateForNotification()
}
```

## Testing

### Test Sounds

```bash
# Test notification sound
adb shell am broadcast \
  -a com.deep.lumoraai.PLAY_NOTIFICATION_SOUND \
  com.deep.lumoraai
```

### Test Vibrations

```bash
# Test vibration pattern
adb shell am broadcast \
  -a com.deep.lumoraai.VIBRATE_TEST \
  com.deep.lumoraai
```

### Manual Testing

1. Open Settings → Notification Settings
2. Toggle Sound ON/OFF
3. Toggle Vibration ON/OFF
4. Send test notification
5. Verify sound and vibration behavior

## Troubleshooting

### Issue: No Sound Playing

**Check:**
- Is "Sound" enabled in settings?
- Is device not in silent mode?
- Check device volume settings
- Verify SoundPool initialized correctly

**Fix:**
```kotlin
// Fallback to system notification sound
soundAndVibrationManager.playDefaultNotificationSound()
```

### Issue: No Vibration

**Check:**
- Is "Vibration" enabled in settings?
- Device has vibrator: `hasVibrator() == true`
- VIBRATE permission granted
- Pattern is not all zeros

**Fix:**
```kotlin
// Check vibrator availability
if (soundAndVibrationManager.hasVibrator()) {
    soundAndVibrationManager.vibrateForNotification()
}
```

### Issue: Wrong Pattern Playing

**Check:**
- Verify pattern definition
- Check if pattern combines correctly
- Ensure pattern values are in milliseconds

**Debug:**
```kotlin
Log.d(TAG, "Pattern: ${HapticFeedback.ERROR.joinToString(", ")}")
// Output: Pattern: 0, 100, 100, 100, 100, 200
```

## Advanced Usage

### Creating Notification Profiles

```kotlin
data class NotificationProfile(
    val name: String,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val soundType: String,
    val vibrationType: String
)

// Quiet profile
val quietProfile = NotificationProfile(
    name = "Quiet",
    soundEnabled = false,
    vibrationEnabled = true,
    soundType = NotificationConfig.SOUND_NONE,
    vibrationType = "LIGHT"
)

// Loud profile
val loudProfile = NotificationProfile(
    name = "Loud",
    soundEnabled = true,
    vibrationEnabled = true,
    soundType = NotificationConfig.SOUND_DEFAULT,
    vibrationType = "STRONG"
)
```

### Time-Based Configuration

```kotlin
// During work hours - minimal feedback
val workHours = 9..17
val now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

if (now in workHours) {
    soundAndVibrationManager.setSoundEnabled(false)
    soundAndVibrationManager.setVibrationEnabled(true)
} else {
    soundAndVibrationManager.setSoundEnabled(true)
    soundAndVibrationManager.setVibrationEnabled(true)
}
```

## Performance Considerations

### SoundPool Management

- Max 5 concurrent sounds
- Sounds are reused (not recreated)
- Properly released on app shutdown

### Vibration Efficiency

- Patterns are played asynchronously
- No blocking on UI thread
- Resource efficient on all Android versions

## Resources

- [Android Vibration API Documentation](https://developer.android.com/reference/android/os/Vibrator)
- [SoundPool Documentation](https://developer.android.com/reference/android/media/SoundPool)
- [VibrationEffect Documentation](https://developer.android.com/reference/android/os/VibrationEffect)
- [AudioAttributes Documentation](https://developer.android.com/reference/android/media/AudioAttributes)
