# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 현재 Debug 개발자 옵션의 `AnalysisDataSourceMode(MOCK/REMOTE)`가 분석뿐 아니라 Home, 최근 정리 목록, Storage, Capture command의 런타임 데이터 소스 선택에도 사용되고 있다.
- 전역 스크린샷 backend 선택으로 역할이 확장되었지만 enum/API/문서/개발자 옵션 명칭은 여전히 "analysis data source"에 묶여 있다.
- Mock 구현은 Room `ScreenshotCardRepository`와 앱 private 원본/썸네일을 서버 대체 SoT로 사용한다. Remote 구현은 서버를 SoT로 사용하고 기기에는 capture ID 기반 썸네일 캐시만 둔다.
- 현재 `Local*Repository` 이름은 "Mock backend"와 "Remote에서 사용하는 로컬 캐시"를 구분하지 못한다.
- 모드 전환 시 Mock Room 데이터와 private 이미지/썸네일을 삭제하는 것은 의도된 정책이다. 이 정책과 분석 실행 중 전환 거부 로직은 현재 `DeveloperViewModel`이 직접 소유한다.
- Swagger에는 batch delete가 없고 `DELETE /api/v1/captures/{captureId}` 단건 API만 있다. 현재 Remote 다중 삭제는 중간 실패 시 일부 서버 삭제가 완료되어도 캐시 정리와 refresh 알림을 하지 않는 문제가 있다.
- Capture 상세의 전역 Mock/Remote 연결은 Storage 구현 이후 별도 작업으로 진행한다. 최근 정리 전체 목록 Remote API도 서버에 아직 없다.
- 사용자는 이번 작업에서 조회 결과의 Empty/Error 구분을 명시적으로 제외했다.

## Spec

### 1. 전역 스크린샷 backend 모드로 승격

- `AnalysisDataSourceMode`를 `ScreenshotBackendMode`로 교체하고 값은 `MOCK`, `REMOTE`만 유지한다.
- 모드의 의미는 "분석 구현 선택"이 아니라 "스크린샷 도메인 전체의 Mock backend/Remote backend 선택"이다. Auth, onboarding, 일반 사용자 설정의 데이터 소스에는 영향을 주지 않는다.
- `UserPreferencesRepository`에서 모드 관련 API와 build별 effective-mode 정책을 분리한다.
- `ScreenshotBackendModeStore` 계약과 DataStore 구현체를 `:core:data`에 추가한다.
  - `mode: Flow<ScreenshotBackendMode>` 또는 동등한 관찰 API
  - `suspend currentMode(): ScreenshotBackendMode`
  - `suspend setMode(mode: ScreenshotBackendMode)`
- 기존과 동일한 effective-mode 정책을 보존한다.
  - Debug: 저장값을 사용하고 런타임 전환 허용
  - non-debug 현재 단계: Remote 미완성 정책에 따라 항상 `MOCK`; `REMOTE` 저장 요청은 no-op
  - 알 수 없는 저장값/저장값 없음: `MOCK`
- DataStore는 기존 `user_preferences`를 그대로 사용한다. 새 DataStore 파일을 만들지 않는다.
- preference key는 새 의미에 맞는 `screenshot_backend_mode`를 사용한다. 새 key가 있으면 이를 우선하고, 새 key가 없을 때만 기존 `analysis_data_source_mode` 값을 fallback으로 읽는다. 다음 `setMode` 성공 시 새 key를 기록하고 legacy key를 제거한다.
- 모든 Switching repository가 `UserPreferencesRepository` 대신 `ScreenshotBackendModeStore`만 의존하도록 변경한다.
  - `SwitchingScreenshotAnalysisRepository`
  - `SwitchingHomeRepository`
  - `SwitchingRecentCapturesRepository`
  - `SwitchingStorageRepository`
  - `SwitchingCaptureCommandRepository`
- 한 번의 list 분석 요청은 기존처럼 모드를 한 번만 resolve하여 단일 delegate로 처리한다.

### 2. Mock 데이터 초기화와 전환 policy 분리

- Mock 스크린샷 데이터만 초기화하는 `MockScreenshotDataResetter`를 `:core:data`에 추가한다.
  - `ScreenshotCardRepository.deleteAllCards()`
  - `ScreenshotImageStorage.clearStoredImages()`
  - session token, onboarding, 사용자 설정은 건드리지 않는다.
  - 기존 `LocalAppDataResetter`는 로그아웃/계정 초기화 용도이므로 재사용하거나 의미를 변경하지 않는다.
- `ScreenshotBackendSwitcher`를 `:core:data` singleton으로 추가하고 아래 정책을 소유하게 한다.
  - 현재 effective mode와 target이 같으면 no-op 성공
  - 분석 실행 중이면 전환 거부
  - 동시에 두 번 전환되지 않도록 `Mutex` 또는 동등한 직렬화 보장
  - 전환 순서: Mock 스크린샷 데이터 초기화 완료 후 target mode 저장
  - 초기화 또는 mode 저장 실패 시 실패 결과 반환, mode는 변경하지 않음
  - 전환 시 Mock 데이터 삭제는 의도된 비가역 정책이며, 전환 후 기존 Mock 데이터를 복원하지 않는다.
  - `isSwitching` 관찰 상태를 제공하여 UI가 중복 요청을 막을 수 있게 한다.
- `DeveloperViewModel`은 `ScreenshotCardRepository`, `ScreenshotImageStorage`, `UserPreferencesRepository`를 통해 전환 순서를 직접 구현하지 않고 `ScreenshotBackendModeStore`와 `ScreenshotBackendSwitcher`를 사용한다.
- 기존 별도 "스크린샷 정리 데이터 초기화" 액션도 `MockScreenshotDataResetter`를 재사용하되 mode는 변경하지 않는다.
- 개발자 옵션의 Kotlin API, UiState/Action, resource identifier 및 사용자 표시 문구를 "analysis data source"에서 "screenshot backend/data source" 의미로 정리한다. 사용자 확인 문구에는 전환 시 Mock 스크린샷 데이터가 삭제됨을 유지한다.

### 3. Local/Mock 및 Remote 캐시 명명 명확화

- 아래 concrete 구현 파일/클래스/생성자 parameter/test 이름을 의미에 맞게 변경한다.
  - `LocalHomeRepository` -> `MockHomeRepository`
  - `LocalRecentCapturesRepository` -> `MockRecentCapturesRepository`
  - `LocalStorageRepository` -> `MockStorageRepository`
  - `LocalCaptureCommandRepository` -> `MockCaptureCommandRepository`
  - `CaptureThumbnailCache` -> `RemoteCaptureThumbnailCache`
- Repository interface와 Switching/Remote 구현 이름은 유지한다.
- Room `ScreenshotCardRepository`와 `ScreenshotImageStorage`는 이번 작업에서 이름을 바꾸지 않는다. 이들은 Mock 구현 내부 저장과 기존 Capture 과도기 구현에서 계속 사용한다.
- DI binding과 테스트 import/fixture 이름을 모두 새 명명에 맞춘다. 구 클래스/typealias를 호환용으로 남기지 않는다.
- Remote repository만 `RemoteCaptureThumbnailCache`를 주입받는다. Mock repository가 Remote cache를 참조하지 않도록 한다.

### 4. Remote 다중 삭제 부분 성공 처리

- `:core:model` capture 영역에 삭제 결과 모델을 추가한다.
  - `deletedIds: Set<Long>`
  - `failedIds: Set<Long>`
  - 전체 성공/부분 성공/전체 실패를 위 두 집합으로 판별 가능해야 한다.
- `CaptureCommandRepository.deleteCaptures` 반환을 `Result<CaptureDeleteResult>` 또는 동일 정보를 보존하는 명시적 결과 계약으로 변경한다.
- Mock 구현:
  - 빈 입력은 두 집합이 빈 성공 결과
  - Room 삭제 성공 시 요청 ID 전체를 `deletedIds`로 반환
  - private 파일 삭제는 기존처럼 best-effort
  - Room 삭제 자체가 실패하면 `Result.failure`
- Remote 구현:
  - 빈 입력은 두 집합이 빈 성공 결과
  - 각 ID의 단건 DELETE를 시도하고 성공/실패 ID를 각각 수집한다. 한 ID 실패 때문에 나머지 ID 처리를 중단하지 않는다.
  - `CancellationException`은 즉시 재throw한다.
  - 성공한 ID만 Remote 썸네일 캐시에서 삭제한다.
  - 성공한 ID가 하나라도 있으면 `CaptureDataChangeNotifier.notifyChanged()`를 정확히 한 번 호출한다.
  - 단건 API 실패를 raw exception/message로 UI에 노출하지 않는다.
- `CollectionViewModel` 삭제 처리:
  - 전체 성공: 기존처럼 selection을 종료하고 실제 `deletedIds.size`로 성공 toast event 발생
  - 부분 성공: 삭제된 ID는 selection에서 제거하고, 실패 ID만 선택 상태로 유지하며 `isDeleting=false`; 삭제/실패 개수를 포함한 부분 실패 toast event 발생
  - 전체 실패 또는 repository `Result.failure`: 기존 선택을 유지하고 `isDeleting=false`; 재시도 가능한 실패 toast event 발생
  - ViewModel이 요청 ID 개수만으로 성공 개수를 추정하지 않는다.
- `CollectionRoute`와 `core/design` 문자열 리소스에 전체 성공/부분 성공/전체 실패 toast 표시를 연결한다. 새 UI 컴포넌트나 새 production dependency는 추가하지 않는다.

### 5. 문서 동기화

- `docs/ANALYSIS_DATA_SOURCE.md`를 전역 backend 모드 구조에 맞게 갱신한다. 파일명 변경은 이번 작업에서 하지 않되, 문서 첫 부분에 현재 문서가 분석을 포함한 전역 스크린샷 backend 전환을 설명한다고 명시한다.
- `docs/LOCAL_DATA.md`에 Mock backend의 Room/원본/썸네일 SoT와 Remote backend의 서버 SoT/로컬 썸네일 캐시 차이를 기록한다.
- `docs/PROJECT.md`의 데이터/외부 연동 항목에 `ScreenshotBackendModeStore`, `ScreenshotBackendSwitcher`, 전역 runtime switch를 반영한다.

## Files to Touch

- `core/data/src/main/java/com/chalkak/recap/core/data/UserPreferencesRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/AnalysisDataSourceMode.kt` (remove/replace)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/*BackendMode*.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/*BackendSwitcher*.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/*DataResetter*.kt` (new)
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/SwitchingScreenshotAnalysisRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/home/{Local*,Mock*,Switching*,Remote*}.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/storage/{Local*,Mock*,Switching*,Remote*}.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/capture/{CaptureCommandRepository,LocalCaptureCommandRepository,MockCaptureCommandRepository,RemoteCaptureCommandRepository,SwitchingCaptureCommandRepository,CaptureThumbnailCache,RemoteCaptureThumbnailCache}.kt`
- 관련 `core/data` Hilt module 파일
- `core/model/src/main/java/com/chalkak/recap/core/model/capture/*`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperViewModel.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperOptionsScreen.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/{CollectionContract,CollectionViewModel,CollectionRoute}.kt`
- `core/design/src/main/res/values/strings.xml`
- 위 변경 대상의 `src/test` 대응 파일
- `docs/PROJECT.md`
- `docs/ANALYSIS_DATA_SOURCE.md`
- `docs/LOCAL_DATA.md`

## Acceptance Criteria

1. production/test/docs에서 `AnalysisDataSourceMode`, `analysisDataSourceMode`, `getAnalysisDataSourceMode`, `setAnalysisDataSourceMode` 참조가 남지 않는다. archive 문서는 제외한다.
2. `ScreenshotBackendModeStore`가 Debug 저장 모드, non-debug 현재 MOCK 강제 정책, unknown/default MOCK 정책과 legacy preference fallback/migration을 단위 테스트로 검증한다.
3. 모든 Switching repository가 동일한 `ScreenshotBackendModeStore`를 기준으로 MOCK/REMOTE delegate를 선택하며 기존 기능 테스트가 새 이름으로 통과한다.
4. 개발자 옵션에서 동일 모드 no-op, 분석 중 거부, 중복 전환 거부, 초기화 실패, mode 저장 실패, 성공 전환을 `ScreenshotBackendSwitcher`/`DeveloperViewModel` 테스트로 검증한다.
5. 성공 전환은 Mock Room 데이터 및 private 이미지/썸네일을 삭제한 뒤 mode를 저장하며 session/onboarding 데이터는 변경하지 않는다.
6. `LocalHomeRepository`, `LocalRecentCapturesRepository`, `LocalStorageRepository`, `LocalCaptureCommandRepository`, `CaptureThumbnailCache` type/file 참조가 남지 않고 각각 지정된 Mock/Remote 이름으로 교체된다.
7. Remote 다중 삭제에서 앞/중간/뒤 ID 실패가 있어도 모든 ID가 시도되고, 성공 ID만 캐시 삭제되며, 성공이 하나 이상이면 change notifier가 한 번 호출된다.
8. Collection 전체 성공은 실제 삭제 개수 toast 후 selection을 종료한다.
9. Collection 부분 성공은 실패 ID만 선택 상태로 남기고 부분 실패 toast를 표시한다.
10. Collection 전체 실패 및 repository failure는 selection을 유지하고 실패 toast를 표시하며 재시도할 수 있다.
11. 빈 입력 삭제, cancellation 재throw, Mock 파일 cleanup best-effort 동작을 회귀 테스트한다.
12. Hilt graph를 포함한 Debug build와 관련 unit test가 통과한다.
13. 신규 production dependency를 추가하지 않는다.

## Validation

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :feature:collection:testDebugUnitTest :feature:developer:testDebugUnitTest :feature:home:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

추가 정적 확인:

```powershell
rg -n "\b(AnalysisDataSourceMode|analysisDataSourceMode|getAnalysisDataSourceMode|setAnalysisDataSourceMode|LocalHomeRepository|LocalRecentCapturesRepository|LocalStorageRepository|LocalCaptureCommandRepository|CaptureThumbnailCache)\b" app core feature docs --glob "*.kt" --glob "*.md" --glob "!docs/handoff/**"
```

위 `rg`는 결과가 없어야 한다.

## Out of scope

- Home/Storage 조회의 Empty/Error 상태 구분 및 retry UI
- Capture 상세 화면의 Mock/Remote repository 전환, 상세 수정/삭제/즐겨찾기 Remote 연결
- Capture upload/organize/poll Remote 구현
- 서버에 없는 최근 정리 전체 목록 API 및 `RemoteRecentCapturesRepository` 실제 구현
- 서버 batch delete API 추가
- Remote 검색 구현
- release effective mode를 `REMOTE`로 전환하는 정책 변경
- Room schema/migration 변경
- Remote 원본 이미지 캐시 추가

## Technical Debt

- none

## Cursor Result

- Changed files: `ScreenshotBackendMode`/`ScreenshotBackendModeStore`/`DataStoreScreenshotBackendModeStore`/`ScreenshotBackendSwitcher`/`MockScreenshotDataResetter`/`ScreenshotBackendModule`, Switching repos, `Mock*`/`RemoteCaptureThumbnailCache` renames, `CaptureDeleteResult` + Remote/Mock partial delete, Developer/Collection UI·VM·strings, docs (`PROJECT`/`ANALYSIS_DATA_SOURCE`/`LOCAL_DATA`), related unit tests + `InMemoryPreferencesDataStore` test double
- Build/test: `.\gradlew.bat :core:data:testDebugUnitTest :feature:collection:testDebugUnitTest :feature:developer:testDebugUnitTest :feature:home:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1` GREEN; `.\gradlew.bat assembleDebug` GREEN; legacy-symbol `rg` (docs/handoff 제외) 0 hits
- Open questions: none

## Codex Review

- Blocking: none
- Nits: none
- Verdict: DONE
