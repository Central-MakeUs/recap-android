# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Cursor

## Context

- Source request: compare the current screenshot mock contract in `docs/SCREENSHOT_MOCK_DATA.md` with the deployed RE-CAP Swagger contract and synchronize the mock implementation to the backend shape.
- Swagger snapshot checked on 2026-07-18: `https://re-cap.duckdns.org/v3/api-docs` (`RE-CAP API` v1.0.0).
- The current `ScreenshotAnalysisResult` is based on a legacy AI-analysis payload:
  - `imageId: String`
  - nested `contentTypes.primaryContentType`
  - `keyFields`
  - `confidence`
  - `isFavorite`, `title`, `summary`, and the later-added `body`
- The deployed API does not expose that legacy analysis payload. A single stored capture is represented by `CaptureDetailResponse`:
  - `captureId: Long`
  - `typeCode: JOB | SHOPPING | PLACE | SCHEDULE | KNOWLEDGE | CONTENT | BENEFIT | RECORD | ETC`
  - `title`, `summary`, `body`
  - `originalImageUrl`
  - `isFavorite`
  - `organizedAt` (`date-time`)
- `CaptureSummaryResponse` is a separate list projection with `thumbnailUrl` instead of `body` and `originalImageUrl`. The local analysis mock represents a completed capture detail, not an upload URL response or a list-summary DTO.
- Current enum mapping to the server contract is:

| Current mock | Swagger `typeCode` |
|---|---|
| `JOB_CAREER` | `JOB` |
| `SHOPPING_PRODUCT` | `SHOPPING` |
| `PLACE_RESTAURANT` | `PLACE` |
| `SCHEDULE_RESERVATION` | `SCHEDULE` |
| `INFO_KNOWLEDGE` | `KNOWLEDGE` |
| `BOOK_CONTENT` | `CONTENT` |
| `BENEFIT_EVENT` | `BENEFIT` |
| `RECORD_CAPTURE` | `RECORD` |
| `OTHER` | `ETC` |

- Room schema version 4 persists `imageId` as a UUID-like `TEXT` primary key and persists legacy `confidence` and key-field data. Navigation, repositories, local image filenames, and Home/Collection/Screenshot feature contracts currently pass this ID as `String`.
- The user confirmed on 2026-07-18 that this is development-only data and may be reset. The synchronized schema becomes a new Room `version = 1`; existing version-4 databases are not migrated or preserved, and `captureId: Long` replaces the string ID throughout the screenshot flow.
- An existing installed development build must have its app data cleared or be uninstalled/reinstalled before the first run with the reset schema. Do not add a destructive migration fallback solely to make an uncleared version-4 database open.
- The user also confirmed that the supplied realistic server response is a shape/example reference only. Do not hardcode it as a fixture: preserve the current mock generator strategy for `typeCode`, `title`, `summary`, and `body`.

## Spec

1. Treat Swagger `CaptureDetailResponse` as the authoritative shape for one completed screenshot mock result.
   - Change the screenshot result/domain contract to expose `captureId: Long`, `typeCode`, `title`, `summary`, `body`, `originalImageUrl`, `isFavorite`, and `organizedAt`.
   - Represent `organizedAt` as `java.time.Instant` in the domain and convert to epoch milliseconds only at Room/UI formatter boundaries.
   - Keep local-only source URI, copied-image path, and thumbnail path in `ScreenshotCardImageRefs`; do not mix them into the Swagger-aligned result model.
   - Remove `contentTypes`, `keyFields`, and `confidence` from the screenshot mock result because the deployed capture schemas do not contain them.
   - Do not change the separate Firebase AI/OCR models under `core/model/RecapAnalysisModels.kt` or `core/data/ai`; they are not the screenshot mock contract named by this task.

2. Align screenshot category values with Swagger.
   - Keep one app-domain enum for screenshot/capture type, but change its values to the exact Swagger codes: `JOB`, `SHOPPING`, `PLACE`, `SCHEDULE`, `KNOWLEDGE`, `CONTENT`, `BENEFIT`, `RECORD`, `ETC`.
   - Replace the nested `contentTypes.primaryContentType` access pattern with the flat `typeCode` property.
   - Update design-label/category mappings, collection ordering/filtering, edit defaults, previews, fixtures, and tests to use the new values without changing existing Korean labels or visual category semantics.

3. Make `MockScreenshotAnalysisRepository` generate the synchronized detail shape.
   - Preserve single and batch APIs and batch input order.
   - Replace UUID generation with an injectable positive `Long` capture-ID supplier so tests remain deterministic and generated IDs do not collide within a batch.
   - Do not add a fixed `PLACE` fixture or copy the supplied restaurant response into production mock output.
   - Preserve the current content-generation behavior: choose `typeCode` through the existing random index strategy and keep `title = "스크린샷" + fileName`, `summary = "요약" + fileName`, and `body = "본문" + fileName`.
   - Do not couple the generated text to a fixed category fixture in this task; the goal is contract synchronization, not replacement of the existing mock content strategy.
   - Generate a nonblank, deterministic mock `originalImageUrl` from `captureId`. Use a clearly non-production `mock://` URL so it cannot be mistaken for a reachable backend object.
   - Generate `organizedAt` through an injectable clock/time supplier; do not call the wall clock directly in tests.
   - Keep `isFavorite = false` by default.
   - Remove confidence/key-field randomization and their suppliers/tests.

4. Synchronize persistence with the new result contract.
   - Make the card key and public repository APIs use `captureId: Long` consistently.
   - Persist `typeCode`, `originalImageUrl`, and `organizedAtMillis`; keep local image refs and an internal update timestamp where needed.
   - Remove the legacy confidence column and `screenshot_key_fields` entity/DAO/mappers because there is no corresponding backend field.
   - Reset `RecapDatabase` to `version = 1` with the synchronized entities as the fresh-install schema.
   - Remove obsolete `MIGRATION_1_2`, `MIGRATION_2_3`, and `MIGRATION_3_4` definitions and remove their registration from `DatabaseModule`. Delete `RecapDatabaseMigrations.kt` if no migration remains.
   - Do not add `fallbackToDestructiveMigration`, downgrade handling, or a 4→1 migration. Existing development installs are reset manually as confirmed by the user.
   - Ensure local image copy/thumbnail/delete operations use a stable string filename derived from `captureId` and continue to prevent path traversal.

5. Propagate `captureId: Long`, flat `typeCode`, and backend `organizedAt` through consumers.
   - Update analysis-progress persistence maps and result tracking.
   - Update app navigation routes and callbacks so screenshot detail navigation uses `Long` and rejects invalid/non-positive IDs at the boundary.
   - Update Home, Collection, Screenshot detail/edit, settings deletion, and related UI contracts/mappers/ViewModels to use `captureId` instead of `imageId` where the value identifies a stored capture.
   - Sort and format capture dates using the persisted backend-aligned `organizedAt`, not local row creation time.
   - Preserve current UI behavior, text, layouts, loading/error/empty states, favorite/edit/delete behavior, and image-path fallback behavior.

6. Rewrite `docs/SCREENSHOT_MOCK_DATA.md` as the synchronized contract.
   - Use Swagger's camelCase JSON names and show a `CaptureDetailResponse`-equivalent mock example.
   - Document the exact Kotlin mapping, enum values, mock generation rules, local-only image refs, Room representation, and batch ordering.
   - Make the example reflect generated mock values rather than the supplied fixed restaurant response, and document that type selection remains randomized while title/summary/body retain their current filename-derived generation.
   - Explicitly distinguish `CaptureDetailResponse` from `CaptureSummaryResponse` (`thumbnailUrl`) and from the upload workflow (`UploadItem.imageKey`/`uploadUrl`, organize `imageKeys`).
   - Remove legacy `image_id`, `content_types`, `key_fields`, and `confidence` documentation.

7. Update affected tests and fixtures.
   - Cover deterministic capture ID and time generation, all type-code boundaries, default favorite state, nonblank mock original URL, and batch order.
   - Update Room DAO/repository tests for the synchronized fields and verify a fresh in-memory version-1 database schema; do not add legacy migration tests.
   - Update navigation, analysis progress, Home, Collection, Screenshot, image storage, and mapper tests for `Long` IDs and `organizedAt`.
   - Preserve the project's JUnit5 conventions and use fixed `Instant` values in tests.

## Files to Touch

- `docs/SCREENSHOT_MOCK_DATA.md`
- `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/*.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/*.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorage.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/*.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabase.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabaseMigrations.kt` (delete after removing obsolete migrations)
- `core/data/src/main/java/com/chalkak/recap/core/data/DatabaseModule.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/category/ScreenshotContentTypeLabels.kt`
- `app/src/main/java/com/chalkak/recap/app/AppRoute.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/*.kt` (only ID/type/date consumers and fixtures)
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/*.kt` (only ID/type/date consumers and fixtures)
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/*.kt` (only ID/type/date consumers and fixtures)
- `feature/settings/src/main/java/com/chalkak/recap/feature/settings/*.kt` (only stored-card ID consumers)
- Matching tests under `app/src/test`, `core/data/src/test`, `core/design/src/test`, and `feature/*/src/test`
- Build files only if a test source set already affected by this work cannot access `java.time`; no new production dependency is expected.

## Acceptance Criteria

- `docs/SCREENSHOT_MOCK_DATA.md` and `ScreenshotAnalysisResult` describe the same completed-capture fields as Swagger `CaptureDetailResponse`.
- The screenshot mock result contains `captureId`, `typeCode`, `title`, `summary`, `body`, `originalImageUrl`, `isFavorite`, and `organizedAt`; it does not contain nested content types, key fields, or confidence.
- All nine screenshot type enum wire values exactly match Swagger.
- Mock single/batch analysis produces positive deterministic capture IDs, fixed-testable timestamps, nonblank mock original URLs, and preserves batch order.
- The mock does not hardcode the supplied restaurant response: type selection remains randomized and title/summary/body preserve the current filename-derived generation rules.
- Room persists and restores every synchronized result field plus local-only image refs without reintroducing legacy fields.
- `RecapDatabase` is reset to a fresh synchronized `version = 1`; obsolete migration definitions/registration are removed and no destructive fallback is enabled.
- App navigation and repository operations use `Long` capture IDs consistently, while local image filenames are safely derived from those IDs.
- Home/Collection/Screenshot behavior remains functionally unchanged and uses `organizedAt` for sorting/date display.
- Existing category labels and UI semantics map correctly to the renamed server type codes.
- Relevant unit tests and debug build pass.

## Validation

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest --continue --no-daemon --no-configuration-cache
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

- In addition to the full commands, Cursor may run targeted module tests while iterating, but the final Result must report both commands above.
- Static check: `rg` must find no screenshot mock/domain references to the removed legacy fields or legacy enum constants, excluding archived handoffs and the separate Firebase AI contract.
- Runtime note: an existing development installation with the old version-4 database must be manually cleared or reinstalled before launch. Cursor must report this requirement but does not need to perform ADB/device data deletion for this task.

## Out of scope

- Implementing Retrofit endpoints, authentication, presigned URL upload, object-storage `PUT`, organize polling, or replacing the mock repository with the live backend.
- Creating DTOs for every Swagger endpoint or changing Home/Storage screens to consume live summary endpoints.
- Changing the separate OCR/Firebase AI schema and prompt contract.
- UI redesign, copy changes, or unrelated refactoring.
- Adding a production dependency.

## Technical Debt

- Stored in BACKLOG: `docs/LOCAL_DATA.md` still documents the pre-sync Room schema (imageId, key_fields, migrations) and needs a follow-up doc update. Live API integration remains a separate future task.

## Cursor Result

- Changed files (Codex CHANGES_REQUESTED follow-up): `core/data/.../ScreenshotMockRandomizer.kt` (default captureId → UUID-based positive Long; no `1..n` restart), `core/data/.../MockScreenshotAnalysisRepositoryTest.kt` (separate-randomizer / sequential-restart regression), `docs/SCREENSHOT_MOCK_DATA.md`
- Build/test: `.\gradlew.bat testDebugUnitTest --continue --no-daemon --no-configuration-cache` GREEN; `.\gradlew.bat assembleDebug` GREEN
- Open questions: none
- Runtime note: existing development installs with Room version-4 DB must clear app data or reinstall before launch

## Codex Review

- Blocking: none.
- Nits: none.
- Validation: Cursor가 보고한 `testDebugUnitTest --continue --no-daemon --no-configuration-cache` 및 `assembleDebug` GREEN 결과를 확인했다. 재검토에서는 UUID 기반 양수 `Long` 기본 공급자, 별도 randomizer 인스턴스 회귀 테스트, 동기화 문서, legacy 계약 정적 검색 결과와 `git diff --check`를 확인했으며 추가 전체 빌드는 반복 실행하지 않았다.
- Verdict: DONE.

