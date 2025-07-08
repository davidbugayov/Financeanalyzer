# Release Notes - Financeanalyzer 2.21.0

## Version: 2.21.0.fd
## Version Code: 40
## Release Date: December 2024

## Changes in this release:

### New Features
- **Achievement Analytics Integration**: Added comprehensive tracking for achievement unlocks and screen views
- **Smart Wallet Selection**: Improved automatic wallet selection when adding expenses from wallet screen

### UI/UX Improvements
- **Enhanced Wallet Dialogs**: Updated wallet selection dialogs with improved balance display
- **Localized Number Formatting**: All balances and amounts now display in localized format
- **Improved Navigation**: Enhanced screen navigation reliability, fixed parameter handling issues
- **Better UX Flow**: Fixed wallet selection reset issue on transaction screen

### Performance Optimizations
- **Analytics System Cleanup**: Removed unused analytics methods and constants (saved 247 lines of code)
- **Code Efficiency**: Streamlined analytics system from 35+ methods to 15 essential ones
- **Memory Optimization**: Reduced app size by removing unused analytics infrastructure

### Bug Fixes
- Fixed wallet selection reset on transaction addition screen
- Improved stability and reliability across the app
- Enhanced parameter handling in navigation

### Technical Improvements
- Updated to version 2.21.0 (versionCode 40)
- Optimized analytics architecture for better performance
- Maintained clean code practices and modularity

## F-Droid Specific Notes
- This version maintains F-Droid compliance (no Google Services, Firebase, or RuStore dependencies)
- Uses only AppMetrica for anonymous analytics
- All user data remains local and private
- Enhanced achievement system respects user privacy

## Build Information
- Built with Gradle 8.14.2
- Target SDK: 34 (Android 14)
- Minimum SDK: 24 (Android 7.0)
- APK Size: ~27MB

## Repository Information
- Source: https://github.com/davidbugayov/Financeanalyzer
- Tag: v2.21.0
- License: GPL-3.0-only 