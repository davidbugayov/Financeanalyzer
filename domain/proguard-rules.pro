# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep domain models
-keep class com.davidbugayov.financeanalyzer.domain.model.** { *; }
-keep class com.davidbugayov.financeanalyzer.domain.repository.** { *; } 