# ANALYSIS_DATA_SOURCE.md - Mock/Remote 분석 데이터 소스

이 문서는 스크린샷 분석의 Debug 런타임 Mock/Remote 전환 구조를 정리한다.
서버 SoT 전환이나 전체 백엔드 아키텍처 교체가 아니라, **분석 기능의 Mock 구현과 Remote 구현을 병렬 개발**하기 위한 스위치다.

관련 문서:
- Mock 결과 계약: `docs/SCREENSHOT_MOCK_DATA.md`
- 로컬 저장(Room/이미지): `docs/LOCAL_DATA.md`

## 목적과 범위

포함:
- Debug에서 개발자 옵션으로 `MOCK` / `REMOTE` 선택
- 분석 요청 시 effective mode에 따라 repository 위임
- 분석 중 모드 전환 거부
- 전환 시 로컬 스크린샷 정리 데이터 초기화 후 모드 저장

포함하지 않음:
- Splash에서 모드 hydrate 대기
- Home/Collection/Detail의 서버 SoT 전환
- 실제 upload / organize / poll Remote API 구현
- instrumentation 테스트용 Hilt replacement 인프라
- release 빌드의 Remote 강제 정책 **코드 반영** (목표 정책은 아래 "현재 vs 목표" 참고. Remote stub 상태에서는 구현하지 않음)

## 빌드별 effective mode: 현재 vs 목표

제품 관점에서는 **release(출시) 빌드의 분석은 서버(Remote)가 맞다.** 다만 Remote 구현이 아직 stub이므로, **지금 코드는 release에서도 Mock을 강제**한다. 문서와 코드를 읽을 때 아래 표를 구분한다.

| 빌드 | 목표 정책 (서버 연동 후) | 현재 구현 |
|------|--------------------------|-----------|
| **Debug** | 개발자 옵션으로 Mock/Remote 전환 | 동일 |
| **Release (non-debug)** | 분석은 **항상 Remote** | Remote 미구현이라 **항상 Mock** |

### 현재 구현 (코드 기준)

`UserPreferencesRepository.resolveEffectiveMode()` / `setAnalysisDataSourceMode()`:

- 저장값 없음 / 알 수 없는 값 → `MOCK`
- **Debug** (`BuildConfig.DEBUG == true`): DataStore에 저장된 모드를 그대로 사용
- **Release (non-debug)**:
  - **읽기**: DataStore에 `REMOTE`가 남아 있어도 effective mode는 **항상 `MOCK`**
  - **쓰기**: `setAnalysisDataSourceMode(REMOTE)` 호출은 **no-op** (저장하지 않음, 예외 없이 무시)

이유: `RemoteScreenshotAnalysisRepository`가 아직 stub이라 release에서 Remote를 켜면 분석이 전부 실패한다. 개발자 옵션 스위치는 Debug 편의용이며, release 정책은 Remote API 연동 handoff에서 바꾼다.

### 목표 정책 (서버 출시 시)

Remote API가 `RemoteScreenshotAnalysisRepository`에 연결되면:

- **Release**: effective mode를 **항상 `REMOTE`**로 고정 (또는 빌드 타입으로 분기, DataStore 토글 없이)
- **Debug**: 지금처럼 개발자 옵션으로 Mock/Remote 전환 유지
- release에서 Mock으로 되돌리는 UI는 제공하지 않음 (필요 시 internal/debug 전용)

구현 시 변경 예상 지점: `UserPreferencesRepository.resolveEffectiveMode()`의 non-debug 분기를 `REMOTE` 반환으로 교체.

## 런타임 구조

```text
Developer Options (Debug only UI)
        │  setAnalysisDataSourceMode()
        ▼
UserPreferencesRepository (DataStore)
        │  getAnalysisDataSourceMode() / analysisDataSourceMode Flow
        ▼
SwitchingScreenshotAnalysisRepository  ◀── Hilt @Binds ScreenshotAnalysisRepository
        │
        ├── MOCK   → MockScreenshotAnalysisRepository
        └── REMOTE → RemoteScreenshotAnalysisRepository  (현재 stub)

ScreenshotAnalysisProgressViewModel (:app)
        │  startAnalysis() → repository.analyze() → Room/이미지 저장
        ▼
ScreenshotAnalysisRunState (:core:data singleton)
        ▲
        └── DeveloperViewModel이 isRunning을 관찰해 전환 버튼 비활성
```

핵심 원칙:
- 호출부는 `ScreenshotAnalysisRepository`만 본다. Mock/Remote를 직접 주입하지 않는다.
- 저장 SoT는 그대로 Room 카드 + 앱 private 이미지다. 모드 스위치가 저장 경로를 바꾸지 않는다.
- Splash는 모드 로드를 기다리지 않는다. 첫 분석 요청이 `getAnalysisDataSourceMode()`로 안전히 조회한다.

## 모드 (`AnalysisDataSourceMode`)

| 값 | 의미 |
|----|------|
| `MOCK` | 로컬 mock 결과 생성 (`docs/SCREENSHOT_MOCK_DATA.md`) |
| `REMOTE` | 서버 분석 경로 (현재 stub, 미연결) |

저장:
- DataStore key: `analysis_data_source_mode` (문자열, enum name)
- API: `UserPreferencesRepository`
  - `analysisDataSourceMode: Flow<AnalysisDataSourceMode>` — UI 관찰
  - `suspend getAnalysisDataSourceMode()` — 분석 요청 시점 조회
  - `suspend setAnalysisDataSourceMode(mode)` — Debug 전환 저장

effective mode 규칙은 **「빌드별 effective mode: 현재 vs 목표」** 절을 본다. 요약:

- **Debug**: 저장된 모드 사용 (개발자 옵션 전환)
- **Release (현재)**: 항상 `MOCK`
- **Release (목표)**: Remote 연동 후 항상 `REMOTE`

테스트에서는 `UserPreferencesRepository.isDebugBuild`로 Debug/non-debug 분기를 주입할 수 있다.

## Repository 위임

| 타입 | 역할 |
|------|------|
| `ScreenshotAnalysisRepository` | `suspend analyze(input)` / `suspend analyze(inputs)` 계약 |
| `SwitchingScreenshotAnalysisRepository` | 유일한 Hilt 바인딩. 요청마다 effective mode 조회 후 위임 |
| `MockScreenshotAnalysisRepository` | 기존 mock 구현 |
| `RemoteScreenshotAnalysisRepository` | stub. `RemoteAnalysisNotWiredException` throw |
| `ScreenshotAnalysisModule` | `Switching` → interface 바인딩 |

주의:
- list overload는 **한 번** mode를 조회한 뒤 동일 구현체의 list API로만 위임한다. 한 요청 안에서 MOCK/REMOTE가 섞이면 안 된다.
- Mock/Remote 구현체는 `@Inject` concrete로 두고, interface에는 Switching만 bind한다.

## 분석 실행과 오류 처리

소유자: `:app`의 `ScreenshotAnalysisProgressViewModel`

흐름:
1. `startAnalysis(images)` (구 `startMockAnalysis`)
2. `ScreenshotAnalysisRunState.beginRun()`
3. 이미지별 `repository.analyze(...)` → Room/이미지 persist
4. 성공/빈 입력/예외/취소 모든 경로에서 `endRun()` (`finally`)

`ScreenshotAnalysisRunState`:
- `isRunning: StateFlow<Boolean>`
- active-run count로 race-safe. 이전 job의 `finally`가 새 job의 running을 지우지 않는다.

예외 정책:
- `CancellationException` → 다시 throw
- 그 외(Remote stub 포함) → Timber 기록, UiState `errorMessage`에 **고정 안전 메시지** (raw exception 금지), idle로 종료

## 개발자 옵션 전환 UX

화면: `:feature:developer` Developer Options (Debug 진입점만 노출)

전환 규칙:
1. 동일 모드 선택 → no-op
2. 분석 중(`isAnalysisRunning`) 또는 전환 중(`isSwitching`) → 버튼 비활성 + 액션에서도 거부
3. 유휴에서 전환 요청 → 확인 다이얼로그
4. 확인 시 다시 running 검사 후:
   1. `ScreenshotCardRepository.deleteAllCards()`
   2. `ScreenshotImageStorage.clearStoredImages()`
   3. `setAnalysisDataSourceMode(target)`
5. 실패 시 기존 mode 유지. 세션 토큰/온보딩 preference는 건드리지 않음
6. 기존 "스크린샷 정리 데이터 초기화" 기능은 별도로 유지

문자열: `core/design/src/main/res/values/strings.xml`의 `developer_options_*analysis_data_source*` / `switch_*`

## 추후 Remote 구현 시 가이드

`RemoteScreenshotAnalysisRepository` 구현과 **release effective mode를 `REMOTE`로 전환**은 같은 handoff(또는 연속 handoff)에서 다룬다.

권장 순서:
1. upload / organize / poll(또는 동등 API) 클라이언트를 `:core:data`에 추가
2. `RemoteScreenshotAnalysisRepository.analyze`에서 stub 예외를 실제 호출로 교체
3. 결과를 기존 `ScreenshotAnalysisResult`로 매핑
4. `UserPreferencesRepository`: non-debug 분기를 **항상 `REMOTE`** 반환으로 변경 (DataStore에 Mock이 남아 있어도 release는 Remote만 사용)
5. Progress ViewModel의 persist 경로는 그대로 두고, Remote 전용 실패/재시도 UX가 필요하면 별도 handoff로 확장
6. Debug 개발자 옵션 Mock/Remote 전환은 유지 (release 정책과 분리)

하지 말 것:
- UI 테스트의 "서버 차단" 수단으로 이 런타임 토글을 쓰지 않는다. 단위 테스트는 fake/mock을 직접 주입한다.
- Splash hydrate / Unresolved-Ready 계층을 이 스위치 위에 올리지 않는다 (의도적으로 단순화됨).
- Mock/Remote ID·썸네일 namespace 재설계, 전역 cache invalidation은 별도 작업이다.

## 주요 파일

```text
core/data/.../UserPreferencesRepository.kt
core/data/.../screenshot/AnalysisDataSourceMode.kt
core/data/.../screenshot/ScreenshotAnalysisRepository.kt
core/data/.../screenshot/MockScreenshotAnalysisRepository.kt
core/data/.../screenshot/RemoteScreenshotAnalysisRepository.kt
core/data/.../screenshot/SwitchingScreenshotAnalysisRepository.kt
core/data/.../screenshot/ScreenshotAnalysisModule.kt
core/data/.../screenshot/ScreenshotAnalysisRunState.kt
app/.../ScreenshotAnalysisProgressViewModel.kt
feature/developer/DeveloperViewModel.kt
feature/developer/DeveloperOptionsScreen.kt
```

단위 테스트 위치:
- `core/data` — mode 저장, Switching 위임, Remote stub, RunState
- `app` — Progress ViewModel 예외/idle 복구
- `feature/developer` — 전환 확인/취소/거부/성공/실패
