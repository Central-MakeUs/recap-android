# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
- Codex review on `design/011-ui-flow-2` found two P1 issues in the mock screenshot analysis flow.
- `ScreenshotAnalysisProgressViewModel.startMockAnalysis()` launches work in `viewModelScope` without switching dispatcher. The loop currently calls `ScreenshotImageStorage.copyImageFromUri()`, which opens a `ContentResolver` input stream and copies image bytes synchronously.
- `persistAnalysisResult()` wraps both image copy and Room persistence in broad `runCatching` blocks. This hides persistence failures from state, and also catches `CancellationException`, so cancellation can be swallowed while a new analysis job has already started.
- Existing validation before this handoff: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN.

## Spec
Fix the P1 analysis persistence issues without changing product flow or adding new dependencies.

1. Move blocking screenshot image copy and persistence work off the Main dispatcher.
   - The image copy path must not run on Main.
   - Acceptable approaches:
     - make `ScreenshotImageStorage.copyImageFromUri(...)` a `suspend` function and perform file/content resolver I/O inside `withContext(Dispatchers.IO)`, or
     - keep storage synchronous but call the copy/persistence sequence from a clearly scoped `withContext(Dispatchers.IO)` block in the ViewModel.
   - Keep UI state updates on the ViewModel coroutine in a predictable order.

2. Stop swallowing cancellation and save failures.
   - Do not use broad `runCatching { ... }.getOrNull()` / ignored `runCatching { ... }` for the save path when it can catch `CancellationException`.
   - Cancellation from `analysisJob?.cancel()` must stop the previous job and must not let the old job publish later progress/results.
   - If image copy fails for a single item, the app may still save the analysis result with `storedImagePath = null`, but cancellation must be rethrown.
   - If `screenshotCardRepository.saveAnalysisResults(...)` fails, the job must not report that item as successfully completed. Add a minimal ViewModel error state if needed, using a debug-safe/non-raw message.
   - Do not expose raw exception messages to UI state.

3. Preserve existing behavior unless directly required by the fixes.
   - Empty input should still finish with `isRunning = false` and `progress = 1f`.
   - Successful analysis should still persist one card per selected screenshot and update progress in order.
   - Starting a second job should cancel/reset the previous job.

## Files to Touch
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorage.kt`
- `app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt`
- Related test files only if signature changes require narrow updates.

## Acceptance Criteria
- Screenshot image copying and Room save work no longer run on Main.
- Cancelling an in-progress mock analysis prevents the old job from publishing progress/results after the next job starts.
- `CancellationException` is not swallowed by image copy or persistence error handling.
- A repository save failure is observable in ViewModel state and does not mark the failed item as successfully completed.
- Existing success, empty input, and restart scenarios continue to pass.
- No new production dependency is added.

## Validation
- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`

## Out of scope
- Permission-denied organize screen UX.
- Replacing mock analysis with real AI/OCR analysis.
- Redesigning Home/Collection/Organize UI.
- Thumbnail generation or image cache optimization.

## Technical Debt
- Permission-denied organize screen UX remains deferred. See `docs/BACKLOG.md` Open item dated 2026-07-08 for the P2 follow-up.

## Cursor Result
- Changed files: app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt, app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt
- Build/test: .\gradlew.bat assembleDebug GREEN, .\gradlew.bat testDebugUnitTest GREEN
- Open questions: none
- Codex follow-up: final progress now derives from successful `completedCount / totalCount` instead of forcing `1f`, so Home progress no longer reports 100% after a save failure.

## Codex Review
Blocking:
- none

Nits:
- none

Validation:
- `.\gradlew.bat testDebugUnitTest` GREEN
- `.\gradlew.bat assembleDebug` GREEN

Verdict: DONE
