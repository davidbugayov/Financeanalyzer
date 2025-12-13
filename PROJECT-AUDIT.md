# Project Audit — FinanceAnalyzer

Generated: 2025-12-13 (automated, low-risk changes)

## Baseline

- Branch at time of edit: `main`
- Working-tree had unstaged changes (these edits are staged separately and limited to docs/build helpers).
- Latest commit (on `main`): b6a76f68 Add Chinese privacy policy for Huawei AppGallery

## Modules (from `settings.gradle.kts`)

- :app
- :data
- :domain
- :core
- :navigation
- :utils
- :ui
- :shared
- :analytics
- :feature
  - :feature:home
  - :feature:profile
  - :feature:history
  - :feature:statistics
  - :feature:transaction
  - :feature:budget
  - :feature:onboarding
  - :feature:security
- :presentation

## Quick-start (local developer)

1. Ensure you have JDK 17+ (project points to Java 21 in `gradle.properties`) and Git configured.
2. From project root run:

```bash
./gradlew listModules
./gradlew help
```

`listModules` is a small helper task added to the root `build.gradle.kts` that prints included modules.

Notes: full `./gradlew assembleDebug` or `./gradlew lint` may require Android SDK + proper `local.properties`.

## Files added/changed in this hygiene pass

- Added: `PROJECT-AUDIT.md` (this file)
- Edited: `README.md` (small wording/quick-start), `build.gradle.kts` (added `listModules` task), `.gitignore` (added `.env` entry)

## gradle.properties vs gradle.properties.local

- `gradle.properties` contains project-wide defaults and JVM/Gradle configuration. Avoid changing values unless you know global effects (memory, JVM path, flags).
- Use `gradle.properties.local` (or `local.properties`) for machine-specific secrets or overrides (keystore paths, local SDK paths). Do NOT commit secrets.

## Next low-risk recommendations

- Add a CONTRIBUTING.md with a minimal dev setup checklist (JDK, Android SDK, local.properties example).
- Add a CI job that runs `./gradlew lint` and `./gradlew assembleDebug` on a runner with Android SDK.

If you'd like, I can now commit these changes on a dedicated branch and push them upstream in separate commits (one change per commit).
