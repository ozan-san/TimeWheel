# TimeWheel

An iOS-style scrolling time picker for Jetpack Compose.

Two snapping wheels (hour and minute) with a highlighted center selection band
and a subtle alpha/scale falloff away from center.

## Modules

- **`:timewheel`** — the reusable Compose library exposing the `TimeWheel` composable.
- **`:app`** — a demo app that hosts `TimeWheel` in `MainActivity`.

## Install

Available via [JitPack](https://jitpack.io).

**1. Add the JitPack repository** (`settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

**2. Add the dependency** (module `build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.github.ozan-san.TimeWheel:timewheel:<tag>")
}
```

Replace `<tag>` with a released version (a git tag / GitHub release), e.g. `1.0.0`.

## Usage

```kotlin
TimeWheel(
    initialHour = 9,
    initialMinute = 41,
    is24Hour = true,
) { hour, minute ->
    // called whenever the selection settles
}
```

### Parameters

| Parameter       | Type                  | Default | Description                              |
|-----------------|-----------------------|---------|------------------------------------------|
| `modifier`      | `Modifier`            | `Modifier` | Layout modifier.                      |
| `initialHour`   | `Int`                 | `0`     | Starting hour.                           |
| `initialMinute` | `Int`                 | `0`     | Starting minute.                         |
| `is24Hour`      | `Boolean`             | `true`  | 24-hour (0–23) vs 12-hour wheel.         |
| `visibleCount`  | `Int`                 | `5`     | Rows visible at once (center + falloff). |
| `itemHeight`    | `Dp`                  | `40.dp` | Height of each row.                      |
| `onTimeChange`  | `(hour, minute) -> Unit` | no-op | Fires when a wheel snaps to a value.   |

## Build

```sh
./gradlew :app:assembleDebug
```

Requires the Android SDK; minSdk 24.
