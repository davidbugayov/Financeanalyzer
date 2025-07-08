## 🚀 New Release: Financeanalyzer 2.21.0.fd

Hi F-Droid team! 👋

I've just released **version 2.21.0** of Financeanalyzer with significant improvements and optimizations.

### 📋 Release Summary
- **Version**: 2.21.0.fd  
- **Version Code**: 40
- **Release Tag**: `v2.21.0`
- **APK Size**: ~44MB
- **Status**: ✅ Built and tested successfully

### 🆕 What's New in 2.21.0
- **Achievement Analytics Integration**: Added comprehensive tracking for achievement unlocks
- **Smart Wallet Selection**: Improved UX when adding expenses from wallet screen  
- **Major Performance Optimizations**: Removed unused analytics code (saved 247 lines!)
- **Enhanced UI/UX**: Updated dialogs, localized formatting, improved navigation
- **Bug Fixes**: Fixed wallet selection issues and improved overall stability

### 🛠️ Technical Improvements
- **Code Optimization**: Streamlined analytics from 35+ methods to 15 essential ones
- **Memory Efficiency**: Removed unused infrastructure for better performance
- **Architecture**: Maintained clean code practices and modularity

### ✅ F-Droid Compliance Maintained
- **No Google Services**: Zero Firebase, Google Play Services, or RuStore dependencies
- **Privacy First**: Only AppMetrica for anonymous analytics (can be disabled)
- **Local Data**: All user financial data stays on device
- **Open Source**: GPL-3.0-only license
- **Build Verified**: `./gradlew assembleFdroidDebug` succeeds

### 📁 Updated Files
- `fdroid/metadata/com.davidbugayov.financeanalyzer.yml` ✅ Updated to 2.21.0.fd
- `fdroid/RELEASE_NOTES_2.21.0.md` ✅ Comprehensive changelog  
- `changelog.txt` ✅ Updated with all changes
- Source code optimized and cleaned up

### 🔗 Links
- **Repository**: https://github.com/davidbugayov/Financeanalyzer
- **Release Tag**: https://github.com/davidbugayov/Financeanalyzer/releases/tag/v2.21.0
- **Full Changelog**: See `fdroid/RELEASE_NOTES_2.21.0.md`

The app is ready for F-Droid automatic build system to pick up the new version. All metadata is updated and the build should work seamlessly.

Thank you for maintaining F-Droid! 🙏

---
*Developer: David Bugayov* 