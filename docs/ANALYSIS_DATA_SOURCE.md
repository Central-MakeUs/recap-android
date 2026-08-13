# ANALYSIS_DATA_SOURCE.md - 전역 스크린샷 Backend 선택

> **참고:** 파일명은 과거 "분석 데이터 소스" 시절을 유지하지만, 현재 문서는 **분석을 포함한 전역 스크린샷 backend(Mock/Remote)의 build-time 고정 선택**을 설명한다.
>
> **과거 런타임 전환 스냅샷:** 제거된 DataStore mode / Switching repository 구조는 `docs/MOCK_REMOTE_CHANGE.md`를 본다. 이 파일은 현재 사실 요약본이다.

관련 문서:
- 과거 런타임 전환 세부 스냅샷: `docs/MOCK_REMOTE_CHANGE.md`
- Mock 결과 계약: `docs/SCREENSHOT_MOCK_DATA.md`
- 로컬 저장(Room/이미지)과 Mock/Remote SoT 차이: `docs/LOCAL_DATA.md`

## 목적과 범위

포함:
- `:core:data` `BuildConfig.USE_MOCK_BACKEND`로 프로세스 수명 동안 Mock/Remote 고정
- Home / Storage / Capture command / Capture 상세 / 최근 정리 / 분석 / User data-summary·consent·delete repository에 동일 선택 적용
- Auth(`getAccountInfo` / `withdraw`)는 Mock 선택에서도 Remote 경로 유지

포함하지 않음:
- 런타임 Mock/Remote 전환 UI 또는 DataStore mode
- Splash에서 backend hydrate 대기
- Capture 상세 content 편집(Remote PATCH) — 로드/삭제/즐겨찾기는 연결됨
- instrumentation 테스트용 Hilt replacement 인프라

## 빌드별 effective backend

단일 기준: `com.chalkak.recap.core.data.BuildConfig.USE_MOCK_BACKEND`

| 조건 | `USE_MOCK_BACKEND` |
|------|--------------------|
| 덮어쓰기 없음 + **debug** | `true` (Mock) |
| **release** | 항상 `false` (Remote; `-P`/local.properties 무시) |
| app **qa** | 항상 `false` (Remote; `:core:data` release fallback, `-P`/local.properties 무시) |
| `local.properties` `USE_MOCK_BACKEND` | **debug만** 해당 값 (`KAKAO_NATIVE_APP_KEY`와 같이 로컬 파일에서 읽음) |
| `-PUSE_MOCK_BACKEND=true` | **debug만** Mock (`local.properties`보다 우선) |
| `-PUSE_MOCK_BACKEND=false` | **debug만** Remote (`local.properties`보다 우선) |
| 그 외 property/local 값 | Gradle configuration 단계에서 실패 |

예:

```powershell
# 로컬 기본 Remote: local.properties에 USE_MOCK_BACKEND=false
.\gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=false
.\gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=true
# release/qa는 -P / local.properties와 무관하게 Remote 유지
.\gradlew.bat assembleRelease
.\gradlew.bat :app:assembleQa
```

## Build-time 선택 구조

```text
BuildConfig.USE_MOCK_BACKEND (core:data)
        │
        ▼
Hilt Module @Provides (Provider<Mock...> / Provider<Remote...>)
        │
        ├── true  → Mock*Repository (+ Room / private images / local consent)
        └── false → Remote*Repository (+ RemoteCaptureThumbnailCache / UserApi)

MockUserRepository (Mock 선택 시)
        └── getAccountInfo / withdraw → RemoteUserRepository
```

핵심 원칙:
- 호출부는 domain repository interface만 본다. Feature/ViewModel은 BuildConfig나 Mock/Remote concrete type을 알지 않는다.
- 선택되지 않은 Mock/Remote 구현은 `Provider`를 통해 lazy resolve하며, 선택 때문에 eager 생성되지 않는다.
- Auth(`getAccountInfo` / `withdraw`), onboarding, 일반 사용자 설정은 backend 선택과 무관하게 Remote auth / 기존 경로를 유지한다.
- 데이터 관리의 data-summary / AI 동의(consent) / 전체 삭제는 선택된 Mock 또는 Remote 구현만 실행한다.
- 프로세스 실행 중 backend를 갈아끼우지 않는다.

## Mock / Remote 의미

| 값 | 의미 |
|----|------|
| Mock (`USE_MOCK_BACKEND=true`) | 기기 Room + private 원본/썸네일을 SoT로 쓰는 Mock backend |
| Remote (`USE_MOCK_BACKEND=false`) | 서버를 SoT로 쓰고 기기는 capture ID 기반 썸네일 캐시만 유지 |

## Repository 선택

동일 `BuildConfig.USE_MOCK_BACKEND`를 사용하는 Hilt 제공:

- `ScreenshotAnalysisRepository`
- `HomeRepository`
- `RecentCapturesRepository`
- `StorageRepository`
- `CaptureMutationRepository`
- `SearchRepository`
- `ScreenshotDetailRepository`
- `UserRepository` (Mock 선택 시 `MockUserRepository`; Auth method만 Remote 위임)

개발자 옵션의 "스크린샷 정리 데이터 초기화"는 `MockScreenshotDataResetter`만 사용하며, Remote 빌드에서도 서버 데이터 삭제로 바뀌지 않는다.

## Remote 다중 삭제

Swagger `POST /api/v1/captures/bulk-delete`를 사용한다. Remote 다중 삭제는:

- 빈 ID set이면 API를 호출하지 않고 빈 `CaptureDeleteResult`를 반환
- 비어 있지 않으면 `bulkDelete`를 1회 호출한다 (서버는 all-or-nothing 204)
- 성공 시 요청 ID 전체를 `deletedIds`로 두고 썸네일 캐시 삭제 후 `RemoteCaptureChangeNotifier.notifyCaptureChanged()`를 한 번 호출
- 실패 시 `deletedIds`는 비우고 요청 ID 전체를 `failedIds`로 둔다 (부분 성공 없음)
- `CancellationException`은 즉시 재throw

## 주요 파일

```text
core/data/build.gradle.kts
core/data/.../backend/BackendSelection.kt
core/data/.../backend/UseMockBackendProperty.kt
core/data/.../screenshot/backend/MockScreenshotDataResetter.kt
core/data/.../screenshot/analysis/ScreenshotAnalysisModule.kt
core/data/.../home/HomeModule.kt
core/data/.../storage/StorageModule.kt
core/data/.../capture/CaptureMutationModule.kt
core/data/.../search/SearchModule.kt
core/data/.../screenshot/persistence/ScreenshotDetailModule.kt
core/data/.../user/UserModule.kt
core/data/.../user/MockUserRepository.kt
core/data/.../user/RemoteUserRepository.kt
app/.../observability/ObservabilityBootstrap.kt
```
