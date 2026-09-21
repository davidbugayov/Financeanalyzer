#!/bin/zsh

# Build verification script after fixing DefaultArtifactPublicationSet error

echo "================================================"
echo "Gradle Build Verification After KMP Fix"
echo "================================================"
echo ""

echo "1. Checking Gradle version..."
./gradlew --version

echo ""
echo "2. Cleaning previous build..."
./gradlew clean

echo ""
echo "3. Building shared module..."
./gradlew :shared:build --stacktrace

echo ""
echo "4. Checking build result..."
if [ $? -eq 0 ]; then
    echo "✅ SUCCESS: Build completed without DefaultArtifactPublicationSet error!"
    echo ""
    echo "5. Building XCFramework for iOS..."
    ./gradlew :shared:assembleXCFramework

    if [ $? -eq 0 ]; then
        echo "✅ SUCCESS: XCFramework built successfully!"
        echo "   Location: shared/build/XCFrameworks/release/shared.xcframework"
    fi
else
    echo "❌ FAILURE: Build failed. Check the error messages above."
    echo ""
    echo "If you see DefaultArtifactPublicationSet error again:"
    echo "1. Ensure you've pulled the latest changes"
    echo "2. Run: ./gradlew clean --no-configuration-cache"
    echo "3. Delete: rm -rf .gradle build-cache"
    echo "4. Try again: ./gradlew :shared:build"
fi

echo ""
echo "================================================"
echo "Verification Complete"
echo "================================================"
