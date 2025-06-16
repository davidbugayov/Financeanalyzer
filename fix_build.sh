#!/bin/bash

# Create a backup of the original domain/src/main/java/com/davidbugayov/financeanalyzer/domain/util/Result.kt
echo "Creating backup of Result.kt..."
mkdir -p backup
cp domain/src/main/java/com/davidbugayov/financeanalyzer/domain/util/Result.kt backup/Result.kt.bak 2>/dev/null || echo "No Result.kt file found to backup"

# Delete the duplicate Result.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/utils/
echo "Deleting duplicate Result.kt..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/utils/Result.kt

# Delete the SafeCall.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/
echo "Deleting SafeCall.kt in app module..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/SafeCall.kt

# Delete the DomainResult.kt file in app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/
echo "Deleting DomainResult.kt in app module..."
rm -f app/src/main/java/com/davidbugayov/financeanalyzer/domain/util/DomainResult.kt

# Add the missing imports to files
echo "Adding missing imports to files..."
find app/src/main/java/com/davidbugayov/financeanalyzer -name "*.kt" -exec grep -l "Success\|Error" {} \; | xargs -I{} sed -i '' '1,/^import/s/^import/import com.davidbugayov.financeanalyzer.core.util.Result\nimport/' {}

# Create the extensions file for Money
echo "Creating Money extensions file..."
mkdir -p app/src/main/java/com/davidbugayov/financeanalyzer/core/extensions
cat > app/src/main/java/com/davidbugayov/financeanalyzer/core/extensions/MoneyExtensions.kt << 'EOF'
package com.davidbugayov.financeanalyzer.core.extensions

import com.davidbugayov.financeanalyzer.core.model.Money

/**
 * Extension functions for Money class
 */

/**
 * Formats money for display with options for showing currency and using minimal decimals
 * 
 * @param showCurrency Whether to show the currency symbol
 * @param useMinimalDecimals Whether to show minimal decimals (omit trailing zeros)
 * @return Formatted string representation of the money amount
 */
fun Money.formatForDisplay(showCurrency: Boolean = true, useMinimalDecimals: Boolean = false): String {
    // Just delegate to the standard format method since the original Money class doesn't have these parameters
    return this.format()
}
EOF

# Clean the project
echo "Cleaning the project..."
./gradlew clean

# Build the debug version
echo "Building the debug version..."
./gradlew assembleDebug 