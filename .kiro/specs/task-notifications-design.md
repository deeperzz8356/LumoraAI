# Task Notifications Feature - Design Spec

## Feature Overview
Add automatic notifications when generation tasks start and complete. Users see real-time feedback on task progress with notifications appearing in the bell icon badge and notification list.

## Design

### 1. Architecture Overview

Generation Task Flow:
- User starts task (Text→Image, Image→Video, etc)
- Trigger "Task Started" Notification
  - Title: "Processing Started"
  - Message: "Your {task_type} is being generated"
  - Type: TASK_PROGRESS
  - Priority: MEDIUM
- Task processing on backend
- Backend completes task
- Trigger "Task Complete" Notification
  - Title: "✓ Task Complete"
  - Message: "Your {task_type} is ready"
  - Type: TASK_COMPLETION
  - Priority: HIGH
  - Action: Deep link to result screen
- User sees notification in bell icon badge
- Taps bell → sees notification list
- Taps notification → navigates to result

### 2. Task Types to Notify

Generation tasks that trigger notifications:
- Text → Image
- Image → Image
- Image → Video
- Text → Video (including Promo Videos)
- Background Replace/Remove
- Photo Enhance
- Compress

### 3. Notification Triggers

#### Task Start Notification
When: User clicks "Generate" button

Display:
- Type Badge: TASK_PROGRESS (blue)
- Title: "Processing Started"
- Message: "Your {task_name} is being generated..."
- Time: Just now
- Action: None (informational)

#### Task Complete Notification
When: Backend completes task

Display:
- Type Badge: TASK_COMPLETION (red/error color)
- Title: "✓ {Task Name} Complete"
- Message: "Your creation is ready to view or download"
- Time: Just now
- Action: Tap to view result

### 4. Implementation Points

Where to Inject Notifications:
1. TextToImageScreen / TextToImageViewModel
   - On "Generate" click → sendTaskStartNotification()
   - On result received → sendTaskCompleteNotification()

2. ImageToImageScreen / ImageToImageViewModel
   - Same pattern

3. ImageToVideoScreen / ImageToVideoViewModel
   - Same pattern

4. BgStudioScreen (Remove/Replace)
   - Same pattern

5. PhotoEnhanceScreen
   - Same pattern

6. CompressScreen
   - Same pattern

### 5. Database Changes

Add optional fields to NotificationEntity:
- taskId: String? (Reference to task)
- resultId: String? (Reference to result)

### 6. NotificationManager Extensions

Add new methods:
- sendTaskStartNotification(taskName, taskId)
- sendTaskCompleteNotification(taskName, message, resultId, thumbnail, taskId)

### 7. User Experience

- t=0s: User clicks Generate
  - Start notification created (MEDIUM priority)
  - Bell badge shows "1"
  
- t=30s: Backend completes
  - Complete notification created (HIGH priority)
  - Bell badge shows "2"
  - User sees notification in list
  
- User taps notification
  - Navigates to result screen with deep link
  - Notification auto-marked as read

### 8. Edge Cases

- User closes app during generation → Notifications still created
- Multiple tasks in progress → Each gets its own notification
- Task fails → Send error notification
- User never opens notifications → Persist in database, cleared after 30 days

## Summary

Key Changes:
1. Add taskStartNotification() and taskCompleteNotification() methods
2. Add taskId and resultId fields to NotificationEntity
3. Inject NotificationManager into generation ViewModels
4. Call notification methods at task start and completion
5. Wire deep links to navigate to result screens

