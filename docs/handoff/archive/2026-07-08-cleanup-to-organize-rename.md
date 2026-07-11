# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
The project describes the product action as organizing screenshots into searchable cards/collections. Current internal code uses `cleanup`/`Cleanup` for that concept in module names, package names, route names, ViewModel/contracts, string resource keys, OCR range models, and notification-related identifiers.

Recent targeted search found these high-impact internal identifiers:
- `settings.gradle.kts` includes `:feature:cleanup`.
- `app/build.gradle.kts` depends on `project(":feature:cleanup")`.
- `feature/cleanup` uses namespace/package `com.chalkak.recap.feature.cleanup`.
- App navigation imports and routes use `CleanupRoute`, `AppRoute.Cleanup`, and `onNavigateToCleanup`.
- Core OCR/data code uses `OcrCleanupRange` and an `onboarding_ocr_cleanup` work name.
- `core/design` string resource keys and components use `cleanup_*` or `Cleanup*` identifiers for the organize flow and related notifications.

User-facing Korean copy such as `정리`, `정리하기`, and `정리 시작하기` is appropriate product language and should remain unless a compile-required resource key rename forces call-site updates. This task is an internal naming migration, not a UX copy change.

## Spec
Rename the internal screenshot organize concept from `cleanup` to `organize` consistently.

Required changes:
- Rename Gradle module `:feature:cleanup` to `:feature:organize`.
- Move source/test files from `feature/cleanup` to `feature/organize`.
- Change package/namespace from `com.chalkak.recap.feature.cleanup` to `com.chalkak.recap.feature.organize`.
- Rename user-flow code identifiers:
  - `CleanupRoute` -> `OrganizeRoute`
  - `CleanupViewModel` -> `OrganizeViewModel`
  - `CleanupUiState` -> `OrganizeUiState`
  - `CleanupAction` -> `OrganizeAction`
  - `CleanupDestination` -> `OrganizeDestination`
  - `onNavigateToCleanup` -> `onNavigateToOrganize`
  - `onCleanupComplete` -> `onOrganizeComplete`
  - `AppRoute.Cleanup` -> `AppRoute.Organize`
- Rename OCR/data identifiers:
  - `OcrCleanupRange` -> `OcrOrganizeRange`
  - update imports, serializers/valueOf call sites, repository/data source/worker APIs.
  - update internal work name string from `onboarding_ocr_cleanup` to an `organize` equivalent unless there is an existing compatibility reason documented in code.
- Rename resource keys and component/internal names that represent the screenshot organize concept:
  - `cleanup_*` resource names used by the organize flow should become `organize_*`.
  - `bottom_nav_cleanup` should become `bottom_nav_organize`.
  - notification setting/resource/internal identifiers like `cleanupCompleteEnabled` should become `organizeCompleteEnabled`.
  - `CleanupNotificationPermissionBottomSheet` should become `OrganizeNotificationPermissionBottomSheet`.
  - onboarding first-cleanup identifiers should become first-organize identifiers.
- Update all tests and previews to use the new names.
- Keep user-visible Korean string values unchanged unless they are already incorrect for the organize concept. Do not change the visual design.
- Do not introduce new dependencies.
- Do not alter behavior, navigation structure, OCR logic, selection limits, persistence semantics, or UI state beyond compile-required name changes.
- After the migration, `rg -n "cleanup|Cleanup|cleanUp" app core feature settings.gradle.kts build.gradle.kts` should return no hits except for clearly unrelated comments/docs. If any hit must remain for compatibility, add a short code comment explaining why.

## Files to Touch
- `settings.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/java/com/chalkak/recap/app/AppRoute.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- `feature/cleanup/**` -> `feature/organize/**`
- `core/model/src/main/java/com/chalkak/recap/core/model/OcrModels.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/LocalScreenshotDataSource.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/ocr/*.kt`
- `core/design/src/main/res/values/strings.xml`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/bottombar/RecapBottomBar.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/bottomsheet/*Cleanup*.kt` -> organize-named equivalent
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/ComponentGardenScreen.kt`
- `feature/onboarding/src/main/java/com/chalkak/recap/feature/onboarding/**/*.kt`
- `feature/onboarding/src/test/java/com/chalkak/recap/feature/onboarding/**/*.kt`
- `feature/mypage/src/main/java/com/chalkak/recap/feature/mypage/**/*.kt`
- `feature/mypage/src/test/java/com/chalkak/recap/feature/mypage/**/*.kt`

## Acceptance Criteria
- Gradle module name is `:feature:organize`; `:feature:cleanup` is no longer included or depended on.
- Feature package is `com.chalkak.recap.feature.organize`; no production/test source remains under `com.chalkak.recap.feature.cleanup`.
- Core model/data OCR range type is named `OcrOrganizeRange`, and all call sites compile.
- App navigation uses `Organize` naming for the route and callbacks.
- Organize flow ViewModel, contract, route, destination, tests, and previews use `Organize*` naming.
- String resource keys used by this concept use `organize` naming while Korean displayed values remain product-appropriate.
- `rg -n "cleanup|Cleanup|cleanUp" app core feature settings.gradle.kts build.gradle.kts` has no remaining hits unless explicitly documented as intentional compatibility.
- Existing behavior remains unchanged from the user perspective.
- No unrelated formatting/refactor changes are included.

## Validation
Run:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
rg -n "cleanup|Cleanup|cleanUp" app core feature settings.gradle.kts build.gradle.kts
```

Expected:
- Unit tests pass.
- Debug build passes.
- The final `rg` command returns no unintended hits.

## Out of scope
- Changing Korean product copy from `정리` to another term.
- Changing UI layout, colors, icons, previews beyond name/reference updates required by the rename.
- Changing OCR/AI analysis behavior, selection behavior, WorkManager execution logic, Room schema design, or navigation UX.
- Adding dependencies.
- Editing archived handoff docs or broad documentation unrelated to compile/runtime naming.

## Technical Debt
None.

## Cursor Result
- Changed files: settings.gradle.kts, app/build.gradle.kts, app/src/main/java/com/chalkak/recap/app/AppRoute.kt, app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt, app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt, feature/cleanup/** -> feature/organize/** (module rename + Organize* types), core/model/OcrModels.kt, core/data/LocalScreenshotDataSource.kt, core/data/ocr/OcrEntities.kt, core/data/ocr/OcrRepository.kt, core/data/ocr/OcrWorker.kt, core/design/res/values/strings.xml, core/design/component/bottombar/RecapBottomBar.kt, core/design/component/bottomsheet/OrganizeNotificationPermissionBottomSheet.kt, feature/developer/ComponentGardenScreen.kt, feature/onboarding/** (first-organize identifiers), feature/mypage/** (organizeCompleteEnabled)
- Build/test: .\gradlew.bat testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN; rg cleanup/Cleanup/cleanUp in app/core/feature/settings.gradle.kts/build.gradle.kts: no hits
- Open questions: none

## Codex Review
- Blocking: none
- Nits: none
- Verdict: DONE
- Validation: Reviewed latest commit `471916ab34c795b6d16e5c43f32362b02c18f7dd`. Confirmed `rg -n "cleanup|Cleanup|cleanUp" app core feature settings.gradle.kts build.gradle.kts` returns no hits, and `git ls-tree -r --name-only HEAD feature/cleanup feature/organize` shows only `feature/organize/**`. Did not rerun Gradle because Cursor Result records `testDebugUnitTest` and `assembleDebug` GREEN, and the review found no evidence requiring equivalent validation to be repeated.
