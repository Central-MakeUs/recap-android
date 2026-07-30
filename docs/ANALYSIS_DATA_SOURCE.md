# ANALYSIS_DATA_SOURCE.md - 전역 스크린샷 Backend 모드

> **참고:** 파일명은 과거 "분석 데이터 소스" 시절을 유지하지만, 현재 문서는 **분석을 포함한 전역 스크린샷 backend(Mock/Remote) 런타임 전환**을 설명한다.

관련 문서:
- Mock 결과 계약: `docs/SCREENSHOT_MOCK_DATA.md`
- 로컬 저장(Room/이미지)과 Mock/Remote SoT 차이: `docs/LOCAL_DATA.md`

## 목적과 범위

포함:
- Debug에서 개발자 옵션으로 `MOCK` / `REMOTE` 선택
- Home / Storage / Capture command / Capture 상세 / 최근 정리 / 분석 / User data-summary·consent·delete Switching repository에 동일 모드 위임
- 분석 중 모드 전환 거부
- 전환 시 Mock 스크린샷 데이터 초기화 후 모드 저장

포함하지 않음:
- Splash에서 모드 hydrate 대기
- Capture 상세 content 편집(Remote PATCH) — 로드/삭제/즐겨찾기는 연결됨
- instrumentation 테스트용 Hilt replacement 인프라
- Auth(`getAccountInfo` / `withdraw`) — 계정 조회·탈퇴는 mode와 무관하게 Remote

## 빌드별 effective mode

| 빌드 | 정책 |
|------|------|
| **Debug** | 개발자 옵션으로 Mock/Remote 전환 |
| **Release (non-debug)** | 스크린샷 도메인은 **항상 Remote** |

### 구현 (코드 기준)

`DataStoreScreenshotBackendModeStore`:

- 저장값 없음 / 알 수 없는 값 → Debug에서는 `MOCK`
- **Debug** (`BuildConfig.DEBUG == true`): DataStore에 저장된 모드를 그대로 사용
- **Release (non-debug)**:
  - **읽기**: DataStore에 `MOCK`이 남아 있어도 effective mode는 **항상 `REMOTE`**
  - **쓰기**: `setMode` 호출은 **no-op**

preference key:
- 신규: `screenshot_backend_mode` (우선)
- legacy fallback: `analysis_data_source_mode`
- `setMode` 성공 시 신규 key를 기록하고 legacy key를 제거

## 런타임 구조

```text
Developer Options (Debug only UI)
        │  ScreenshotBackendSwitcher.switchTo()
        ▼
ScreenshotBackendModeStore (user_preferences DataStore)
        │  mode Flow / currentMode() / setMode()
        ▼
Switching*Repository (analysis / home / storage / capture / recent / user data)
        │
        ├── MOCK   → Mock*Repository (+ Room / private images / local consent)
        └── REMOTE → Remote*Repository (+ RemoteCaptureThumbnailCache / UserApi)

ScreenshotAnalysisRunState
        ▲
        └── ScreenshotBackendSwitcher / DeveloperViewModel이 isRunning 관찰
```

핵심 원칙:
- 호출부는 domain repository interface만 본다. Mock/Remote를 직접 주입하지 않는다.
- Auth(`getAccountInfo` / `withdraw`), onboarding, 일반 사용자 설정은 이 모드의 영향을 받지 않는다.
- 데이터 관리의 data-summary / AI 동의(consent) / 전체 삭제는 backend 모드에 따라 Mock 또는 Remote로 위임한다.
- Splash는 모드 로드를 기다리지 않는다. 요청 시 `currentMode()`로 조회한다.

## 모드 (`ScreenshotBackendMode`)

| 값 | 의미 |
|----|------|
| `MOCK` | 기기 Room + private 원본/썸네일을 SoT로 쓰는 Mock backend |
| `REMOTE` | 서버를 SoT로 쓰고 기기는 capture ID 기반 썸네일 캐시만 유지 |

API: `ScreenshotBackendModeStore`
- `mode: Flow<ScreenshotBackendMode>`
- `suspend currentMode()`
- `suspend setMode(mode)`

테스트에서는 `DataStoreScreenshotBackendModeStore.isDebugBuild`로 Debug/non-debug 분기를 주입할 수 있다.

## Switching repository

동일 `ScreenshotBackendModeStore`를 사용하는 Switching 구현:

- `SwitchingScreenshotAnalysisRepository`
- `SwitchingHomeRepository`
- `SwitchingRecentCapturesRepository`
- `SwitchingStorageRepository`
- `SwitchingCaptureMutationRepository`
- `SwitchingUserRepository` (data-summary / consent / deleteAccountData만 mode 위임; Auth는 항상 Remote)

주의:
- list 분석 overload는 **한 번** mode를 조회한 뒤 동일 구현체로만 위임한다.
- Mock/Remote 구현체는 `@Inject` concrete로 두고, interface에는 Switching만 bind한다.

## 전환 policy (`ScreenshotBackendSwitcher`)

1. 동일 모드 → no-op 성공
2. 분석 중 또는 다른 전환 진행 중 → `RejectedBusy`
3. 성공 순서: `MockScreenshotDataResetter.reset()` → `setMode(target)`
4. 초기화/저장 실패 시 mode 미변경, `Failure` 반환
5. 전환 시 Mock 데이터 삭제는 비가역이며 session/onboarding은 건드리지 않음
6. `isSwitching` StateFlow로 UI 중복 요청 방지

별도 "스크린샷 정리 데이터 초기화" 액션은 `MockScreenshotDataResetter`만 재사용하고 mode는 바꾸지 않는다.

## Remote 다중 삭제

Swagger `POST /api/v1/captures/bulk-delete`를 사용한다. Remote 다중 삭제는:

- 빈 ID set이면 API를 호출하지 않고 빈 `CaptureDeleteResult`를 반환
- 비어 있지 않으면 `bulkDelete`를 1회 호출한다 (서버는 all-or-nothing 204)
- 성공 시 요청 ID 전체를 `deletedIds`로 두고 썸네일 캐시 삭제 후 `RemoteCaptureChangeNotifier.notifyCaptureChanged()`를 한 번 호출
- 실패 시 `deletedIds`는 비우고 요청 ID 전체를 `failedIds`로 둔다 (부분 성공 없음)
- `CancellationException`은 즉시 재throw


## 주요 파일

```text
core/data/.../screenshot/backend/ScreenshotBackendMode.kt
core/data/.../screenshot/backend/ScreenshotBackendModeStore.kt
core/data/.../screenshot/backend/DataStoreScreenshotBackendModeStore.kt
core/data/.../screenshot/backend/ScreenshotBackendSwitcher.kt
core/data/.../screenshot/backend/MockScreenshotDataResetter.kt
core/data/.../screenshot/analysis/SwitchingScreenshotAnalysisRepository.kt
core/data/.../home/SwitchingHomeRepository.kt
core/data/.../storage/SwitchingStorageRepository.kt
core/data/.../capture/SwitchingCaptureMutationRepository.kt
core/data/.../user/SwitchingUserRepository.kt
core/data/.../user/MockUserRepository.kt
core/data/.../user/RemoteUserRepository.kt
feature/developer/DeveloperViewModel.kt
feature/developer/DeveloperOptionsScreen.kt
```
