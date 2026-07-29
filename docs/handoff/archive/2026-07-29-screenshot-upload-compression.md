# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 갤러리 선택 및 외부 공유 스크린샷은 `ScreenshotConfirmationScreen`에서 확인한 뒤
  `ScreenshotAnalysisProgressViewModel`을 통해 서버에 업로드·분석된다.
- 지원 입력 형식 PNG, JPEG/JPG, HEIC, HEIF를 업로드 전에 JPEG 품질 75로 정규화하되
  표시 기준 원본 해상도와 EXIF 방향을 유지해야 한다.
- 이미지 압축은 사용자에게 별도 상태로 노출하지 않고 Confirmation 진입 시 백그라운드에서
  미리 시작한다.
- 사용자가 압축 완료 전에 시작할 수 있어야 하며, 남은 압축과 업로드는 Progress 화면 진입
  직후 이어서 수행한다.
- Progress bar는 압축 및 업로드 진행률을 포함하지 않고 서버 분석 status polling 기준
  진행률만 표시한다.
- 압축본은 메모리에서만 보관하고 Confirmation 이탈 시 분석 경로로 전달되지 않은 참조를
  제거한다.

## Spec

### 1. 메모리 전용 업로드 모델

- `PreparedScreenshot`은 원본 `LocalImage`, JPEG `ByteArray`, 고정 MIME type
  `image/jpeg`를 보관한다.
- `ScreenshotUploadCandidate`는 원본 이미지, 선택적으로 완료된 `PreparedScreenshot`,
  이미 소비한 준비 시도 횟수를 보관한다.
- 준비 시도는 Confirmation과 Progress를 합쳐 이미지당 최대 2회로 제한한다.
- 바이트 배열은 Intent, `SavedStateHandle`, `Parcelable`, `Serializable`, 디스크 파일에
  저장하지 않는다.
- 모델의 동등성 및 캐시 식별은 바이트 배열이 아니라 원본 URI를 기준으로 한다.

### 2. JPEG 준비 컴포넌트

- `ScreenshotUploadPreparer`는 PNG, JPEG/JPG, HEIC, HEIF를 JPEG 품질 75로 변환한다.
- resize/downsample 없이 EXIF 방향을 반영한 표시 기준 원본 가로·세로를 유지한다.
- alpha 입력은 흰색 배경에 합성한다.
- URI read, decode, orientation, encode는 `Dispatchers.IO`에서 수행한다.
- singleton `Mutex`로 full-resolution 준비 작업을 직렬화해 여러 화면/단계에서도 한 번에
  한 이미지만 처리한다.
- bitmap과 임시 배열은 이미지별 처리 후 즉시 해제하고 압축된 바이트만 누적한다.
- 빈 입력, decode 실패, 빈 JPEG 출력은 실패시키고 `CancellationException`은 재전파한다.
- 새 production dependency를 추가하지 않는다.

### 3. Confirmation 선행 압축

- `OrganizeRoute`가 Confirmation destination에 진입하면 `onConfirmationEntered()`를
  호출한다.
- `OrganizeViewModel`은 현재 선택 URI 순서대로 준비를 시작하고 URI별 완료 결과와 시작된
  시도 횟수를 메모리에 보관한다.
- 공유 seed가 Confirmation 진입 뒤 도착하거나 선택 목록이 변경되어도 현재 선택 항목을
  reconcile한다.
- 선택 해제, Add more, 화면/시스템 back, overlay 종료, Composable dispose 시 stale 작업을
  취소하고 분석으로 전달하지 않은 캐시를 제거한다.
- 압축 중/실패 UI, 압축 진행률, retry 버튼은 표시하지 않는다.
- 시작 버튼 활성화는 기존 selection 유효성만 사용하며 압축 완료 여부에 의존하지 않는다.
- 시작 시점에 선택 순서의 `ScreenshotUploadCandidate` 목록을 즉시 snapshot하고 진행 중인
  Confirmation 준비 작업을 취소한다.
- 이미 시작된 준비 호출은 완료 여부와 무관하게 한 번의 시도로 기록해 Progress 단계까지
  포함한 실제 호출 횟수가 최대 2회를 넘지 않게 한다.

### 4. Progress 단계 압축·업로드·분석

- `ScreenshotAnalysisProgressViewModel.startAnalysis()`는 candidate 목록을
  `ScreenshotAnalysisInput`으로 변환해 repository에 전달한다.
- 이미 준비된 JPEG는 그대로 사용하고, 미완료 candidate는 남은 시도 예산 안에서
  repository가 선택 순서대로 준비한다.
- Remote는 준비된 항목을 먼저 업로드하고 미완료 항목은 준비 성공 즉시 업로드한다.
- 모든 업로드 body와 content type은 각각 JPEG bytes와 `image/jpeg`를 사용하며 원본 URI를
  업로드 fallback으로 다시 읽지 않는다.
- 이미지 준비가 두 번 모두 실패한 항목은 실패 수에 포함하고 나머지 성공 항목은 계속
  분석해 전체 실패 또는 부분 성공 terminal result를 만든다.
- 압축 및 업로드 중에는 progress callback을 호출하지 않는다. Remote progress callback은
  `getOrganizeStatus()` polling 결과만 전달한다.
- Mock backend도 미완료 준비를 이어서 수행하되 기존 로컬 결과 저장에는 원본
  `LocalImage`를 사용한다.
- 취소 또는 terminal 완료 후 분석 Job이 보유한 candidate/input 참조는 장기 cache에
  남기지 않는다.

### 5. 일반/공유 전달 경로

- `OrganizeRoute`, `RecapMainScreen`, `RecapNavHost`, `MainActivity`까지
  `List<ScreenshotUploadCandidate>`를 전달한다.
- 외부 공유 경로는 candidate 목록을 `SharedAnalysisRequestStore`에 request id별로
  in-process 보관한다.
- Intent에는 기존처럼 request id와 원본 `LocalImage` 메타데이터만 넣고 JPEG bytes는
  넣지 않는다.
- 등록되지 않은 request id는 거부하고 정상 consume 및 launch cancel cleanup은
  one-shot으로 유지한다.

### 6. 테스트

- `ScreenshotUploadPreparerTest`에서 PNG/JPEG 변환, 크기 유지, EXIF orientation,
  alpha 흰색 합성, 품질 75, 실패, cancellation, 직렬 실행을 검증한다.
- 제공된 실제 샘플을 test resource에 포함해 HEIC/HEIF decode가 JPEG 출력으로 완료되는지
  skip 없이 검증한다.
- `OrganizeViewModelTest`에서 Confirmation 진입 선행 준비, 공유 seed race, 선택 순서,
  selection 변경/stale 결과, exit cleanup, 압축 중 즉시 시작, 시도 횟수 handoff를
  검증한다.
- Remote repository 테스트에서 준비 완료/미완료 혼합 순서, JPEG 업로드, 최대 재시도,
  부분 실패, 업로드 중 progress 미방출, 서버 polling progress를 검증한다.
- Progress ViewModel 및 공유 request store/ViewModel 테스트에서 candidate 전달, 원본 이미지
  저장, one-shot consume와 forged request 거부를 검증한다.

## Files to Touch

- `core/model/src/main/java/com/chalkak/recap/core/model/PreparedScreenshot.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/image/ScreenshotUploadPreparer.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/analysis/`
- `feature/organize/src/main/java/com/chalkak/recap/feature/organize/`
- `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- `app/src/main/java/com/chalkak/recap/app/`
- 위 경로의 관련 unit test 및 `core/data/src/test/resources/image_fixtures/`
- exhaustive `when` 및 타입 전달 변경으로 직접 영향을 받은 최소 call site

## Acceptance Criteria

- Confirmation 진입 시 선택 이미지 압축이 메인 스레드를 막지 않고 즉시 시작된다.
- PNG, JPEG/JPG, HEIC, HEIF가 표시 해상도를 유지한 JPEG 품질 75,
  MIME `image/jpeg` payload로 정규화된다.
- full-resolution 압축은 전역으로 직렬 실행되고 압축 bytes만 메모리에 누적된다.
- 사용자는 압축 완료를 기다리지 않고 시작할 수 있다.
- Progress 진입 시 남은 압축과 업로드를 즉시 이어서 수행하되 해당 과정은 UI 진행률에
  나타나지 않는다.
- Progress bar는 서버 분석 polling 진행률만 표시하며 업로드 완료 후 100%에서 분석 0%로
  역행하지 않는다.
- 이미지별 준비 호출은 Confirmation과 Progress를 합쳐 최대 2회다.
- 일부 이미지 준비 실패는 성공 이미지 분석을 막지 않고 부분 성공/전체 실패로 집계된다.
- back/Add more/overlay 종료/dispose 시 미전달 압축 cache가 제거되고 디스크에 기록되지
  않는다.
- 공유 JPEG bytes는 Intent가 아니라 검증된 in-process one-shot store를 통해 전달된다.
- 관련 단위 테스트, debug build, diff 정적 검사가 통과한다.

## Validation

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :feature:organize:testDebugUnitTest :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug --no-daemon --no-configuration-cache --max-workers=1
```

```powershell
git diff --check
```

## Out of scope

- 이미지 resize, adaptive quality, 파일 크기 목표 기반 재압축
- 지원 입력 형식 추가
- 서버 API/presigned URL 계약 변경
- 압축본 디스크 영속화, WorkManager retry, 프로세스 사망 후 업로드 재개
- 분석 Progress 화면 레이아웃/문구 개편
- Confirmation grid 썸네일을 압축본으로 교체
- ADB/mobile-mcp 기반 런타임 검증

## Technical Debt

- none

## Cursor Result

- Changed files: `PreparedScreenshot`/`ScreenshotUploadCandidate`,
  `ScreenshotUploadPreparer`, 분석 input/outcome 및 Mock/Remote repository,
  Organize Confirmation/Route/ViewModel 계약, Progress ViewModel, 일반/공유 전달 경로,
  관련 unit test와 HEIC/HEIF fixtures.
- Build/test:
  - `:feature:organize:testDebugUnitTest --tests OrganizeViewModelTest` GREEN
  - `:core:data:testDebugUnitTest :feature:organize:testDebugUnitTest :app:testDebugUnitTest` GREEN
  - `assembleDebug` GREEN
  - `git diff --check` GREEN
- Notes: 실제 `sewing-threads.heic`, `heif-apple-circles.heif` fixture를 포함해 HEIC/HEIF
  테스트를 skip 없이 실행한다. ADB/mobile-mcp는 요청 범위에 없어 실행하지 않았다.
- Open questions: none

## Codex Review

- Blocking: none
- Nits: none
- Verdict: DONE
