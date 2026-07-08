# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
- User requested the Collection tab be turned into a Room-backed "보관함" experience using `docs/LOCAL_DATA.md` and the provided visual references:
  - Image #1: non-empty 보관함 overview with 즐겨찾기 and 유형별 보기 sections.
  - Image #2: type detail list screen.
  - Image #3: empty 보관함 state that drives the user to organize screenshots.
- `docs/LOCAL_DATA.md` says `ScreenshotCardRepository` already exists in `:core:data` and exposes `observeStoredCards()`, `saveAnalysisResults(...)`, `updateFavorite(...)`, and `deleteCard(...)`.
- Current `feature:collection` is only a placeholder `CollectionScreen`/`CollectionContract`; it does not observe Room data and the module does not depend on `:core:data`, Hilt, lifecycle compose, or Navigation3.
- Current app navigation renders the collection tab through `RecapMainTabNavHost -> CollectionScreen()`.
- Current organize completion path is `OrganizeRoute -> RecapNavHost -> ScreenshotAnalysisProgressViewModel.startMockAnalysis(selectedScreenshots)`. The progress ViewModel analyzes selected `LocalImage`s but does not save the resulting `ScreenshotAnalysisResult`s to Room.
- `ScreenshotImageStorage` can copy selected images into app private storage, but has no clear/reset API yet.
- `ScreenshotCardRepository` has no clear-all API yet. `ScreenshotCardDao.deleteByImageId(...)` exists; `screenshot_key_fields` cascade delete is configured when deleting parent `screenshot_cards`.
- Existing content types are the 10 enum values in `ScreenshotContentType`: `JOB_CAREER`, `SHOPPING_PRODUCT`, `PLACE_RESTAURANT`, `SCHEDULE_RESERVATION`, `INFO_KNOWLEDGE`, `DESIGN_REFERENCE`, `BOOK_CONTENT`, `BENEFIT_EVENT`, `RECORD_CAPTURE`, `OTHER`.
- User-facing "컬렉션" should become "보관함". Internal `Collection*` route/class/module names must remain unless a specific symbol rename is strictly necessary for the implementation; do not perform a broad internal rename.

## Spec
1. Collection tab naming
   - Change user-visible bottom tab label from `컬렉션` to `보관함`.
   - Change collection screen title and related user-facing text to Korean "보관함" copy.
   - Update any user-visible string that says "컬렉션" for this tab context, including the collection search placeholder if it remains in use.
   - Do not rename package/module/class symbols solely for naming preference. Keep `feature:collection`, `CollectionRoute`, `CollectionScreen`, etc. unless implementation requires otherwise.
   - Keep all new UI strings in the existing string resource location used by this project (`core/design/src/main/res/values/strings.xml`).

2. Collection route and navigation
   - Replace direct `CollectionScreen()` usage in `RecapMainTabNavHost` with a route-level composable, e.g. `CollectionRoute`.
   - `CollectionRoute` should own an internal Navigation3 back stack for:
     - overview: 보관함 main screen.
     - type detail: screenshots filtered by one `ScreenshotContentType`.
     - favorite detail: screenshots filtered to `isFavorite == true`.
   - The 보관함 feature should therefore have 11 possible child list views: 10 type detail views plus 1 favorite detail view.
   - Pass `onNavigateToOrganize` from `RecapMainScreen` through `RecapMainTabNavHost` into `CollectionRoute` so the empty-state "스크린샷 정리하기" button opens the existing organize flow.
   - Overview favorite aggregate card click must navigate to favorite detail. If there are no favorites, the no-favorite thin card is not clickable.
   - Type detail and favorite detail list item body click is no-op for this task; only back navigation and favorite star toggling are required.

3. Room-backed collection state
   - Add a `CollectionViewModel` in `:feature:collection`.
   - Inject `ScreenshotCardRepository`.
   - Observe `ScreenshotCardRepository.observeStoredCards()` and map cards into immutable collection UI state.
   - Add the required module dependencies/plugins to `feature/collection/build.gradle.kts` for Hilt, KSP, lifecycle compose/ViewModel compose, Navigation3, and `:core:data`; do not add new external production libraries beyond already version-cataloged project dependencies.
   - Overview UI state should derive:
     - `hasStoredScreenshots`
     - favorite cards where `analysisResult.isFavorite == true`
     - type summaries for only content types that have at least one stored card
     - per-type count
     - per-type preview cards/thumbnails from the latest stored cards
   - Use `StoredScreenshotCard.createdAtMillis` for latest-first ordering in overview/detail.

4. Overview screen behavior
   - If there are no stored screenshot cards in Room, show the Image #3 style empty state:
     - title: `보관함`
     - empty title: `아직 보관된 캡처가 없어요`
     - description: `스크린샷을 정리하면 유형별로\n모아볼 수 있어요` or equivalent line-broken resource.
     - primary button: `스크린샷 정리하기`, which calls `onNavigateToOrganize`.
     - Use an existing drawable icon such as `ic_storage_24` or add a vector asset if needed; do not use `Icons.*`, Canvas, or text as an icon substitute.
   - If there is at least one stored screenshot card, show the Image #1 style overview:
     - title: `보관함`
     - subtitle: `즐겨찾기와 유형별로 정리된 캡처를 모아봐요`
     - section title: `즐겨찾기`
     - section title: `유형별 보기`
   - Favorites section:
     - If favorite cards exist, show a compact aggregate card with count text like `%1$d개의 캡처`, representative thumbnails, and a chevron if clickable.
     - If no favorite cards exist, show a thin card containing exactly `즐겨찾기 설정된 스크린샷이 없어요.`
   - Type section:
     - Show a type card only for a `ScreenshotContentType` with count > 0.
     - Each type card should show the Korean type label, count, representative thumbnails, and short examples from stored card titles.
     - Type card example text rule: take up to 2 latest card titles for that type, join with ` · `. If the type has 3 or more cards, append ` 외 %1$d건`, where `%1$d` is `totalCount - 2`.
     - Type card count text rule: `%1$d개`.
     - Clicking a type card navigates to the type detail screen for that content type.
   - Use app theme tokens (`MaterialTheme`, existing `core/design/theme` tokens) and existing drawable icons (`ic_chevron_right_24`, `ic_star_24`, `ic_storage_24`, etc.) where possible. Do not introduce new `Icons.*` usages.
   - Add previews for empty, no-favorite, and populated overview states, wrapped in `RECAPTheme`.

5. Content type labels
   - Provide Korean labels for all 10 existing `ScreenshotContentType` values. Use these labels in overview and detail:
     - `JOB_CAREER`: `직무·커리어`
     - `SHOPPING_PRODUCT`: `쇼핑·상품`
     - `PLACE_RESTAURANT`: `장소·맛집`
     - `SCHEDULE_RESERVATION`: `일정·예약`
     - `INFO_KNOWLEDGE`: `정보·지식`
     - `DESIGN_REFERENCE`: `디자인·레퍼런스`
     - `BOOK_CONTENT`: `도서·콘텐츠`
     - `BENEFIT_EVENT`: `혜택·이벤트`
     - `RECORD_CAPTURE`: `기록·캡처`
     - `OTHER`: `기타`
   - Prefer a small mapper/helper in `feature:collection` or `core:model` only if it is reusable. Avoid a broad shared abstraction unless needed.

6. Type detail screen behavior
   - Implement an Image #2 style list page for a selected `ScreenshotContentType`.
   - Header:
     - back button using drawable `ic_chevron_left_24`.
     - title = selected Korean type label.
     - subtitle/count = `%1$d개의 캡처`.
     - sort control supports exactly two options: `최신순` and `이름순`.
     - Default sort is `최신순`.
     - `최신순` sorts by `StoredScreenshotCard.createdAtMillis` descending.
     - `이름순` sorts by `analysisResult.title` ascending, using the platform/default Kotlin string ordering unless an existing project convention says otherwise.
   - List:
     - Show stored cards filtered by selected type and sorted by the selected sort option.
     - Each row shows thumbnail, title, summary, type badge, date, and favorite star state.
     - Tapping the star toggles `ScreenshotCardRepository.updateFavorite(imageId, !current)`.
     - Date format for `createdAtMillis`: `M월 d일` in the device default locale/time zone.
     - Use `thumbnailPath`, `storedImagePath`, then `sourceImageUri` as image model fallback order. If all are null or loading fails, show a subtle placeholder using an existing drawable or non-icon surface treatment.
   - If a type detail becomes empty after reset/deletion, show a simple empty state and allow back navigation.
   - Add previews for the detail screen with populated and empty states, wrapped in `RECAPTheme`.

7. Favorite detail screen behavior
   - Implement the same list layout as type detail, filtered to `analysisResult.isFavorite == true`.
   - Header title: `즐겨찾기`.
   - Count text: `%1$d개의 캡처`.
   - Sort control supports exactly `최신순` and `이름순`, with the same default and ordering rules as type detail.
   - Tapping the star on a favorite detail row should update favorite to false. If that removes the last favorite, show a simple empty state with text `즐겨찾기 설정된 스크린샷이 없어요.` and keep back navigation available.

8. Save organize analysis results to Room
   - Update `ScreenshotAnalysisProgressViewModel` so organizing screenshots persists analyzed cards into `ScreenshotCardRepository`.
   - Inject `ScreenshotCardRepository` and `ScreenshotImageStorage`.
   - Keep existing progress behavior and tests: selected image display names must still be passed to `ScreenshotAnalysisRepository` in order, and progress still advances per image.
   - For each selected `LocalImage`, after `ScreenshotAnalysisRepository.analyze(...)` returns a `ScreenshotAnalysisResult`:
     - copy the original image into app private storage with `ScreenshotImageStorage.copyImageFromUri(result.imageId, Uri.parse(localImage.uri))`;
     - create `ScreenshotCardImageRefs(sourceImageUri = localImage.uri, storedImagePath = copiedPath, thumbnailPath = null)`;
     - save the result through `ScreenshotCardRepository.saveAnalysisResults(listOf(result), mapOf(result.imageId to refs))`.
   - Match analysis result to source image by list order; current mock analysis generates its own `imageId`, so do not assume the source URI and result ID are the same.
   - If image copying fails, still save the analysis result with `sourceImageUri` and `storedImagePath = null`.
   - If saving one result fails, do not expose a raw exception to UI. Let the current analysis job continue for remaining screenshots if feasible, and keep progress state coherent. Add a generic debug-safe failure state only if needed by tests.
   - Save each screenshot as soon as that screenshot's analysis completes. Do not wait for the whole selected batch to finish before persisting earlier results.
   - Do not block returning to Home after organize completion; keep the current behavior where analysis continues in `ScreenshotAnalysisProgressViewModel`.

9. Developer option local DB reset
   - Add a developer option button labeled `로컬 DB 초기화` or `스크린샷 정리 데이터 초기화`.
   - Clicking it should delete screenshot organization data:
     - delete all rows from `screenshot_cards`; key fields should be removed through cascade or explicit delete;
     - best-effort delete app private screenshot image/thumbnail files under `filesDir/recap/images` and `filesDir/recap/thumbnails`;
     - do not delete original MediaStore screenshots;
     - do not reset onboarding DataStore;
     - do not delete OCR job/result history.
   - Add clear-all APIs in the smallest suitable layer:
     - `ScreenshotCardDao.deleteAllCards()` or equivalent.
     - `ScreenshotCardRepository.deleteAllCards()` / `clearStoredCards()` facade.
     - `ScreenshotImageStorage.clearStoredImages()` / equivalent best-effort file cleanup.
   - Wire the developer button through `DeveloperOptionAction` and `DeveloperViewModel`.
   - After reset succeeds, show a developer-options-screen text/snackbar equivalent such as `스크린샷 정리 데이터를 초기화했어요.`
   - If reset fails, show generic text such as `초기화에 실패했어요. 다시 시도해주세요.` Do not show raw exception messages to the user.

10. Error/loading handling
   - Collection screen should not crash if the repository emits an empty list.
   - Use `isLoading = true` only before the first Room emission. Once `observeStoredCards()` emits an empty list, render the empty 보관함 state rather than a spinner.
   - For repository/delete errors, show a recoverable generic message or keep developer-only feedback concise; no raw exception text in UI.

## Files to Touch
- `docs/handoff/HANDOFF.md` (this spec only; Cursor should update result fields after implementation)
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
- `app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt`
- `feature/collection/build.gradle.kts`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionContract.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionScreen.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionRoute.kt` (new, if using route-level navigation)
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionViewModel.kt` (new)
- `feature/collection/src/test/java/com/chalkak/recap/feature/collection/CollectionViewModelTest.kt` (new, if practical)
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperOptionsScreen.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperRoute.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperViewModel.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardDao.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorage.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardDaoTest.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorageTest.kt`
- `core/design/src/main/res/values/strings.xml`
- `core/design/src/main/res/drawable/*.xml` only if existing drawable assets are insufficient

## Acceptance Criteria
- Bottom navigation and collection tab user-facing title use `보관함`, not `컬렉션`.
- Collection tab no longer shows the placeholder `RecapPlaceholderScreen`.
- With zero stored `ScreenshotCardRepository` cards, the 보관함 tab shows the Image #3 style empty state and `스크린샷 정리하기` opens the existing organize flow.
- With stored cards and zero favorites, the overview shows the exact empty-favorites text `즐겨찾기 설정된 스크린샷이 없어요.` in a thin card.
- With stored favorite cards, the overview shows a favorites aggregate card with the correct favorite count.
- The type overview shows cards only for content types that have at least one stored screenshot card.
- Each type overview card click opens a detail page filtered to that content type.
- Type detail page shows title, count, sorted rows, thumbnails or placeholders, title, summary, type badge, date, and favorite star.
- Type and favorite detail pages support `최신순` and `이름순`; default is `최신순`.
- Favorite aggregate card opens a favorite detail list; unfavoriting the final item shows the no-favorite empty text on that detail screen.
- Toggling a favorite in the detail list updates Room through `ScreenshotCardRepository.updateFavorite(...)` and the overview favorite count/empty state reacts to the change.
- Organize analysis results are saved into `screenshot_cards` and `screenshot_key_fields` as analysis completes.
- Saved card image refs include the original `LocalImage.uri`; private copied path is stored when copy succeeds.
- Developer option reset deletes all stored screenshot card rows and their key fields, and clears app private copied screenshot/thumbnail files without deleting original MediaStore screenshots.
- Developer option reset does not delete OCR job/result history or onboarding state.
- New UI strings are string resources; no hardcoded user-facing UI text remains in composables.
- New Compose screens/components have previews wrapped in `RECAPTheme`.
- No new `Icons.*`, Canvas icon substitutes, or text icon substitutes are introduced.
- Existing organize progress behavior remains intact.

## Validation
- Run:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
  ```
- Run:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
  ```
- Recommended focused test coverage:
  - `CollectionViewModel` mapping: empty state, no-favorite state, favorite count, type grouping, latest-first sorting.
  - `ScreenshotAnalysisProgressViewModel`: saves each analysis result with refs while preserving progress behavior and ordered repository inputs.
  - `ScreenshotCardDao`/repository: clear-all removes cards and key fields.
  - `ScreenshotImageStorage`: clear API deletes files under `recap/images` and `recap/thumbnails`.

## Out of scope
- Full screenshot detail/edit page outside the type/favorite list required here.
- Search implementation inside 보관함.
- Real Firebase AI/OCR pipeline changes beyond saving the current organize analysis results.
- Thumbnail generation pipeline; use existing stored image or source URI fallback for now.
- Account/cloud sync.
- Large internal rename from `Collection*` to `Archive*`/`Library*` unless Cursor finds it is smaller than leaving current internal names.

## Technical Debt
- No new technical debt added by this plan. Thumbnail generation remains out of scope and is already tracked separately by project docs/backlog.

## Cursor Result
- Changed files: core/design strings.xml (remaining `컬렉션` → `보관함` copy), feature/collection/CollectionContract.kt, feature/collection/CollectionOverviewComponents.kt, feature/collection/CollectionScreen.kt, feature/collection/CollectionViewModelTest.kt
- Build/test: .\gradlew.bat assembleDebug GREEN, .\gradlew.bat testDebugUnitTest GREEN
- Open questions: none
- Review follow-up: replaced all remaining user-visible `컬렉션` strings; moved type example suffix formatting to composable via `collection_type_examples_more`
## Codex Review
- Blocking: none
- Nits: none
- Validation:
  - `.\gradlew.bat testDebugUnitTest` GREEN.
  - `.\gradlew.bat assembleDebug` GREEN.
- Verdict: DONE
