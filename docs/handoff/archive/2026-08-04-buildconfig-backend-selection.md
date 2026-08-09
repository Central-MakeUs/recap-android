# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Cursor

## Context

- 현재 스크린샷 도메인은 `ScreenshotBackendModeStore`의 DataStore 값을 매 요청/Flow마다 읽는 `Switching*Repository`로 Mock/Remote 구현을 런타임 선택한다.
- 개발자 옵션의 backend 전환 UI는 `ScreenshotBackendSwitcher`를 통해 Mock 데이터 초기화와 mode 저장을 수행한다.
- 이번 작업은 이 런타임 전환 계층을 제거하고, `:core:data`의 `BuildConfig.USE_MOCK_BACKEND` 하나로 프로세스 수명 동안 사용할 backend를 고정한다.
- debug 빌드도 Mock으로 강제하지 않는다. 기본값은 debug `true`, qa/release `false`이지만 Gradle project property `USE_MOCK_BACKEND`로 명시적으로 덮어쓸 수 있어야 한다.
- 현재 `UserRepository`는 backend 선택 대상인 data-summary/consent/account-data와 항상 Remote인 auth(`getAccountInfo`, `withdraw`)를 한 interface에 함께 둔다. Mock 선택에서도 auth의 기존 Remote 동작을 보존해야 한다.

## Spec

### 1. BuildConfig 기반 backend 설정

- `core/data/build.gradle.kts`에 boolean `BuildConfig.USE_MOCK_BACKEND`를 생성한다.
- Gradle project property `USE_MOCK_BACKEND`가 있으면 debug/qa/release 모두 그 값을 사용한다.
  - 예: `./gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=false`는 debug Remote 빌드다.
  - 예: `./gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=true`는 debug Mock 빌드다.
- property가 없을 때 기본값은 다음과 같다.
  - debug: `true`
  - release: `false`
  - app의 qa는 `:core:data` release fallback을 사용하므로 `false`
- property 값은 `true` 또는 `false`만 허용한다. 다른 문자열은 조용히 fallback하지 말고 Gradle configuration 단계에서 이해 가능한 오류로 실패시킨다.
- app과 core:data에 서로 다른 backend 상수를 중복 정의하지 않는다. repository 선택과 backend 관측성 값의 단일 기준은 `com.chalkak.recap.core.data.BuildConfig.USE_MOCK_BACKEND`다.

### 2. 런타임 선택 계층 제거 및 Hilt 직접 선택

- 아래 런타임 mode 구성요소를 production code에서 제거한다.
  - `ScreenshotBackendMode`
  - `ScreenshotBackendModeStore`
  - `DataStoreScreenshotBackendModeStore`
  - `ScreenshotBackendSwitcher`와 `ScreenshotBackendSwitchResult`
  - `ScreenshotBackendModule`의 mode-store binding(모듈에 다른 역할이 없으면 파일 자체 삭제)
- 아래 `Switching*Repository`를 제거한다.
  - `SwitchingScreenshotAnalysisRepository`
  - `SwitchingHomeRepository`
  - `SwitchingRecentCapturesRepository`
  - `SwitchingStorageRepository`
  - `SwitchingCaptureMutationRepository`
  - `SwitchingSearchRepository`
  - `SwitchingScreenshotDetailRepository`
  - `SwitchingUserRepository`
- 각 repository Hilt 모듈은 interface를 Switching 구현에 `@Binds`하지 않고, `BuildConfig.USE_MOCK_BACKEND`에 따라 Mock 또는 Remote concrete 구현을 한 번 선택해 제공한다.
- 선택되지 않은 구현을 graph 생성 시 불필요하게 인스턴스화하지 않도록 `Provider<Mock...>` / `Provider<Remote...>` 또는 동등한 lazy resolution을 사용한다.
- feature/ViewModel 호출부는 기존 repository interface만 의존하며 BuildConfig나 Mock/Remote concrete type을 알지 않는다.
- repository method 실행 중 mode를 재조회하거나 Flow를 `flatMapLatest`로 다른 backend에 갈아끼우는 동작은 남기지 않는다.
- 새로운 production dependency는 추가하지 않는다.

### 3. UserRepository의 Remote auth 예외 보존

- Mock backend 선택 시 data-summary, consent, account-data 삭제는 `MockUserRepository` 동작을 사용한다.
- backend와 무관하게 `getAccountInfo()`와 `withdraw()`는 기존처럼 `RemoteUserRepository`로 위임한다.
- 이를 위해 `MockUserRepository`가 Remote auth delegate를 주입받아 두 auth method만 위임하도록 정리하고, Mock 선택 시 `UserRepository`를 `MockUserRepository`에 직접 제공한다. 별도의 runtime switching wrapper를 새로 만들지 않는다.
- Remote backend 선택 시 `UserRepository`는 `RemoteUserRepository`를 직접 사용한다.
- Mock/Remote 각각의 refresh, consent, delete 동작은 현재 선택된 구현만 실행한다. 기존 `SwitchingUserRepository.refresh*()`처럼 양쪽 구현을 동시에 호출하지 않는다.

### 4. 개발자 옵션 정리

- 개발자 옵션에서 현재 backend 표시, Mock/Remote 전환 버튼, 확인 popup, busy/success/failure feedback을 제거한다.
- 관련 `DeveloperOptionsUiState` 필드와 `DeveloperOptionAction`, ViewModel 의존성 및 전환 로직을 제거한다.
- backend 전환과 무관한 기존 개발자 기능은 유지한다.
  - Component Garden
  - onboarding reset
  - 스크린샷 데이터 초기화
  - test crash
- 스크린샷 데이터 초기화는 기존 `MockScreenshotDataResetter`를 계속 사용한다. BuildConfig가 Remote여도 이 개발자 액션이 서버 데이터를 삭제하는 의미로 바뀌어서는 안 된다.
- 제거된 UI에만 쓰이던 string resource와 Preview/test case를 함께 정리하고, 남은 화면 Preview는 `RECAPTheme`를 유지한다.

### 5. 관측성 및 문서 동기화

- `ObservabilityBootstrap`은 mode Flow를 관찰하지 않는다. `backend_mode` custom key는 `BuildConfig.USE_MOCK_BACKEND`에서 계산한 고정 문자열 `mock` 또는 `remote`를 설정한다.
- onboarding/login Flow 관찰과 기존 observability key 동작은 유지한다.
- `docs/PROJECT.md`, `docs/ANALYSIS_DATA_SOURCE.md`, `docs/LOCAL_DATA.md`를 새 build-time 고정 선택 구조에 맞춘다.
  - DataStore backend mode/key, runtime switching, 전환 시 reset 정책 설명을 제거한다.
  - debug에서도 Gradle property로 Remote 선택이 가능함을 명시한다.
  - Mock/Remote 저장 SoT와 `MockScreenshotDataResetter` 자체 설명은 유지한다.
- 완료된 과거 기록인 `docs/BACKLOG.md`와 archive handoff는 역사 보존을 위해 수정하지 않는다.

## Files to Touch

- `core/data/build.gradle.kts`
- `core/data/src/main/java/com/chalkak/recap/core/data/**/**Module.kt` 중 repository binding 모듈
  - `screenshot/analysis/ScreenshotAnalysisModule.kt`
  - `home/HomeModule.kt`
  - `storage/StorageModule.kt`
  - `capture/CaptureMutationModule.kt`
  - `search/SearchModule.kt`
  - `screenshot/persistence/ScreenshotDetailModule.kt`
  - `user/UserModule.kt`
- 삭제: `core/data/src/main/java/com/chalkak/recap/core/data/**/Switching*Repository.kt`
- 삭제: `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/backend/{ScreenshotBackendMode.kt,ScreenshotBackendModeStore.kt,DataStoreScreenshotBackendModeStore.kt,ScreenshotBackendSwitcher.kt,ScreenshotBackendModule.kt}`
- `core/data/src/main/java/com/chalkak/recap/core/data/user/MockUserRepository.kt`
- 삭제 또는 대체: `core/data/src/test/java/com/chalkak/recap/core/data/**/Switching*RepositoryTest.kt`
- 삭제: `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/backend/{ScreenshotBackendModeStoreTest.kt,ScreenshotBackendSwitcherTest.kt}`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperViewModel.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperOptionsScreen.kt`
- `feature/developer/src/test/java/com/chalkak/recap/feature/developer/DeveloperViewModelTest.kt`
- `core/design/src/main/res/values/strings.xml`
- `app/src/main/java/com/chalkak/recap/app/observability/ObservabilityBootstrap.kt`
- 관련 observability test가 있거나 새 고정 backend 값 검증이 필요하면 `app/src/test/...`
- `docs/PROJECT.md`
- `docs/ANALYSIS_DATA_SOURCE.md`
- `docs/LOCAL_DATA.md`

## Acceptance Criteria

- [ ] Gradle property가 없으면 debug의 `BuildConfig.USE_MOCK_BACKEND == true`, qa/release의 값은 `false`다.
- [ ] `-PUSE_MOCK_BACKEND=false` debug 빌드가 Remote repository들을 제공하고, `-PUSE_MOCK_BACKEND=true` debug 빌드가 Mock repository들을 제공한다.
- [ ] 잘못된 property 값은 Gradle configuration 오류로 실패한다.
- [ ] 8개 repository interface(analysis, home, recent captures, storage, capture mutation, search, screenshot detail, user)가 mode store 없이 BuildConfig에 맞는 concrete 구현을 직접 받는다.
- [ ] 선택되지 않은 Mock/Remote repository는 단순 provider 선택 때문에 eager 생성되지 않는다.
- [ ] production code와 활성 테스트에 `ScreenshotBackendMode*`, `ScreenshotBackendSwitcher`, `Switching*Repository`, `screenshot_backend_mode`, `analysis_data_source_mode` 참조가 남지 않는다.
- [ ] Mock backend에서도 계정 조회와 탈퇴는 Remote 경로를 사용하고, 나머지 User data/consent/delete 기능은 Mock 경로를 사용한다.
- [ ] Remote backend에서는 UserRepository 전체가 Remote 경로를 사용한다.
- [ ] 개발자 옵션에서 런타임 backend 표시/전환 UI가 사라지고 다른 개발자 기능은 유지된다.
- [ ] `backend_mode` observability key는 선택된 BuildConfig에 맞춰 `mock` 또는 `remote`로 설정된다.
- [ ] 관련 switching/mode/switcher 테스트는 삭제하고, 변경된 DI 선택 및 Mock auth 위임에 필요한 회귀 테스트를 추가하거나 기존 테스트를 조정한다.
- [ ] 프로젝트 문서가 런타임 전환 구조를 현재 사실로 설명하지 않는다.
- [ ] 요청과 무관한 production 동작, API 계약, 화면은 변경하지 않는다.

## Validation

PowerShell에서 다음을 실행한다.

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :feature:developer:testDebugUnitTest :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

debug Mock 기본/명시 빌드:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=true
```

동일 debug 변형의 Remote 빌드:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug -PUSE_MOCK_BACKEND=false
```

qa 기본 Remote 빌드:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :app:assembleQa
```

정적 잔존 참조 확인:

```powershell
rg -n "ScreenshotBackendMode|ScreenshotBackendSwitcher|Switching[A-Za-z]*Repository|screenshot_backend_mode|analysis_data_source_mode" core app feature docs/PROJECT.md docs/ANALYSIS_DATA_SOURCE.md docs/LOCAL_DATA.md -g "!**/build/**"
```

- 마지막 `rg`는 production code/활성 테스트/현재 사실 문서에서 결과가 없어야 한다. 과거 기록인 `docs/handoff/archive`와 `docs/BACKLOG.md`는 검사 범위에서 제외한다.
- 잘못된 값 검증으로 `assembleDebug -PUSE_MOCK_BACKEND=invalid`가 configuration 단계에서 명확히 실패하는지 확인한다. 이 명령의 실패가 기대 결과임을 Cursor Result에 기록한다.

## Out of scope

- Mock/Remote repository 내부 비즈니스 로직 또는 API 계약 변경
- Remote endpoint, Room schema, DataStore의 backend mode 외 사용자 preference 변경
- `MockScreenshotDataResetter`의 삭제 범위 변경
- instrumentation용 Hilt replacement 인프라 추가
- build flavor 신설 또는 기존 debug/qa/release 구조 개편
- 과거 archive handoff 및 완료된 backlog 기록 재작성
- 현재 기술부채 목록의 정리/리팩터링

## Technical Debt

- 새 공유 기술부채 없음. BACKLOG.md 미갱신.

## Cursor Result

- Changed files: `core/data/build.gradle.kts`; Hilt modules (`ScreenshotAnalysisModule`, `HomeModule`, `StorageModule`, `CaptureMutationModule`, `SearchModule`, `ScreenshotDetailModule`, `UserModule`); `MockUserRepository.kt` + `MockUserRepositoryTest.kt`; deleted Switching* repos/tests and backend mode/store/switcher/module/tests; `DeveloperViewModel.kt`, `DeveloperOptionsScreen.kt`, `DeveloperViewModelTest.kt`; `strings.xml`; `ObservabilityBootstrap.kt`; `docs/PROJECT.md`, `docs/ANALYSIS_DATA_SOURCE.md`, `docs/LOCAL_DATA.md`
- Build/test: `:core:data:testDebugUnitTest :feature:developer:testDebugUnitTest :app:testDebugUnitTest` GREEN; `assembleDebug -PUSE_MOCK_BACKEND=true` GREEN; `assembleDebug -PUSE_MOCK_BACKEND=false` GREEN; `:app:assembleQa` GREEN; `assembleDebug -PUSE_MOCK_BACKEND=invalid` FAILED as expected (`Invalid USE_MOCK_BACKEND='invalid'`); residual-reference rg clean; default BuildConfig debug=`true` / release=`false`
- Open questions: none

## Codex Review

- Blocking: none
- Fixes applied by Codex:
  - debug Remote에서도 실패하던 고정 `BuildConfig.USE_MOCK_BACKEND == true` 테스트를 제거했다.
  - 실제 Gradle 설정과 연결되지 않고 로직을 복제하던 `UseMockBackendProperty` production helper와 테스트를 제거했다.
  - 삭제한 helper의 문서 파일 목록 참조를 제거했다.
- Nits:
  - `docs/MOCK_REMOTE_CHANGE.md`는 handoff 범위 밖에서 추가됐지만 historical 상태가 명확하고 현재 사실 문서와 구분되어 있어 보존을 허용한다.
- Validation:
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest -PUSE_MOCK_BACKEND=false --no-daemon --no-configuration-cache --max-workers=1`: GREEN
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1`: GREEN
  - `git diff --check`: GREEN (line-ending warnings only)
  - 제거 대상 및 review helper 잔존 참조 검사: GREEN
  - Cursor 검증: 전체 관련 unit test GREEN, debug Mock/Remote assemble GREEN, qa assemble GREEN, invalid property expected failure
- Verdict: DONE

