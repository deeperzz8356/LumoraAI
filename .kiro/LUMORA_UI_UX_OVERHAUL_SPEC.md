# LumoraAI UI/UX Overhaul Specification

## Overview
This spec outlines the implementation for a consistent, responsive UI across the entire LumoraAI app. The goal is to create a unified experience with mobile-first responsive layouts, persistent navigation elements, consistent branding placement, and improved user accessibility to authentication features.

## Requirements Summary
1. **Responsive Mobile-first Layouts**: All pages automatically fit content to available screen space
2. **Fixed Top Header on All Pages**: Display user credits (left side) and notifications (right side)
3. **Bottom Navigation Bar**: Visible only on mobile devices (<600dp screen width)
4. **Consistent Branding**: Remove "LumoraAI" text from individual section titles
5. **"Powered by LumoraAI" Footer**: Display on every page (except auth screens) at the bottom
6. **Login/Signup Access**: Add quick-access authentication button in Settings

## Technical Architecture

### 1. AppShell Component (Task #1)
**Location**: `app/src/main/java/com/deep/lumoraai/core/components/AppShell.kt`
**Purpose**: Wrapper composable that provides consistent layout structure for all main screens

**Implementation Details**:
```kotlin
@Composable
fun AppShell(
    title: String,
    modifier: Modifier = Modifier,
    onNotifications: () -> Unit,
    onNavigate: (String) -> Unit,
    currentRoute: String,
    userCredits: Int = 0, // Will be fetched from ViewModel
    unreadNotificationCount: Int = 0, // From NotificationViewModel
    showBottomNav: Boolean = true, // Control based on screen size
    content: @Composable (PaddingValues) -> Unit
)
```

**Responsive Logic**:
- Use `LocalConfiguration.current.screenWidthDp` to detect screen size
- Show bottom navigation only when `screenWidthDp < 600` (Material3 mobile breakpoint)
- Apply appropriate padding: `navigationBarsPadding()`, `statusBarsPadding()`
- Use Scaffold with: TopBar = UserHeaderBar, BottomBar = BottomNavigationBar (conditional), Footer (built-in)

### 2. UserHeaderBar Component (Task #2)
**Location**: `app/src/main/java/com/deep/lumoraai/core/components/UserHeaderBar.kt`
**Purpose**: Unified top bar showing user credits (left) and notifications (right)

**Implementation Details**:
```kotlin
@Composable
fun UserHeaderBar(
    title: String,
    userCredits: Int,
    unreadNotificationCount: Int,
    onNotifications: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

**Layout**:
```
[← Back] [Page Title]                    [💰 500 Credits] [🔔(3)]
  Left side: Page title (centered)
  Right side: Credits badge + Notification bell with count
```

**Styling**:
- Credits badge: Green accent color, rounded corner
- Notification bell: Existing AppToolbar style with red badge
- Typography: Material3 headlineSmall for title

### 3. BottomNavigationBar Updates (Task #3)
**Current File**: `app/src/main/java/com/deep/lumoraai/core/components/PrimaryButton.kt`
**Changes Needed**:
1. Add screen width detection
2. Conditionally render based on screen size
3. Adjust spacing/font sizing for small screens

**Responsive Logic**:
```kotlin
val configuration = LocalConfiguration.current
val screenWidth = configuration.screenWidthDp
val showBottomNav = screenWidth < 600

if (showBottomNav) {
    BottomNavigationBar(...) // Existing implementation
}
```

### 4. AppFooter Component (Task #4)
**Location**: `app/src/main/java/com/deep/lumoraai/core/components/AppFooter.kt`
**Purpose**: Consistent footer text across all pages

**Implementation**:
```kotlin
@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Powered by LumoraAI",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
```

### 5. Navigation System Integration (Task #5)
**File**: `app/src/main/java/com/deep/lumoraai/core/navigation/NavGraph.kt`

**Integration Strategy**:
1. **Wrap main screens** with AppShell: Home, CreateHub, Settings, Profile, History, etc.
2. **Exclude auth screens**: Splash, Language, Onboarding, Auth
3. **Pass navigation callbacks** through AppShell to individual screens

**Example Implementation**:
```kotlin
composable(Screen.Home.route) { backStackEntry ->
    AppShell(
        title = Screen.Home.title,
        currentRoute = Screen.Home.route,
        onNotifications = { navController.goTo(Screen.Notifications.route) },
        onNavigate = { navController.goTo(it) }
    ) { padding ->
        HomeRoute(onNext = next(Screen.Home), onNavigate = { navController.goTo(it) })
    }
}
```

### 6. Screen Title Cleanup (Task #6)
**Scope**: Audit all 20+ screen files
**Current Pattern**: Many screens have "LumoraAI" in titles or hardcoded branding
**Goal**: Use only the page name from `Screen.title`

**Files to Update** (partial list):
- `HomeScreen.kt`, `SettingsScreen.kt`, `ProfileScreen.kt`, `CreateHubScreen.kt`
- `TextToImageScreen.kt`, `TextToVideoScreen.kt`, `ImageToImageScreen.kt`
- `TemplatesScreen.kt`, `AIToolsScreen.kt`, `HistoryScreen.kt`
- `CreditsScreen.kt`, `NotificationsScreen.kt`, `ResultScreen.kt`
- `QueueScreen.kt`, `SubscriptionScreen.kt`

**Update Pattern**:
```kotlin
// Before: AppToolbar(title = "LumoraAI - Text to Image")
// After:  AppToolbar(title = Screen.TextToImage.title) // "Text To Image"
```

### 7. Settings Authentication Button (Task #7)
**File**: `app/src/main/java/com/deep/lumoraai/feature/settings/SettingsScreen.kt`

**Implementation**:
Add a new setting item in the settings list:
```kotlin
SettingsItem(
    title = "Login / Signup",
    subtitle = "Access your account or create new one",
    icon = Icons.Default.AccountCircle,
    onClick = { onNavigate(Screen.Auth.route) }
)
```

### 8. Testing Strategy (Task #8)
**Preview Composables** to create:
1. Phone portrait (width < 600dp)
2. Phone landscape (height < 600dp but width > 600dp)
3. Tablet portrait (width ≥ 600dp)
4. Tablet landscape (width ≥ 840dp)

**Test Scenarios**:
- Verify bottom nav appears/hides correctly
- Check footer positioning across screen sizes
- Validate top bar scaling and spacing
- Test navigation flows (with/without AppShell)

## Implementation Order
1. **Task #1**: AppShell component (foundation)
2. **Task #2**: UserHeaderBar component
3. **Task #5**: Navigation integration (verify AppShell works)
4. **Task #3**: BottomNavigationBar updates
5. **Task #4**: AppFooter component
6. **Task #6**: Screen title cleanup
7. **Task #7**: Settings authentication button
8. **Task #8**: Responsive testing

## Success Criteria
- [ ] All main screens use AppShell wrapper
- [ ] Top header shows user credits + notifications on all pages (except auth)
- [ ] Bottom navigation appears only on mobile (<600dp width)
- [ ] "Powered by LumoraAI" footer visible on all main pages
- [ ] No redundant "LumoraAI" text in screen titles
- [ ] Settings has login/signup access button
- [ ] Layout adapts correctly to phone, tablet, landscape orientations
- [ ] Existing functionality remains intact

## Notes
- Reuse existing NotificationViewModel for unread count
- Consider creating UserCreditsViewModel for credit balance
- Maintain backward compatibility during migration
- Test thoroughly on different API levels