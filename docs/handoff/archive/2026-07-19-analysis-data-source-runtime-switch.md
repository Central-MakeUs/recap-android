# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 목적은 서버 중심 데이터 아키텍처 전환이 아니라, 솔로 개발 중 스크린샷 분석 기능의 Mock/Remote 구현을 병렬 개발할 수 있는 Debug 런타임 스위치를 만드는 것이다.
- 현재 `ScreenshotAnalysisRepository`는 동기 `analyze()` API이며 `ScreenshotAnalysisModule`이 `MockScreenshotAnalysisRepository`를 정적으로 바인딩한다.
- 분석 작업은 `:app`의 `ScreenshotAnalysisProgressViewModel`이 소유하고 결과를 기존 Room 카드/이미지 저장 경로에 저장한다. 이 저장 구조는 이번 작업에서 유지한다.
- 개발자 옵션은 `:feature:developer`에 있어 `:app`의 Progress ViewModel을 직접 참조할 수 없다. 분석 중 전환 거부에 필요한 최소 실행 상태만 `:core:data`의 singleton으로 공유한다.
- 개발자 진입점은 이미 Debug에서만 노출된다. non-debug의 분석 소스는 현재 동작과 동일하게 Mock으로 고정하며, production Remote 전환 정책은 서버 출시 작업에서 별도로 결정한다.

## Spec

### 1. 분석 데이터 소스 모드 저장

- `AnalysisDataSourceMode` enum은 `MOCK`, `REMOTE`만 가진다.
- 기존 `UserPreferencesRepository`의 DataStore에 모드를 저장한다. 별도 startup hydrate, `Unresolved/Ready`, Splash gating, 메모리 `peek()` 계층은 만들지 않는다.
- 저장값이 없거나 읽을 수 없는 경우의 기본 effective mode는 `MOCK`이다.
- Debug에서는 저장된 모드를 읽고 쓸 수 있다. non-debug에서는 저장값과 무관하게 effective mode를 항상 `MOCK`으로 반환하고 Remote로 변경하지 않는다.
- UI 관찰용 mode `Flow`와 분석 요청용 suspend current-mode 조회 API를 제공한다. enum은 Boolean preference가 아닌 문자열 등 확장 가능한 형태로 저장하고, 알 수 없는 저장값도 `MOCK`으로 복구한다.

### 2. suspend Repository와 런타임 위임

- `ScreenshotAnalysisRepository.analyze(input)` 및 list overload를 `suspend`로 변경한다. 호출부와 기존 Mock 구현/테스트를 이에 맞춘다.
- `SwitchingScreenshotAnalysisRepository`가 `ScreenshotAnalysisRepository`의 유일한 Hilt 바인딩이 된다.
- 각 분석 요청 시 effective mode를 suspend로 조회한 후 다음 구현으로 위임한다.
  - `MOCK` → 기존 `MockScreenshotAnalysisRepository`
  - `REMOTE` → 신규 `RemoteScreenshotAnalysisRepository`
- `RemoteScreenshotAnalysisRepository`는 이번 작업에서는 stub이다. 분석 호출 시 전용 `RemoteAnalysisNotWiredException`을 던진다. 실제 upload/organize/poll API는 구현하지 않는다.
- list overload는 선택된 동일 구현체의 list API로 한 번만 위임하여 한 요청 안에서 모드가 섞이지 않게 한다.

### 3. 분석 실행 상태 공유

- `:core:data`에 singleton `ScreenshotAnalysisRunState`를 추가하고 `isRunning: StateFlow<Boolean>`을 노출한다.
- `ScreenshotAnalysisProgressViewModel`이 분석 시작/종료를 run state에 반영한다. 취소, 예외, 빈 입력을 포함한 모든 종료 경로에서 반드시 idle로 복구한다.
- 기존 작업 취소 직후 새 작업이 시작되는 경우 이전 작업의 `finally`가 새 작업의 running 상태를 덮어쓰지 않도록, 단순 Boolean last-write가 아니라 active-run count 또는 동등한 race-safe token 방식으로 구현한다.
- `startMockAnalysis()`는 실제 선택 모드와 무관한 이름인 `startAnalysis()`로 변경하고 호출부/테스트를 갱신한다.
- Repository 예외는 분석 coroutine 밖으로 전파해 앱을 크래시시키지 않는다. `CancellationException`은 다시 던지고, Remote stub을 포함한 일반 예외는 Timber에 기록한 뒤 기존 UiState의 `errorMessage`에 raw exception이 아닌 고정된 안전한 메시지를 설정한다. `isRunning`은 false로 종료한다.

### 4. 개발자 옵션 전환 UX

- `DeveloperOptionsUiState`에 현재 mode, `isAnalysisRunning`, `isSwitching`, 확인 다이얼로그에 대기 중인 target mode를 표현한다.
- 개발자 옵션 화면에 현재 분석 데이터 소스(Mock/Remote)를 표시하고 반대 모드로 전환하는 액션을 추가한다.
- 동일 모드 선택은 no-op이다.
- 분석 중이거나 이미 전환 처리 중이면 전환 버튼을 비활성화한다. ViewModel 액션 처리에서도 다시 검사하여 직접 호출 시 전환을 거부하고 안전한 feedback resource를 설정한다.
- 유휴 상태에서 전환을 요청하면 "전환 시 기존 로컬 스크린샷 정리 데이터가 삭제된다"는 확인 다이얼로그를 표시한다.
- 확인 시 다시 running 상태를 검사한 뒤 다음 순서로 처리한다.
  1. `ScreenshotCardRepository.deleteAllCards()`
  2. `ScreenshotImageStorage.clearStoredImages()`
  3. 새 mode 저장
- 성공하면 현재 mode와 성공 feedback을 갱신한다. 실패하면 기존 mode를 유지하고 실패 feedback을 표시한다. 세션 토큰과 온보딩 preference는 변경하지 않는다.
- 다이얼로그 취소는 데이터와 mode를 변경하지 않는다.
- 기존 "스크린샷 정리 데이터 초기화" 기능은 유지한다.
- 새 UI 문자열은 기존 위치인 `core/design/src/main/res/values/strings.xml`에 정의한다. 기존 Developer Options 화면의 UI 스타일과 UiState/Action 패턴을 따르고 필요한 Mock/Remote 및 다이얼로그 상태 Preview를 `RECAPTheme`로 추가한다. 새 아이콘과 production dependency는 추가하지 않는다.

### 5. 테스트 격리

- 런타임 토글은 수동 Debug 개발 편의를 위한 기능이다. UI 테스트의 서버 차단 수단으로 사용하지 않는다.
- 이 작업에서 새로운 instrumentation/Hilt 테스트 인프라를 만들지 않는다. 단위 테스트는 fake/mock dependency를 직접 주입하여 네트워크와 DataStore 전역 상태에 의존하지 않게 한다.

## Files to Touch

- `core/data/src/main/java/com/chalkak/recap/core/data/UserPreferencesRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/AnalysisDataSourceMode.kt` (new, 이름은 역할을 유지하는 범위에서 조정 가능)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisRunState.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/RemoteScreenshotAnalysisRepository.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/SwitchingScreenshotAnalysisRepository.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/ScreenshotAnalysisModule.kt`
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperViewModel.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperRoute.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperOptionsScreen.kt`
- `core/design/src/main/res/values/strings.xml`
- 관련 `core/data`, `app`, `feature/developer` unit test 파일

## Acceptance Criteria

- Debug에서 개발자 옵션을 통해 Mock/Remote 모드를 확인하고 앱 재시작 후에도 선택값을 유지할 수 있다.
- 앱 시작 Splash는 모드 DataStore hydrate를 기다리지 않으며, 첫 분석 요청 자체가 저장된 effective mode를 안전하게 조회한다.
- Mock 모드 분석은 기존 Mock 결과 생성과 Room/이미지 저장 동작을 유지한다.
- Remote 모드 분석은 Remote stub으로 위임되고, 앱이 크래시하지 않으며 분석 UiState가 안전한 오류와 idle 상태로 종료된다.
- 분석 실행 중에는 전환 버튼이 비활성화되고 ViewModel 직접 액션도 mode 변경·카드 삭제·이미지 삭제를 수행하지 않는다.
- 유휴 상태의 전환 확인 시 카드와 이미지를 초기화한 후에만 새 mode를 저장한다. 취소 또는 실패 시 새 mode가 저장되지 않는다.
- 전환은 분석 job을 취소하지 않고, 분석 중 전환 거부 정책으로 동시성 문제를 제한한다.
- non-debug effective mode는 항상 Mock이며 Remote 선택이 적용되지 않는다.
- 기존 Home/Collection/Detail, Room 기반 카드 SoT, 세션 토큰, 온보딩 상태와 기존 수동 데이터 초기화 기능은 유지된다.
- 단위 테스트가 mode 기본값/저장, Mock/Remote 위임, Remote 오류 처리, run-state 종료 복구, 분석 중 전환 거부, 확인/취소/성공/실패 전환을 검증한다.
- 관련 단위 테스트와 `assembleDebug`가 통과한다.

## Validation

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :app:testDebugUnitTest :feature:developer:testDebugUnitTest assembleDebug
```

## Out of scope

- `RecapBackend`, `BackendRouter`, domain facade 도입
- Splash mode hydrate gating 및 `Unresolved/Ready` startup 상태
- `CaptureRepository` 기반 실제 Remote upload/organize/poll 구현
- Home/Collection/Detail/Storage/Auth Repository 전환
- 서버 SoT 전환, Room `ScreenshotCardRepository` 제거 또는 캐시 재설계
- 최근 로컬 스크린샷 40장 자동/수동 Mock 시드
- Mock 인메모리 catalog, `sessionEpoch`, 전역 cache invalidation
- Mock/Remote ID 및 썸네일 캐시 namespace 재설계
- release 빌드의 Remote 강제 정책
- instrumentation/Compose UI test용 Hilt replacement 인프라 구축
- 기존 개발자 옵션 화면 전체 디자인 리팩터링

## Technical Debt

- 없음. 서버 API가 준비된 뒤 Remote 분석 구현과 서버 SoT 전환은 별도 작업으로 계획한다.

## Cursor Result
- Changed files: core/data/.../UserPreferencesRepository.kt, AnalysisDataSourceMode.kt, ScreenshotAnalysisRunState.kt, ScreenshotAnalysisRepository.kt, MockScreenshotAnalysisRepository.kt, RemoteScreenshotAnalysisRepository.kt, SwitchingScreenshotAnalysisRepository.kt, ScreenshotAnalysisModule.kt, app/.../ScreenshotAnalysisProgressViewModel.kt, RecapNavHost.kt, feature/developer/DeveloperViewModel.kt, DeveloperRoute.kt, DeveloperOptionsScreen.kt, feature/developer/build.gradle.kts, core/design/.../strings.xml, 관련 unit test 파일
- Build/test: `.\gradlew.bat :core:data:testDebugUnitTest :app:testDebugUnitTest :feature:developer:testDebugUnitTest assembleDebug` GREEN
- Open questions: none

## Codex Review

- Blocking: none
- Nits:
  - `git diff --check`가 `ScreenshotAnalysisProgressViewModel.kt` 전체의 trailing whitespace/CR 문자와 EOF 공백을 보고한다. 동작 차단 이슈는 아니지만 diff 노이즈를 제거해야 한다.
- Validation: Cursor가 보고한 `:core:data:testDebugUnitTest :app:testDebugUnitTest :feature:developer:testDebugUnitTest assembleDebug` GREEN 결과를 신뢰하여 동일 검증은 재실행하지 않았다.
- Verdict: DONE
