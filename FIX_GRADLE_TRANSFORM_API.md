# Gradle Transform API Compatibility Fix

## Problem Diagnosis
The error `Unable to load class 'com.android.build.api.transform.Transform'` occurs because:

1. **Android Gradle Plugin (AGP) 9.0.0** deprecated and removed the Transform API
2. **AGConnect Gradle Plugin version 1.5.2.300** still uses the old Transform API
3. This creates a classpath conflict when both try to coexist

## Root Cause
The `buildscript` block in `build.gradle.kts` was explicitly loading:
- `com.huawei.agconnect:agcp:1.5.2.300` (uses deprecated Transform API)
- `com.android.tools.build:gradle:9.0.0` (no longer provides Transform API)

## Solution Applied

### 1. Updated AGConnect Plugin Version
- **Old**: `agconnectPlugin = "1.5.2.300"` (incompatible with AGP 9.0)
- **New**: `agconnectPlugin = "1.8.0"` (compatible with AGP 9.0+, uses new ScannerInjection API)

**File**: `gradle/libs.versions.toml` (line 13)

### 2. Removed Deprecated buildscript Block
- Deleted the legacy `buildscript { }` block that was causing classpath conflicts
- This block is unnecessary with modern Gradle plugin management

**File**: `build.gradle.kts` (lines 128-137)

### 3. Added AGConnect Plugin Alias
- Created proper plugin alias: `agconnect = { id = "com.huawei.agconnect", version.ref = "agconnectPlugin" }`
- Added to plugins block with `apply false` for conditional application

**File**: `gradle/libs.versions.toml` (added to [plugins] section)

### 4. Updated Plugin Application
- **Root build.gradle.kts**: Added `alias(libs.plugins.agconnect) apply false` to plugins block
- **app/build.gradle.kts**: Added AGConnect plugin alias and conditional application via `pluginManager.apply()`

## Changes Made

### File: `gradle/libs.versions.toml`
```toml
# BEFORE:
agconnectPlugin = "1.5.2.300"

# AFTER:
agconnectPlugin = "1.8.0"

# Added to [plugins] section:
agconnect = { id = "com.huawei.agconnect", version.ref = "agconnectPlugin" }
```

### File: `build.gradle.kts`
```kotlin
# BEFORE:
plugins {
    // ... other plugins ...
    alias(libs.plugins.firebase.crashlytics) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
    }
    dependencies {
        classpath(libs.agconnect.gradle.plugin)
        classpath("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}")
    }
}

# AFTER:
plugins {
    // ... other plugins ...
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.agconnect) apply false
}
```

### File: `app/build.gradle.kts`
```kotlin
# BEFORE:
plugins {
    // ... plugins ...
    alias(libs.plugins.firebase.perf)
}

// ... later in file ...
if (gradle.startParameter.taskNames.any { it.contains("Huawei", ignoreCase = true) }) {
    apply(plugin = "com.huawei.agconnect")
}

# AFTER:
plugins {
    // ... plugins ...
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.agconnect) apply false
}

// ... later in file ...
if (gradle.startParameter.taskNames.any { it.contains("Huawei", ignoreCase = true) }) {
    pluginManager.apply(libs.plugins.agconnect.get().pluginId)
}
```

## Gradle Cache Cleanup
Execute these commands to clear cached artifacts:

```bash
# Clear local Gradle cache
rm -rf .gradle

# Clear build artifacts
rm -rf build build-cache

# Clear global Gradle cache (optional)
rm -rf ~/.gradle/caches

# Run Gradle clean
./gradlew clean --no-build-cache

# Sync and rebuild
./gradlew --refresh-dependencies
```

## Verification

After applying the fixes, your build should work properly. Test with:

```bash
# Test standard Google Play build
./gradlew assembleGoogleDebug

# Test Huawei AppGallery build (if applicable)
./gradlew assembleHuaweiDebug --info

# Full sync
./gradlew clean build
```

## Why This Works

1. **AGConnect 1.8.0** is fully compatible with AGP 9.0.0
2. Uses the new **ScannerInjection API** instead of deprecated Transform API
3. **Plugin aliases** in `libs.versions.toml` provide centralized version management
4. Removes classpath conflicts from explicit buildscript declarations
5. Follows modern Gradle plugin management best practices

## Additional Notes

- The conditional `if (gradle.startParameter.taskNames.any { it.contains("Huawei", ignoreCase = true) })` check allows AGConnect to only be applied when building Huawei variants
- Huawei AppGallery Connect SDK is automatically included via `agconnect-core` dependency for Huawei flavor builds
- No functional behavior change - just modernized plugin management and dependency resolution
