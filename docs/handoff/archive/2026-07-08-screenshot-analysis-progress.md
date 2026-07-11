# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
- User request: from `feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotConfirmationScreen.kt`, pressing the "정리 시작하기" button should no longer show the unimplemented placeholder snackbar. It should start mock screenshot analysis and move to the Home screen.
- Current behavior is implemented in `OrganizeRoute`, not directly inside `ScreenshotConfirmationScreen`: `onStartOrganizingClick` launches a coroutine, shows `organize_start_placeholder_message`, then calls `onOrganizeComplete()`.
- `docs/SCREENSHOT_MOCK_DATA.md` defines the mock screenshot analysis repository contract. The implementation already exists in `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepository.kt`, bound as `ScreenshotAnalysisRepository`.
- `ScreenshotAnalysisInput` only needs `fileName`; use the selected `LocalImage.displayName` values in the same order as `uiState.selectedUris`.
- The app currently has a modular structure (`:app`, `:feature:organize`, `:feature:home`, `:core:data`, etc.) even though `docs/PROJECT.md` still describes the older single-module state. Follow the actual module layout.
- Home is rendered through `RecapMainScreen` -> `RecapMainTabNavHost` -> `HomeRoute` -> `HomeScreen`. `RecapMainScreen` owns the main tab back stack, so navigating to Home after organize completion requires a signal into `RecapMainScreen`, not only popping `AppRoute.Organize`.

## Spec
1. Remove the placeholder snackbar behavior for starting organize.
   - Delete the `startPlaceholderMessage` usage and the `snackbarHostState.showSnackbar(startPlaceholderMessage)` call from `OrganizeRoute`.
   - Keep the existing max-selection snackbar behavior.
   - Remove `organize_start_placeholder_message` from `core/design/src/main/res/values/strings.xml` only if it becomes unused.

2. Pass selected screenshots upward when organize starts.
   - In `OrganizeRoute`, compute the selected screenshots in the same order used by `ScreenshotConfirmationScreen`: filter `uiState.availableScreenshots` by `uiState.selectedUris`, then sort by each URI's index in `uiState.selectedUris`.
   - Change `OrganizeRoute`'s completion callback from `onOrganizeComplete: () -> Unit` to `onOrganizeComplete: (List<LocalImage>) -> Unit`.
   - On "정리 시작하기", when `uiState.canProceed` is true, call `onOrganizeComplete(selectedScreenshots)` directly. Do not wait for analysis before returning to Home.

3. Add app-level mock analysis progress state.
   - Add a small app-scoped ViewModel in `app/src/main/java/com/chalkak/recap/app/` that injects `ScreenshotAnalysisRepository`.
   - The ViewModel should expose immutable UI state through `StateFlow`.
   - Suggested state fields:
     - `isRunning: Boolean`
     - `completedCount: Int`
     - `totalCount: Int`
     - `progress: Float` derived or stored as `completedCount / totalCount`, clamped to `0f..1f`
     - optional `results: List<ScreenshotAnalysisResult>` for future display work, but do not render these results in this task.
   - Add a `startMockAnalysis(images: List<LocalImage>)` function.
   - For each selected image, wait `500L` milliseconds, then call `ScreenshotAnalysisRepository.analyze(ScreenshotAnalysisInput(fileName = image.displayName))`.
   - Update progress after each image completes.
   - If `startMockAnalysis` is called while a previous mock analysis job is running, cancel the previous job and start a new one with progress reset to `0 / newTotal`.
   - On completion, progress should reach `1f`. Hide the Home progress UI after completion; do not show result data yet.
   - No WorkManager, OCR, Firebase AI, Room persistence, notification, or real image decoding is part of this task.

4. Navigate back to the Home tab after starting mock analysis.
   - In `RecapNavHost`, when `OrganizeRoute` completes:
     - start mock analysis with the selected screenshots,
     - pop `AppRoute.Organize`,
     - signal `RecapMainScreen` to select `MainTabRoute.Home`.
   - Because `RecapMainScreen` owns the tab back stack, add a simple one-way request signal such as an incrementing `homeNavigationRequestId: Int`.
   - `RecapMainScreen` should observe that request with `LaunchedEffect(homeNavigationRequestId)` and switch its internal main tab back stack to `MainTabRoute.Home` when the value changes.
   - Avoid recreating the main tab back stack unnecessarily.

5. Show a simple progress bar at the top of the Home screen while analysis is running.
   - Add a home-facing progress UI model in `feature/home/src/main/java/com/chalkak/recap/feature/home/HomeContract.kt` or pass equivalent primitive values into `HomeScreen`.
   - `HomeRoute` and `HomeScreen` should accept the analysis progress from the app layer without making `feature:home` depend on `:app`.
   - In `HomeScreen`, render a compact `LinearProgressIndicator` near the top of the screen, before existing Home sections, only while analysis is running.
   - Use Material 3 / existing theme colors. Add any visible text only if useful, and define it in `core/design/src/main/res/values/strings.xml`.
   - Add or update the Home preview to cover the running-progress state, wrapped in `RECAPTheme`.

6. Keep module boundaries clean.
   - `:app` may depend on `:core:data`, `:core:model`, `:feature:organize`, and `:feature:home`.
   - `:feature:home` should not depend on `:core:data` just to show progress.
   - `:feature:organize` does not need to depend on `ScreenshotAnalysisRepository`; analysis should be triggered by the app-level owner after the selected images are handed upward.

## Files to Touch
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- New app-level progress ViewModel/state file under `app/src/main/java/com/chalkak/recap/app/`
- `feature/organize/src/main/java/com/chalkak/recap/feature/organize/OrganizeRoute.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/HomeRoute.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/HomeScreen.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/HomeContract.kt`
- `core/design/src/main/res/values/strings.xml` if adding progress text or removing the unused placeholder string
- Tests as appropriate:
  - `app/src/test/java/com/chalkak/recap/app/<ProgressViewModelName>Test.kt`
  - existing home/organize tests only if signatures require updates

## Acceptance Criteria
- Pressing "정리 시작하기" on the confirmation screen no longer shows the unimplemented placeholder snackbar.
- Pressing "정리 시작하기" with a valid selection immediately returns the user to the main Home tab.
- The selected screenshots are analyzed through `ScreenshotAnalysisRepository` using `ScreenshotAnalysisInput(fileName = LocalImage.displayName)`.
- Mock analysis takes `0.5` seconds per selected image, updates progress after each image, and preserves the selected image order.
- While mock analysis is running, Home shows a compact progress bar at the top of the Home content.
- After mock analysis finishes, no analysis result cards/details are displayed yet.
- Existing max-selection snackbar behavior still works.
- No new production dependency is added.
- UI strings are resource-backed if any new visible text is introduced.
- Compose previews compile and use `RECAPTheme`.

## Validation
- Run unit tests after implementation:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
  ```
- Run debug build:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
  ```
- Unit test expectations:
  - app-level progress ViewModel starts at idle state.
  - `startMockAnalysis` sets running state and total count.
  - advancing coroutine test time by `500ms` per image increments completed count by one.
  - repository inputs use selected image display names in order.
  - starting a second job cancels/resets the previous job.

## Out of scope
- Rendering mock analysis result data on Home or any detail screen.
- Persisting analysis results to Room/DataStore.
- Real OCR, Firebase AI, image decoding, upload, or network work.
- WorkManager/background execution.
- Notifications or completion snackbars.
- Redesigning Home sections or changing existing mock Home cards.

## Technical Debt
- none

## Cursor Result
- Changed files: app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt, app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt, app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt, feature/organize/src/main/java/com/chalkak/recap/feature/organize/OrganizeRoute.kt, feature/home/src/main/java/com/chalkak/recap/feature/home/HomeContract.kt, feature/home/src/main/java/com/chalkak/recap/feature/home/HomeRoute.kt, feature/home/src/main/java/com/chalkak/recap/feature/home/HomeScreen.kt, core/design/src/main/res/values/strings.xml, app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt
- Build/test: .\gradlew.bat assembleDebug GREEN, .\gradlew.bat testDebugUnitTest GREEN
- Open questions: none

## Codex Review
- Blocking: none
- Nits: none
- Verdict: DONE
- Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN
