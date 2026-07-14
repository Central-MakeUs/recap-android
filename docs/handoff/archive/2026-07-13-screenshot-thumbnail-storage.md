# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 분석 완료된 스크린샷은 `ScreenshotImageStorage.copyImageFromUri`로 원본을 `files/recap/images`에 복사하고, Room의 `storedImagePath`에 저장한다.
- `files/recap/thumbnails` 디렉터리와 `thumbnailPath` DB 필드는 이미 존재하지만 실제 썸네일 생성 경로가 없어 신규 저장 시에도 `thumbnailPath = null`이다.
- 홈/컬렉션 mapper는 이미 `thumbnailPath -> storedImagePath -> sourceImageUri` 순서로 모델을 선택한다.
- 스크린샷 상세/편집/전체화면은 하나의 `resolveScreenshotImageModel`을 공유하며 현재 `storedImagePath -> sourceImageUri -> thumbnailPath` 순서라 상세/편집도 원본을 우선 표시한다.
- Coil 3.5 `AsyncImage`는 Composable 제약과 `ContentScale`에 맞춰 디코딩 크기를 결정하므로 화면 표시 시 원본 해상도 bitmap을 항상 메모리에 올리는 것은 아니다. 다만 JPEG 품질과 영구 저해상도 파일을 제어하려면 별도 썸네일 파일 생성이 필요하다.

## Spec

### 1. 영구 썸네일 생성

- `ScreenshotImageStorage`에 source `Uri`로부터 영구 썸네일을 생성하고 성공 시 절대 경로를 반환하는 API를 추가한다.
- 썸네일 규격은 다음으로 고정한다.
  - 포맷: JPEG
  - 품질: 80
  - 해상도: orientation을 반영한 원본 이미지의 가로 폭을 50%로 축소하고, 세로 높이도 원본 비율을 유지해 같은 배율로 축소
  - 여기서 "현재 해상도"는 실행 기기의 화면 해상도가 아니라 각 source 이미지 자체의 픽셀 해상도를 뜻한다. 예: `1080x2400 -> 540x1200`, `1440x3200 -> 720x1600`.
  - 반올림으로 0px가 되지 않도록 각 변은 최소 1px로 계산한다.
  - 회전/EXIF orientation이 있는 입력은 사람이 보는 방향 기준으로 올바르게 저장한다.
  - JPEG가 alpha를 지원하지 않으므로 alpha가 있는 입력은 흰색 배경에 합성한다.
- Android 플랫폼 decoder/bitmap compression을 사용한다. Coil은 생성된 파일을 UI에서 읽는 역할로 유지하고, 영구 JPEG 생성만을 위해 `:core:data`에 Coil 의존성을 새로 추가하지 않는다.
- 결과 파일명은 `<imageId>.jpg`, 위치는 기존 `files/recap/thumbnails`이다.
- 최종 파일에 직접 쓰지 말고 같은 디렉터리의 임시 파일에 쓴 뒤 성공 시 교체한다. 취소/예외/압축 실패 시 임시 파일과 불완전한 결과 파일을 제거하고 `null`을 반환한다. `CancellationException`은 삼키지 않는다.
- 기존 원본 복사 동작은 유지한다. 원본 저장과 썸네일 생성은 같은 IO 구간에서 source `Uri`를 사용해 각각 수행한다.
- 썸네일 생성 실패는 분석 결과 저장 전체를 실패시키지 않는다. `thumbnailPath = null`로 저장하고 기존 원본 fallback이 동작하게 하며, raw exception을 사용자에게 노출하지 않고 Timber에 debug-safe한 경고만 남긴다.
- `<imageId>.jpg` 명명으로 바뀐 썸네일도 개별 삭제/전체 삭제 시 함께 제거되도록 삭제 경로와 traversal 방어를 갱신한다.

### 2. 저장 파이프라인 연결

- `ScreenshotAnalysisProgressViewModel.persistAnalysisResult`에서 원본 복사와 썸네일 생성을 수행한다.
- 성공한 썸네일 절대 경로를 `ScreenshotCardImageRefs.thumbnailPath`로 전달해 Room에 저장한다.
- 원본 복사 실패와 썸네일 생성 성공/실패를 독립적으로 처리한다. 둘 중 하나가 실패해도 이용 가능한 다른 이미지 참조와 분석 결과는 저장한다.

### 3. 화면별 이미지 선택 정책

- 단일 resolver를 목적별로 분리하거나 명시적인 우선순위 인자를 사용해 호출부가 정책을 드러내게 한다.
- 비전체화면(스크린샷 상세 hero, 편집 preview): `thumbnailPath -> storedImagePath -> sourceImageUri`.
- 전체화면(`ScreenshotFullscreenScreen` 진입 모델): `storedImagePath -> sourceImageUri -> thumbnailPath`. 원본이 유실된 경우에만 마지막 복구 수단으로 썸네일을 허용한다.
- 홈/컬렉션의 기존 thumbnail-first mapper는 유지한다.
- 저장 전 MediaStore 이미지를 보여주는 organize picker/selection/confirmation과 demo 화면은 영구 `thumbnailPath`가 아직 없는 단계이므로 이번 변경 대상이 아니다. 해당 `AsyncImage`는 현재처럼 Compose 제약 기반 Coil 다운샘플링을 사용한다.

### 4. 기존 데이터와 fallback

- DB 컬럼과 디렉터리가 이미 있으므로 Room schema migration은 추가하지 않는다.
- 기존 레코드 중 `thumbnailPath == null`인 항목을 일괄 backfill하지 않는다. 이 항목과 썸네일 생성 실패 항목은 원본 fallback으로 계속 표시한다.
- 이번 작업 이후 새로 분석/저장되는 스크린샷은 정상 경로에서 반드시 영구 썸네일 경로를 저장한다.

## Files to Touch

- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorage.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorageTest.kt`
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
- `app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotFormatters.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotDetailScreen.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotEditScreen.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotRoute.kt`
- `feature/screenshot/src/test/java/com/chalkak/recap/feature/screenshot/ScreenshotFormattersTest.kt` (new)

## Acceptance Criteria

- 새 스크린샷 분석 저장 성공 후 `files/recap/thumbnails/<imageId>.jpg`가 존재하고 DB의 `thumbnailPath`가 그 절대 경로를 가진다.
- 생성된 파일은 JPEG이며 orientation이 반영된 원본 폭의 50%이고, 높이는 원본 비율을 유지한 50%이다.
- 스크린샷 상세/편집, 홈, 컬렉션은 `thumbnailPath`가 있으면 원본보다 우선 사용한다.
- 전체화면은 원본 저장 경로 또는 source Uri를 썸네일보다 우선 사용한다.
- 썸네일 생성 실패 시 분석 결과와 이용 가능한 원본 참조는 저장되고 비전체화면도 원본 fallback으로 표시된다.
- 이미지 삭제 및 전체 데이터 삭제는 원본과 `.jpg` 썸네일을 모두 정리하며 경로 traversal 방어가 유지된다.
- 기존 데이터의 `thumbnailPath == null`은 오류 없이 원본 fallback으로 표시된다.
- 새 production dependency와 Room migration을 추가하지 않는다.

## Validation

- `ScreenshotImageStorageTest`에 최소 다음을 검증한다: JPEG 생성, orientation 반영 후 가로/세로 50% 축소, 비율 유지, 홀수 픽셀 반올림과 최소 1px 보장, 실패 시 임시/결과 파일 미잔존, `.jpg` 썸네일 개별/전체 삭제, traversal 방어.
- `ScreenshotAnalysisProgressViewModelTest`에서 썸네일 성공 경로가 repository에 전달되는 경우와 썸네일 실패가 전체 저장을 막지 않는 경우를 검증한다.
- `ScreenshotFormattersTest`에서 preview와 fullscreen의 우선순위 및 blank/null fallback을 각각 검증한다.
- 실행 명령:
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`

## Out of scope

- 기존 `thumbnailPath == null` 레코드의 일괄/백그라운드 backfill
- 원본 이미지 자체의 재압축 또는 삭제
- 여러 크기의 썸네일 tier 생성
- Coil 전역 `ImageLoader`/캐시 정책 변경
- organize picker/selection/confirmation 및 demo 화면용 임시 JPEG 파일 생성
- 로딩/에러 UI 디자인 변경

## Technical Debt

- none

## Cursor Result

- Changed files:
  - `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorage.kt`
  - `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotImageStorageTest.kt`
  - `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
  - `app/src/test/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModelTest.kt`
  - `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotFormatters.kt`
  - `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotDetailScreen.kt`
  - `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotEditScreen.kt`
  - `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotRoute.kt`
  - `feature/screenshot/src/test/java/com/chalkak/recap/feature/screenshot/ScreenshotFormattersTest.kt` (new)
- Build/test: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN
- Open questions: none
- Notes:
  - CHANGES_REQUESTED 반영: bounds decode + `inSampleSize = 2`로 decoder 단계 다운샘플 후 orientation/홀수 픽셀 정밀 보정.
  - CHANGES_REQUESTED 반영: temp→backup→publish 교체, publish 실패 시 기존 target 복구/보존 검증.
  - Coil dependency / Room migration 추가 없음.

## Codex Review

- Blocking: none
- Nits: none
- Validation: `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest` GREEN, `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug` GREEN, `git diff --check` GREEN.
- Verdict: DONE
