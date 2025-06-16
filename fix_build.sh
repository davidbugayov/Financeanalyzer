#!/bin/bash

# Create a backup of the original domain/src/main/java/com/davidbugayov/financeanalyzer/domain/util/Result.kt
echo "Creating backup of Result.kt..."
mkdir -p backup
cp domain/src/main/java/com/davidbugayov/financeanalyzer/domain/util/Result.kt backup/Result.kt.bak

# Delete the duplicate Result.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/utils/
echo "Deleting duplicate Result.kt..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/utils/Result.kt

# Delete the SafeCall.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/
echo "Deleting SafeCall.kt in app module..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/SafeCall.kt

# Delete the DomainResult.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/
echo "Deleting DomainResult.kt in app module..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/DomainResult.kt

# Clean the project
echo "Cleaning the project..."
./gradlew clean

# Build the debug version
echo "Building the debug version..."
./gradlew assembleDebug 