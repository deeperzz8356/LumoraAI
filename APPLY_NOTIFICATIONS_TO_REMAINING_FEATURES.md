# Apply Task Notifications to Remaining 6 Features

## Overview
Follow this checklist to apply the task notification pattern to the remaining 6 generation features. Each feature requires the same ~3 code changes as TextToImage.

---

## Feature 1: ImageToImage ❌

### File
`feature/imagetoimage/ImageToImageViewModel.kt`

### Changes Required

1. **Add imports:**
   ```kotlin
   import com.deep.lumoraai.core.notification.NotificationManager
   import com.deep.lumoraai.core.notification.TaskNotificationHelper
   import java.util.UUID
   ```

2. **Inject NotificationManager in constructor:**
   ```kotlin
   private val notificationManager = NotificationManager(
       LumoraDatabase.getInstance(application).notificationDao
   )
   ```

3. **Find the generate/processImage method and add:**
   ```kotlin
   val taskId = UUID.randomUUID().toString()
   
   // Send start notification
   viewModelScope.launch {
       notificationManager.sendTaskStartNotification(
           taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
           taskId = taskId,
           displayName = "Image to Image"
       )
   }
   ```

4. **On successful completion, add:**
   ```kotlin
   // Send complete notification
   notificationManager.sendTaskCompleteNotification(
       taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
       taskId = taskId,
       resultId = result.id,
       displayName = "Image to Image"
   )
   ```

5. **On error, add:**
   ```kotlin
   // Send error notification
   launch {
       notificationManager.sendTaskFailureNotification(
           taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
           taskId = taskId,
           displayName = "Image to Image",
           errorMessage = errorMessage
       )
   }
   ```

### Testing
- Generate image with modifications
- Verify start notification appears in bell badge (count: 1)
- Wait for completion
- Verify complete notification appears (count: 2)
- Tap notification → should navigate to result

---

## Feature 2: ImageToVideo ❌

### File
`feature/imagetovideo/ImageToVideoViewModel.kt`

### Task Type
`TaskNotificationHelper.IMAGE_TO_VIDEO`

### Display Name
`"Image to Video"`

### Changes
Same as ImageToImage, just different task type and display name.

### Testing
- Generate video from image
- Verify notifications appear
- Tap to navigate to result

---

## Feature 3: TextToVideo (including PromoVideo) ❌

### Files
`feature/texttovideo/TextToVideoViewModel.kt`

### Task Types
- **Text to Video:** `TaskNotificationHelper.TEXT_TO_VIDEO`
- **Promo Video:** `TaskNotificationHelper.PROMO_VIDEO`

### Display Names
- "Text to Video"
- "Promo Video"

### Special Handling
The ViewModel likely handles both text-to-video and promo videos. Use appropriate task type based on which mode is active:

```kotlin
val taskType = if (isPromo) TaskNotificationHelper.PROMO_VIDEO else TaskNotificationHelper.TEXT_TO_VIDEO
val displayName = if (isPromo) "Promo Video" else "Text to Video"

notificationManager.sendTaskStartNotification(
    taskType = taskType,
    taskId = taskId,
    displayName = displayName
)
```

### Changes
Same pattern as others, adapt based on promo vs regular mode.

---

## Feature 4: BgStudio (Remove/Replace) ❌

### File
`feature/bgstudio/BgStudioViewModel.kt`

### Task Types
- **Remove:** `TaskNotificationHelper.BG_REMOVE`
- **Replace:** `TaskNotificationHelper.BG_REPLACE`

### Display Names
- "Background Remove"
- "Background Replace"

### Special Handling
BgStudio has two modes. Determine mode and use appropriate task type:

```kotlin
val taskType = if (mode == "remove") {
    TaskNotificationHelper.BG_REMOVE
} else {
    TaskNotificationHelper.BG_REPLACE
}

val displayName = if (mode == "remove") {
    "Background Remove"
} else {
    "Background Replace"
}

notificationManager.sendTaskStartNotification(
    taskType = taskType,
    taskId = taskId,
    displayName = displayName
)
```

### Testing
- Test "Remove Background" mode → verify correct task type
- Test "Replace Background" mode → verify correct task type
- Verify notifications distinguish between modes

---

## Feature 5: PhotoEnhance ❌

### File
`feature/photoenhance/PhotoEnhanceViewModel.kt`

### Task Type
`TaskNotificationHelper.PHOTO_ENHANCE`

### Display Name
`"Photo Enhance"`

### Changes
Standard pattern - same as ImageToImage.

### Testing
- Enhance a photo
- Verify start/complete notifications
- Tap to view result

---

## Feature 6: Compress ❌

### File
`feature/compress/CompressViewModel.kt`

### Task Type
`TaskNotificationHelper.COMPRESS`

### Display Name
`"Compress"`

### Special Note
TaskNotificationHelper has custom message for compress:
```kotlin
COMPRESS -> "Your files have been compressed successfully"
```

This will auto-use the right message in `sendTaskCompleteNotification()`.

### Changes
Standard pattern.

### Testing
- Compress image/video
- Verify notifications
- Tap to view result

---

## ✅ Completion Checklist

After implementing all 6 features:

### Code Quality
- [ ] All imports added correctly
- [ ] NotificationManager injected in all 6 ViewModels
- [ ] Task types match TaskNotificationHelper constants
- [ ] Display names are user-friendly
- [ ] Error handling includes error notifications
- [ ] No compilation errors

### Testing
- [ ] Build project successfully
- [ ] TextToImage notifications still work
- [ ] ImageToImage generates start/complete notifications
- [ ] ImageToVideo generates start/complete notifications
- [ ] TextToVideo generates start/complete notifications
- [ ] BgStudio generates notifications for both modes
- [ ] PhotoEnhance generates start/complete notifications
- [ ] Compress generates start/complete notifications

### Integration
- [ ] All notifications appear in bell badge in real-time
- [ ] Tapping notification navigates to result screen
- [ ] Mark as read/delete functions work
- [ ] Badge updates when notifications marked as read
- [ ] Notifications persist after app restart

### Polish
- [ ] Error messages are helpful and user-friendly
- [ ] No duplicate notifications
- [ ] Performance is smooth with multiple notifications
- [ ] App doesn't crash under notification load

---

## Estimation

**Per Feature:** 10-15 minutes
- 2 min: Find generate/process method
- 3 min: Add imports and inject NotificationManager
- 3 min: Add start notification
- 3 min: Add complete notification
- 3 min: Add error notification
- 1 min: Quick test

**Total for 6 Features:** 60-90 minutes

**Build & Full Testing:** 20-30 minutes

**Total Project Time:** ~2 hours to full completion

---

## Common Mistakes to Avoid

❌ **Wrong:** Forgetting to import UUID
✅ **Correct:** Import `java.util.UUID` and use `UUID.randomUUID().toString()`

❌ **Wrong:** Using wrong task type constant
✅ **Correct:** Use exact constant from `TaskNotificationHelper` (e.g., `TaskNotificationHelper.TEXT_TO_IMAGE`)

❌ **Wrong:** Passing `actionUrl` to `sendTaskCompleteNotification()`
✅ **Correct:** Don't pass actionUrl - method builds it internally from resultId

❌ **Wrong:** Calling notification in UI thread without launch
✅ **Correct:** Wrap in `viewModelScope.launch { }` since it's suspend function

❌ **Wrong:** Forgetting resultId in complete notification
✅ **Correct:** Always pass `resultId = result.id` or `resultId = saved.id`

---

## Quick Reference

```kotlin
// Template for any feature
private val notificationManager = NotificationManager(
    LumoraDatabase.getInstance(application).notificationDao
)

fun generate() {
    val taskId = UUID.randomUUID().toString()
    
    viewModelScope.launch {
        // START
        notificationManager.sendTaskStartNotification(
            taskType = TaskNotificationHelper.[TYPE],
            taskId = taskId,
            displayName = "[Display Name]"
        )
    }
    
    // ... do work ...
    
    if (success) {
        // COMPLETE
        viewModelScope.launch {
            notificationManager.sendTaskCompleteNotification(
                taskType = TaskNotificationHelper.[TYPE],
                taskId = taskId,
                resultId = result.id,
                displayName = "[Display Name]"
            )
        }
    } else {
        // ERROR
        viewModelScope.launch {
            notificationManager.sendTaskFailureNotification(
                taskType = TaskNotificationHelper.[TYPE],
                taskId = taskId,
                displayName = "[Display Name]",
                errorMessage = error.message ?: "Unknown error"
            )
        }
    }
}
```

---

## Status Tracking

| Feature | Status | Notes |
|---------|--------|-------|
| TextToImage | ✅ DONE | Template implementation |
| ImageToImage | ⬜ TODO | Same as TextToImage |
| ImageToVideo | ⬜ TODO | Same as TextToImage |
| TextToVideo | ⬜ TODO | Handle promo mode separately |
| BgStudio | ⬜ TODO | Handle remove vs replace modes |
| PhotoEnhance | ⬜ TODO | Same as TextToImage |
| Compress | ⬜ TODO | Same as TextToImage |

---

## Questions?

Refer back to:
- TextToImageViewModel for working example
- TaskNotificationHelper for all supported task types
- TASK_NOTIFICATIONS_IMPLEMENTATION_SUMMARY.md for architecture

Happy implementing! 🚀
