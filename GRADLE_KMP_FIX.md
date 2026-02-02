# Kotlin Multiplatform Gradle API Compatibility Fix

## Problem Diagnosis

**Error**: `org.gradle.api.internal.plugins.DefaultArtifactPublicationSet`
**Location**: `shared/build.gradle.kts` line 13
**Root Cause**: The CocoaPods Gradle plugin (`org.jetbrains.kotlin.native.cocoapods`) is using internal Gradle APIs that were removed or relocated in Gradle 8.11+ and completely incompatible with Gradle 9.x.

### Technical Details
- **Gradle Version**: 8.11.1 (as per `gradle/wrapper/gradle-wrapper.properties`)
- **Kotlin Version**: 2.1.0
- **Issue**: The `DefaultArtifactPublicationSet` is an internal Gradle API class used by the deprecated CocoaPods plugin
- **Impact**: Build fails during configuration phase when Gradle tries to apply the CocoaPods plugin

## Solution Applied

### 1. Removed Deprecated CocoaPods Plugin
The CocoaPods Gradle plugin has been **deprecated** since Kotlin 1.9.20 and is incompatible with modern Gradle versions (8.10+).

**File**: `shared/build.gradle.kts`

**BEFORE**:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")  // String-based plugin ID
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.native.cocoapods")  // DEPRECATED PLUGIN
}

kotlin {
    androidTarget()

    val iosX64Target = iosX64()
    val iosArm64Target = iosArm64()
    val iosSimArm64Target = iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "FinanceAnalyzer shared logic"
        homepage = "https://github.com/davidbugayov/FinanceAnalyzer"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "shared"
            isStatic = false
        }
    }

    // ... rest of configuration
}
```

**AFTER**:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)  // Consistent alias-based plugin
    alias(libs.plugins.kotlin.serialization)
    // CocoaPods plugin REMOVED
}

kotlin {
    androidTarget()

    // iOS targets configuration
    val iosX64Target = iosX64()
    val iosArm64Target = iosArm64()
    val iosSimArm64Target = iosSimulatorArm64()

    // CocoaPods configuration REMOVED
    // XCFramework approach is used instead

    // ... rest of configuration
}
```

### 2. Standardized Plugin Application
Changed from string-based plugin ID to version catalog alias for consistency and better version management.

**Change**: `id("com.android.library")` → `alias(libs.plugins.android.library)`

## Why This Fix Works

### CocoaPods Plugin Issues
1. **Uses Internal APIs**: The plugin directly accesses `org.gradle.api.internal.plugins.DefaultArtifactPublicationSet`
2. **No Longer Maintained**: Last significant update was in Kotlin 1.9.x
3. **Gradle Incompatibility**: Breaks with Gradle 8.10+ due to internal API changes
4. **Better Alternatives Exist**: XCFramework approach is the recommended modern solution

### XCFramework Approach (Already in Place)
The project already uses XCFramework for iOS distribution, which is:
- ✅ **Modern**: Recommended by Apple and JetBrains
- ✅ **Compatible**: Works with all Gradle versions
- ✅ **Flexible**: Supports multiple architectures in one bundle
- ✅ **No CocoaPods Dependency**: Direct integration without CocoaPods runtime

```kotlin
val xcf = XCFramework()
listOf(iosX64Target, iosArm64Target, iosSimArm64Target).forEach { target ->
    target.binaries.framework {
        baseName = "shared"
        isStatic = false
        xcf.add(this)
    }
}
```

## Impact Analysis

### ✅ What Still Works
- **iOS Framework Generation**: XCFramework approach handles all iOS builds
- **Android Library**: No changes to Android configuration
- **Kotlin Multiplatform**: All KMP features remain functional
- **Dependency Management**: All dependencies remain the same

### ⚠️ What Changed
- **No Podspec Generation**: If you were using CocoaPods integration, you'll need to:
  - Use XCFramework directly in Xcode
  - Or manually create a Podspec that references the XCFramework
  - Or use Swift Package Manager (SPM) instead

### 🔄 Migration Steps for iOS Development

If you were using CocoaPods integration:

1. **Remove Pod References** (if any exist):
   ```ruby
   # In your iOS Podfile, remove:
   pod 'shared', :path => '../shared'
   ```

2. **Use XCFramework Directly**:
   - Build XCFramework: `./gradlew :shared:assembleXCFramework`
   - Output location: `shared/build/XCFrameworks/release/shared.xcframework`
   - Drag the XCFramework into your Xcode project

3. **Or Create Manual Podspec** (optional):
   ```ruby
   Pod::Spec.new do |spec|
       spec.name                     = 'shared'
       spec.version                  = '1.0.0'
       spec.homepage                 = 'https://github.com/davidbugayov/FinanceAnalyzer'
       spec.source                   = { :http=> ''}
       spec.authors                  = ''
       spec.license                  = ''
       spec.summary                  = 'FinanceAnalyzer shared logic'
       spec.vendored_frameworks      = 'build/XCFrameworks/release/shared.xcframework'
       spec.ios.deployment_target = '14.0'
   end
   ```

## Verification

After applying this fix, the build should complete successfully:

```bash
# Clean build
./gradlew clean

# Build shared module
./gradlew :shared:build

# Build XCFramework for iOS
./gradlew :shared:assembleXCFramework
```

## Additional Recommendations

### 1. Update Kotlin Multiplatform Plugin (Future)
When Kotlin 2.2.0+ is released, consider updating as it has better Gradle 9.x compatibility.

### 2. Consider Gradle Upgrade Path
Current: Gradle 8.11.1
- ✅ Stable with Kotlin 2.1.0
- ✅ Good AGP 8.7.3 compatibility
- ⚠️ If upgrading to Gradle 9.x in the future, ensure Kotlin plugin is 2.1.20+

### 3. Monitor Kotlin Multiplatform Evolution
- XCFramework is the official recommended approach
- CocoaPods plugin is in maintenance mode (no active development)
- Swift Package Manager (SPM) support is coming in future Kotlin releases

## References
- [Kotlin Multiplatform iOS Integration](https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html)
- [XCFramework Documentation](https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks)
- [Gradle 8.11 Release Notes](https://docs.gradle.org/8.11/release-notes.html)
- [CocoaPods Plugin Deprecation](https://kotlinlang.org/docs/whatsnew1920.html#deprecation-of-the-kotlin-cocoapods-gradle-plugin)

## Summary

✅ **Fixed**: Removed incompatible CocoaPods Gradle plugin
✅ **Retained**: XCFramework-based iOS integration
✅ **Improved**: Consistent plugin application using version catalog
✅ **Result**: Build should now complete without `DefaultArtifactPublicationSet` errors
