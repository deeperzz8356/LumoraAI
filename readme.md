# Lumora AI

Lumora AI is an Android app built with Jetpack Compose and a clean, feature-based architecture. This repository currently focuses on the foundation layer for Phase 2: UI structure, navigation, reusable components, and fake local data. There is no Firebase, AI integration, Room, Retrofit, or network dependency in this phase.

## Tech Stack

## Paid billing security

The Android app uses Google Play Billing for `SUBS` and `INAPP` products. It
never sends client-controlled credit amounts. The `backend/` FastAPI service
is the verification boundary: it authenticates Firebase ID tokens, verifies
purchase tokens with the Google Play Developer API, maps product IDs to
server-side entitlements, and records purchase tokens uniquely before
fulfillment. Configure its service-account paths, package name, catalog JSON,
and database path as documented in `backend/README.md`; deploy it behind TLS.
Do not enable paid access until this endpoint is deployed and tested.

## Tester unlimited access

To grant unlimited credits to specific Firebase email accounts, add a comma-separated
allowlist to `local.properties` before building:

```properties
TESTER_EMAILS=tester@example.com,another.tester@example.com
```

This only grants the tester entitlement after the user signs in with Firebase.
Do not put passwords in the repository or in `local.properties`; create tester
accounts in Firebase Authentication and share credentials securely.

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle
- Navigation Compose
- Gradle Kotlin DSL
- Android SDK 35
- Min SDK 24
- Java 11 / Kotlin JVM 11

## Architecture

The app is organized around a layered package structure:

- `core/` for shared UI, navigation, theme, utilities, constants, and design system pieces
- `feature/` for screen-level feature modules
- `data/` for fake local data sources and models
- `domain/` for future business logic contracts
- `di/` for future dependency injection modules

### Core Modules

- `core/components` - reusable UI components such as buttons, toolbars, search fields, dialogs, sheets, loading states, and progress indicators
- `core/navigation` - screen routes, navigation graph, and navigation helpers
- `core/theme` - colors, typography, shapes, spacing, and dimensions
- `core/utils` - shared utility helpers
- `core/constants` - app-wide constants
- `core/common` - shared base types and common models
- `core/extension` - Kotlin extension functions
- `core/designsystem` - design system primitives and tokens

### Data and Domain

- `data/local` - local-only data sources
- `data/remote` - reserved for future API sources
- `data/repository` - fake repository implementation used for UI development
- `data/model` - fake models used to drive screens
- `domain/model` - future domain models
- `domain/repository` - future repository contracts
- `domain/usecase` - future business use cases

### DI Modules

- `di/AppModule.kt`
- `di/NetworkModule.kt`
- `di/RepositoryModule.kt`
- `di/StorageModule.kt`
- `di/DatabaseModule.kt`

## Features

The repository includes these feature modules:

- `auth`
- `createhub`
- `credits`
- `discover`
- `history`
- `home`
- `imagetovideo`
- `notifications`
- `onboarding`
- `profile`
- `queue`
- `result`
- `settings`
- `splash`
- `subscription`
- `templates`
- `texttoimage`
- `texttovideo`
- `tools`

## Screen Flow

The current navigation flow is:

`Splash -> Onboarding -> Home -> Create Hub -> Text To Image -> Image To Video -> Text To Video -> Templates -> Queue -> Result -> History -> Credits -> Notifications -> Subscription -> Profile -> Settings`

## Fake Data

The app uses fake in-memory data to keep the foundation layer isolated from backend and storage work.

Included fake models include:

- `TemplateModel`
- `HistoryModel`
- `NotificationModel`
- `CreditModel`
- `QueueModel`
- `ResultModel`
- `ProfileModel`

The fake repository currently exposes sample methods such as:

- `getTemplates()`
- `getHistory()`
- `getCredits()`
- `getNotifications()`
- `getQueue()`
- `getResults()`
- `getProfile()`

## UI System

The app has a reusable Compose UI layer built from shared components and theme tokens.

### Shared Components

- Primary and secondary buttons
- Gradient button
- App toolbar
- Bottom navigation bar
- Search bar
- Prompt text field
- Loading, empty, and error states
- Dialog and bottom sheet wrappers
- Progress bar
- Tool card

### Theme

The theme layer includes:

- Color palette
- Typography scale
- Shapes
- Spacing tokens
- Dimension tokens

## Previews

Each screen is designed to support preview variants for development and design review, including:

- Light preview
- Dark preview
- Tablet preview
- Landscape preview

## Assets

Stitch export placeholders are organized under:

`app/src/main/assets/stitch/`

Each screen folder contains:

- `design.md`
- `code.html`
- `image.png`

## Build

### Debug APK

```bash
./gradlew assembleDebug
```

### Release APK

```bash
./gradlew assembleRelease
```

### Debug Kotlin Compile

```bash
./gradlew :app:compileDebugKotlin
```

## Notes

- MainActivity only hosts the theme and navigation graph.
- Screens rely on fake local data only.
- This phase intentionally avoids Firebase, AI, Room, and Retrofit.
- The project is ready for Phase 3 Firebase integration without restructuring the app layout.
