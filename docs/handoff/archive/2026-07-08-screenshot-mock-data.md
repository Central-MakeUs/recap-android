# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Cursor

## Context
- The user wants screenshot analysis mock data while the real screenshot/OCR/AI analysis path is not ready.
- A screenshot image source will be passed into a mock repository. The repository must return a JSON-equivalent analysis result containing `image_id`, `title`, `summary`, `content_types`, `key_fields`, and `confidence`.
- `docs/PROJECT.md` describes future `RecapAnalysisRepository`, `RecapAnalysisPrompt`, and `RecapAnalysisSchema`, but the current source tree only contains the app shell. Add the new mock analysis code in a narrow `core` package inside the current single `:app` module.
- This task is not a UI task. No Compose screens, string resources, previews, OCR, Firebase AI, Room, or WorkManager integration are required.

## Spec
Implement a screenshot analysis mock repository and document the mock contract.

1. Add JSON-equivalent Kotlin model types for screenshot analysis.
   - Suggested package: `com.chalkak.recap.core.model.screenshot`.
   - Required result fields:
     - `imageId: String`
     - `title: String`
     - `summary: String`
     - `contentTypes: ScreenshotContentTypes`
     - `keyFields: List<ScreenshotKeyField>`
     - `confidence: ScreenshotAnalysisConfidence`
   - `ScreenshotContentTypes` must currently include only `primaryContentType`.
   - Do not add `secondaryContentTypes` in this mock implementation.
   - `primaryContentType` enum values must match these names exactly:
     - `JOB_CAREER`
     - `SHOPPING_PRODUCT`
     - `PLACE_RESTAURANT`
     - `SCHEDULE_RESERVATION`
     - `INFO_KNOWLEDGE`
     - `DESIGN_REFERENCE`
     - `BOOK_CONTENT`
     - `BENEFIT_EVENT`
     - `RECORD_CAPTURE`
     - `OTHER`
   - `ScreenshotKeyField` must include:
     - `label: String`
     - `value: String`
     - `displayPriority: Int`
     - `isSensitive: Boolean`
   - `ScreenshotAnalysisConfidence` enum values must match:
     - `HIGH`
     - `MEDIUM`
     - `LOW`
   - Do not add a production JSON/serialization dependency just for this task. Use plain Kotlin data classes/enums unless an existing serialization convention is already available.

2. Add a mock repository API and implementation.
   - Suggested package: `com.chalkak.recap.core.data.screenshot`.
   - Add an input model that carries at least the original screenshot file name/display name.
     - Suggested type: `ScreenshotAnalysisInput(fileName: String)`.
     - The mock does not need to inspect image pixels or read image bytes.
   - Add repository interface:
     - Suggested name: `ScreenshotAnalysisRepository`.
     - Required behavior: analyze one or more screenshot inputs and return the corresponding mock analysis results.
     - If a batch method is added, preserve input order in the returned list.
   - Add mock implementation:
     - Suggested name: `MockScreenshotAnalysisRepository`.
     - The implementation must be injectable through Hilt if repository injection is already available or easy to add without broadening scope.
     - Keep the implementation independent of UI and Android-specific image decoding.

3. Generate mock values using these exact rules.
   - `image_id`: random unique id, preferably `UUID.randomUUID().toString()`.
   - `title`: `"스크린샷" + fileName`.
   - `summary`: `"요약" + fileName`.
   - `content_types.primary_content_type`: choose one random value from the 10 content type enum values.
   - `key_fields`: always generate exactly 3 items:
     - item 1: `label = "라벨1"`, `value = "값1"`, `display_priority = 1`
     - item 2: `label = "라벨2"`, `value = "값2"`, `display_priority = 2`
     - item 3: `label = "라벨3"`, `value = "값3"`, `display_priority = 3`
   - `key_fields[*].is_sensitive`: evaluate independently per key field with a 5% probability of `true`.
   - `confidence`: choose by weighted probability:
     - `HIGH`: 60%
     - `MEDIUM`: 25%
     - `LOW`: 15%

4. Make randomness testable without making tests flaky.
   - Isolate random/id generation behind a small helper or injectable function so unit tests can force deterministic outputs.
   - Unit tests must not assert statistical distribution by repeated random sampling.
   - Tests should cover deterministic boundary cases for:
     - content type selection
     - `isSensitive` threshold behavior
     - confidence thresholds
     - title/summary file-name composition
     - key field count, labels, values, and priorities

5. Add documentation under `docs/`.
   - Suggested file: `docs/SCREENSHOT_MOCK_DATA.md`.
   - Document:
     - The expected JSON-equivalent shape.
     - The current mock generation rules.
     - The enum values and confidence probabilities.
     - That `secondary_content_types` is intentionally excluded for now.
     - The implementation file/package locations.
     - That the mock repository does not read actual image contents.

## Files to Touch
- `app/src/main/java/com/chalkak/recap/core/model/screenshot/...`
- `app/src/main/java/com/chalkak/recap/core/data/screenshot/...`
- `app/src/main/java/com/chalkak/recap/core/data/di/...` only if needed for Hilt binding
- `app/src/test/java/com/chalkak/recap/core/data/screenshot/...`
- `docs/SCREENSHOT_MOCK_DATA.md`

## Acceptance Criteria
- A caller can pass a screenshot file/display name into the mock repository and receive a screenshot analysis result matching the requested JSON contract.
- `contentTypes` contains `primaryContentType` only; no `secondaryContentTypes` is exposed by the mock result.
- The 10 content type enum values and 3 confidence enum values match the user-provided names exactly.
- Every generated result contains exactly three key fields with the requested Korean labels/values and priorities `1`, `2`, and `3`.
- `isSensitive` is evaluated independently for each key field using a 5% true threshold.
- `confidence` uses the requested weighted thresholds: `HIGH` 60%, `MEDIUM` 25%, `LOW` 15%.
- Randomness/id generation can be controlled in unit tests, and tests are deterministic.
- `docs/SCREENSHOT_MOCK_DATA.md` explains the mock contract and generation behavior.
- No real OCR, Firebase AI call, image decoding, persistence write, or UI behavior is added.

## Validation
- Run local unit tests:
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
- Run debug build:
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
- If only docs are changed because implementation is blocked, report `not run - docs/spec only` in `Cursor Result` and explain the blocker.

## Out of scope
- Real screenshot OCR.
- Real Firebase AI analysis.
- Prompt/schema integration with a remote model.
- UI cards, filters, collection screens, or user-visible text changes.
- Room/DataStore persistence.
- `secondary_content_types`.
- Statistical distribution testing through repeated random runs.

## Technical Debt
- None.

## Cursor Result
- Changed files:
  - `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotContentType.kt`
  - `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotAnalysisConfidence.kt`
  - `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotKeyField.kt`
  - `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotContentTypes.kt`
  - `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotAnalysisResult.kt`
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisInput.kt`
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisRepository.kt`
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotMockRandomizer.kt`
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepository.kt`
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisModule.kt`
  - `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepositoryTest.kt`
  - `core/data/build.gradle.kts`
  - `docs/SCREENSHOT_MOCK_DATA.md`
- Path expansion: HANDOFF의 `:app` 경로 대신 현재 멀티모듈 구조(`:core:model`, `:core:data`)에 맞춰 구현함.
- Build/test: `.\gradlew.bat :core:data:testDebugUnitTest` GREEN, `.\gradlew.bat :app:assembleDebug` GREEN
- Open questions: none
## Codex Review
Blocking: none

Nits: none

Verdict: DONE

Validation:
- `.\gradlew.bat :core:data:testDebugUnitTest :app:assembleDebug` GREEN
