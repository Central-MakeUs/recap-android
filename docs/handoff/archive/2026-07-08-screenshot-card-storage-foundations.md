# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
- RECAP stores user-selected or shared screenshots after OCR/AI analysis and will manage them as cards/collections.
- Current mock analysis contract is documented in `docs/SCREENSHOT_MOCK_DATA.md`.
- Current mock analysis output is `ScreenshotAnalysisResult` in `:core:model` and is produced by `MockScreenshotAnalysisRepository` in `:core:data`.
- Current mock analysis results are only kept in `ScreenshotAnalysisProgressViewModel` memory state; they are not persisted.
- Current `RecapDatabase` only contains OCR tables: `ocr_jobs`, `ocr_results`.
- `UserPreferencesRepository` currently owns a private `Context.userPreferencesDataStore` delegate for `user_preferences` and only exposes onboarding completion. Keep its public behavior mostly unchanged.
- Room, Preference DataStore, Hilt, KSP, and Room test dependencies already exist in the project.

## Spec
Implement storage foundations for analyzed screenshot cards, settings DataStore access, and future image persistence.

1. Update mock screenshot analysis contract before implementation details:
   - Add a screenshot-level favorite boolean to `docs/SCREENSHOT_MOCK_DATA.md`.
   - JSON-equivalent field name: `is_favorite`.
   - Kotlin field name: `ScreenshotAnalysisResult.isFavorite`.
   - Default mock generation rule: `false` unless a test-provided/randomizer path explicitly supplies another value.
   - Update the field mapping table, mock generation rules, implementation location notes, and scope notes so Room/DataStore/image storage are no longer listed as fully out of scope for this task.

2. Extend screenshot analysis domain/model:
   - Add `isFavorite: Boolean` to `ScreenshotAnalysisResult`.
   - Update all production, preview, and test constructors.
   - Update `MockScreenshotAnalysisRepository` so generated results include `isFavorite = false`.
   - Keep existing `imageId`, `title`, `summary`, `contentTypes`, `keyFields`, and `confidence` behavior unchanged.

3. Add Room persistence for analyzed screenshot cards:
   - Create Room entities under a focused package such as `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/`.
   - Use one parent table for screenshot cards and one child table for key fields.
   - Parent table requirements:
     - Table name: `screenshot_cards`.
     - Primary key: `imageId: String`.
     - Columns for `sourceImageUri: String?`, `storedImagePath: String?`, `thumbnailPath: String?`.
     - Columns for `title`, `summary`, `primaryContentType`, `confidence`, `isFavorite`.
     - Columns for `createdAtMillis` and `updatedAtMillis`.
   - Child table requirements:
     - Table name: `screenshot_key_fields`.
     - Auto-generated primary key is acceptable.
     - Foreign key to `screenshot_cards.imageId` with cascade delete.
     - Columns for `imageId`, `label`, `value`, `displayPriority`, `isSensitive`.
     - Add an index on `imageId`.
   - Add a relation holder for reading a card with its key fields.
   - Add mapper functions between Room rows and `ScreenshotAnalysisResult`. Include image reference fields in a small data class if needed, without forcing them into `ScreenshotAnalysisResult`.
   - Add a DAO with at least:
     - Observe all cards ordered by `createdAtMillis DESC`.
     - Get one card by `imageId`.
     - Transactional save/upsert of a list of analysis results, replacing key fields for each saved card.
     - Update favorite state by `imageId`.
     - Delete by `imageId`.
   - Add a repository facade, for example `ScreenshotCardRepository`, that hides DAO details and exposes suspend/Flow APIs for saving a list of `ScreenshotAnalysisResult`, observing stored cards, updating favorite state, and deleting cards.
   - Register new entities and DAO in `RecapDatabase` and `DatabaseModule`.
   - Increase database version and add a non-destructive Room migration from the current version to the new version that creates the two new tables.

4. Add singleton provisioning for the existing PreferenceDataStore without rewriting settings behavior:
   - Preserve `UserPreferencesRepository` external behavior: `onboardingCompleted` Flow and `setOnboardingCompleted(completed)`.
   - Do not create a second/new PreferenceDataStore.
   - Keep using the existing Preferences DataStore name: `user_preferences`.
   - Move the existing `Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")` ownership out of the repository-private scope and into a reusable provider/module or equivalent singleton owner.
   - Provide that same `DataStore<Preferences>` through Hilt so the app has one canonical injected instance for `user_preferences`.
   - Prefer injecting `DataStore<Preferences>` into `UserPreferencesRepository` over injecting `Context`, while keeping the repository API unchanged.
   - Do not introduce Proto DataStore for this task.
   - Add only minimal setting keys if needed for compile/tests; do not invent product settings beyond existing onboarding completion.

5. Add app-specific image storage structure:
   - Do not store image bytes as Room BLOBs.
   - Add a focused image storage component under `:core:data`, for example `ScreenshotImageStorage`.
   - Store future copied images under app-private files, using stable subdirectories:
     - `files/recap/images/`
     - `files/recap/thumbnails/`
   - Expose small APIs to:
     - Resolve/create the root image and thumbnail directories.
     - Build stable file paths for a given `imageId`.
     - Optionally copy a `Uri` into the image directory if implementation is simple and safe.
   - Room should store only the original source URI and/or app-private stored path/thumbnail path strings.
   - Do not implement thumbnail generation unless it is trivial and covered by tests; path structure is enough for this task.

6. Wiring scope:
   - It is acceptable to keep UI behavior unchanged.
   - Do not replace Home mock cards with Room-backed data in this task.
   - Do not connect real OCR/Firebase AI outputs to this storage in this task.
   - If saving mock analysis results from `ScreenshotAnalysisProgressViewModel` requires broad app-flow changes, leave that wiring out and keep the repository directly testable. If it is a small constructor injection plus save call, it may be added, but UI output must remain unchanged.

## Files to Touch
- `docs/SCREENSHOT_MOCK_DATA.md`
- `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotAnalysisResult.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepository.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepositoryTest.kt`
- `app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt`
- `feature/demo/src/main/java/...` or other preview/test files only where `ScreenshotAnalysisResult` constructors require `isFavorite`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabase.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/DatabaseModule.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/UserPreferencesRepository.kt`
- New focused files under:
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/`
  - `core/data/src/main/java/com/chalkak/recap/core/data/image/` or `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/`
  - matching `core/data/src/test/java/...` paths for DAO/repository/path tests
- Avoid unrelated UI, navigation, collection screen, and design component changes.

## Acceptance Criteria
- `docs/SCREENSHOT_MOCK_DATA.md` documents `is_favorite` / `isFavorite` in the JSON-equivalent shape, Kotlin mapping, and mock generation rules.
- `ScreenshotAnalysisResult` contains `isFavorite: Boolean`.
- `MockScreenshotAnalysisRepository` still follows the existing mock rules and now emits `isFavorite = false` by default.
- Room has persistent storage for screenshot cards and key fields, including `isFavorite` and image path/reference columns.
- Room migration creates the new tables without dropping existing OCR tables.
- DAO/repository tests prove that:
  - A list of `ScreenshotAnalysisResult` can be saved and read back in stable order.
  - Key fields are preserved and replaced correctly on re-save.
  - Favorite state can be updated independently.
  - Deleting a card removes its key fields via cascade or equivalent DAO behavior.
- `UserPreferencesRepository` behavior remains compatible with existing callers.
- There is one canonical provider for the existing `user_preferences` PreferenceDataStore; no second DataStore file/name is introduced.
- App-private image storage component creates/resolves `files/recap/images/` and `files/recap/thumbnails/` paths and does not store binary image data in Room.
- No new production dependency is added.
- Existing public UI behavior remains unchanged unless a tiny persistence save hook is added behind the scenes.

## Validation
- Run local unit tests after implementation:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
  ```
- Run debug build:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
  ```
- If Room migration validation fails because schema export is disabled, add focused DAO/repository tests that exercise the migrated database path with Room testing, or clearly report the limitation in `Cursor Result`.

## Out of scope
- Replacing Home, Collection, or search UI mock data with Room-backed data.
- Real OCR/Firebase AI persistence integration.
- Upload/sync/backend API design.
- Full collection/card relationship schema beyond screenshot card storage.
- Thumbnail generation pipeline.
- Storing original images as Room BLOBs.
- Adding Proto DataStore.
- Adding new production dependencies.

## Technical Debt
- `:core:data` Robolectric 테스트가 JUnit4 `@RunWith`와 JUnit5가 혼재함. 상세는 `docs/BACKLOG.md` Open 항목 `2026-07-08 - :core:data Robolectric 테스트를 JUnit5로 통일` 참고.

## Cursor Result
- Changed files: docs/SCREENSHOT_MOCK_DATA.md, core/model/.../ScreenshotAnalysisResult.kt, core/data/.../MockScreenshotAnalysisRepository.kt, core/data/.../RecapDatabase.kt, core/data/.../DatabaseModule.kt, core/data/.../RecapDatabaseMigrations.kt, core/data/.../UserPreferencesRepository.kt, core/data/.../UserPreferencesDataStoreOwner.kt, core/data/.../UserPreferencesModule.kt, core/data/.../screenshot/persistence/*, core/data/.../screenshot/image/ScreenshotImageStorage.kt, core/data/build.gradle.kts, gradle/libs.versions.toml, related unit tests
- Build/test: .\gradlew.bat testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN
- Open questions: none

## Codex Review
- Blocking: none
- Nits: none
- Verdict: DONE
- Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN
