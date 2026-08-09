# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- `user_preferences`는 Preferences DataStore 하나에 온보딩 완료 여부, 세션 토큰, 최근 검색어, 알림 설정, Debug 스크린샷 backend 모드 등을 함께 저장한다.
- 현재 `UserPreferencesDataStoreOwner.kt`의 `ReplaceFileCorruptionHandler`는 손상 파일을 빈 Preferences로 교체한다. 그러나 이 교체가 실제 corruption 복구였다는 사실을 시작 계층에 전달하지 않아 Room DB와 앱 내부 이미지 저장소를 연계 초기화할 수 없다.
- 현재 `PreferencesDataStoreExt.safeData()`는 모든 `IOException`을 빈 Preferences로 바꾼다. 이 때문에 디스크의 일시적인 읽기 실패도 온보딩 미완료/로그아웃처럼 해석되며, corruption과 일반 I/O 실패를 구분할 수 없다.
- AndroidX DataStore의 공식 계약상 corruption handler는 역직렬화 불가능을 뜻하는 `CorruptionException`에만 호출된다. 일반 읽기 `IOException`은 Flow 수집자에게 전달되며, 다시 수집하면 DataStore가 읽기를 다시 시도한다.
- `CorruptionException`은 `IOException`의 하위 타입이지만, corruption handler가 교체에 성공하면 원래 예외는 소비되고 교체된 Preferences가 방출된다. 교체 자체가 실패해 예외가 전달되는 경우에는 DB 초기화를 시작하면 안 된다.
- `LocalAppDataResetter.resetDatabaseAndOnboarding()`는 이미 Room 전체 테이블과 `ScreenshotImageStorage`의 `recap/images`, `recap/thumbnails`, 세션, 최근 검색어, 온보딩 상태를 초기화한다. corruption 복구에서도 이 정책을 재사용한다.
- 앱 진입은 `RecapStartupUiState.Ready`가 될 때까지 차단된다. 복구 및 DataStore 읽기는 Home/Collection prefetch보다 먼저 끝나야 한다.

## Spec

### 1. 실패 분류 정책

- **확정된 corruption**은 `ReplaceFileCorruptionHandler`가 `CorruptionException`을 받은 경우만 의미한다.
- handler는 DataStore API를 다시 호출하지 않는다. 대신 빈 Preferences가 아니라 내부 boolean marker `user_preferences_recovery_required=true`만 포함한 새 Preferences를 반환한다. 이 marker가 DB/이미지 초기화를 허용하는 유일한 조건이다.
- corruption handler가 교체 파일 쓰기에 실패해 예외가 바깥으로 전달되면 일반 시작 읽기 실패로 처리한다. 아직 유효한 replacement와 marker가 영속화되지 않았으므로 DB나 이미지 저장소를 초기화하지 않는다.
- **일시적인 일반 읽기 실패**는 corruption marker 없이 발생한 `IOException` 중 자동 재수집 안에 정상 읽기로 회복된 경우다.
- `safeData()`는 더 이상 `IOException`에서 `emptyPreferences()`를 방출하지 않는다. 일반 `IOException`에 한해서 동일 Flow를 최대 2회 재수집한다(최초 시도 포함 총 3회). 재시도 전 delay는 100ms, 300ms로 고정한다.
- 재시도 도중 정상 Preferences를 읽으면 그대로 방출하고 앱 시작을 계속한다. 비 `IOException`은 즉시 다시 던진다. 코루틴 취소도 삼키지 않는다.
- 자동 재시도 2회가 모두 실패하면 이를 **지속 중인 읽기 오류**로 분류해 마지막 `IOException`을 전달한다. 지속 오류를 corruption으로 승격하거나 로컬 데이터를 자동 삭제하지 않는다. 사용자의 명시적 재시도는 새로운 총 3회 읽기 묶음을 시작한다.
- 각 자동 재시도는 `Timber.w`, 최종 소진은 `Timber.e`로 기록하되 사용자 데이터나 토큰 값은 로그에 남기지 않는다.

### 2. corruption recovery marker와 coordinator

- `:core:data`에 marker key 생성, marker 조회/해제, corruption replacement 생성을 한곳에서 소유하는 작은 내부 컴포넌트를 둔다. handler와 시작 복구 coordinator가 문자열 key를 중복 선언하지 않게 한다.
- `ReplaceFileCorruptionHandler`는 위 컴포넌트의 replacement 생성 함수를 사용한다. callback 안에서는 로깅과 replacement 생성 외에 Room, 파일 저장소, DataStore edit를 호출하지 않는다.
- `:core:data`에 앱 시작 전용 recovery coordinator를 추가한다. 공개 suspend 함수 하나가 다음 순서를 직렬로 수행한다.
  1. 재시도 정책이 적용된 DataStore를 한 번 읽는다.
  2. marker가 없으면 아무 데이터도 삭제하지 않고 성공한다.
  3. marker가 있으면 `LocalAppDataResetter.resetDatabaseAndOnboarding()`를 호출한다. 이 호출로 Room, 원본 이미지, 썸네일, 세션, 최근 검색어, 온보딩 상태가 초기화된다.
  4. 전체 초기화가 성공한 뒤에만 DataStore edit로 marker를 제거한다.
- DB/파일/Preferences 초기화 중 예외가 발생하면 marker를 제거하지 않고 예외를 전달한다. 다음 자동 또는 사용자 재시도에서 동일 복구를 다시 수행할 수 있어야 하므로 복구 과정은 멱등이어야 한다.
- `ScreenshotImageStorage.clearStoredImages()`가 `recap/images`와 `recap/thumbnails` 양쪽을 비우는 현재 계약을 유지하며, corruption 복구가 별도의 썸네일 전용 삭제 경로를 만들지 않는다.
- 시작 복구는 `RemoteCaptureThumbnailCache`나 Home/Collection prefetch가 작업을 시작하기 전에 수행한다. 시작 시점에는 다운로드가 없어야 하므로 이 작업에서 cache generation/in-flight 취소 API는 추가하지 않는다.

### 3. 시작 상태 및 재시도

- `RecapStartupViewModel`은 생성 후 `Loading`에서 recovery coordinator를 먼저 실행한다.
- coordinator 성공 후에만 `onboardingCompleted`를 읽어 `Ready`를 방출하고, `true`일 때 기존 Home/Collection prefetch를 수행한다.
- 자동 재시도까지 소진된 DataStore `IOException` 또는 corruption 연계 초기화 실패는 `RecapStartupUiState.ReadError`로 전환한다. raw exception/message는 UiState에 넣지 않는다.
- `retryStartup()` action을 추가한다. `ReadError -> Loading`으로 바꾼 뒤 recovery coordinator와 온보딩 읽기를 처음부터 다시 실행한다. 중복 탭은 실행 중인 시작 job 하나로 합치거나 무시해 동시 복구가 일어나지 않게 한다.
- `completeOnboarding()`과 `resetOnboarding()`의 기존 계약은 유지한다.
- `ReadError`에서는 메인/온보딩 navigation과 prefetch를 시작하지 않는다.

### 4. 시작 오류 UI

- Lottie splash 완료 후 상태가 `ReadError`이면 무한 splash 대신 `RecapBackground` 기반의 빈 시작 surface 위에 `RecapPopup`을 표시한다.
- 팝업에는 기술 세부사항 없이 로컬 데이터를 읽지 못했다는 복구 가능한 안내와 `다시 시도` action 하나만 제공한다. 데이터 초기화나 앱 종료 action은 제공하지 않는다.
- 현재 `RecapPopup`은 confirm/cancel 두 버튼을 필수로 요구하므로, 기존 호출부 호환성을 유지하면서 cancel action이 없는 단일 액션 variant를 지원하도록 `RecapPopup.kt`를 최소 확장한다. 별도 팝업 디자인을 복제하지 않는다.
- 시작 오류 팝업은 `dismissOnBackPress=false`, `dismissOnClickOutside=false`로 설정하고 `onDismissRequest`로 상태를 숨기지 않는다. 시작 오류가 해결되기 전에는 팝업 뒤에 navigation 가능한 화면이 없어야 한다.
- 단일 액션에서는 confirm 버튼이 버튼 영역의 사용 가능한 너비를 차지하고 기본 primary 색상(`RecapBlue300`)을 사용한다. 기존 2버튼 destructive/primary variant의 레이아웃과 기본값은 바꾸지 않는다.
- 문자열은 `core/design/src/main/res/values/strings.xml`에 추가하고 기존 디자인 토큰 및 공통 버튼을 사용한다. 새 `Icons.*`, Canvas/텍스트 아이콘, 새 production dependency를 추가하지 않는다.
- `RecapPopup` 단일 액션 variant에 `RECAPTheme` Preview를 추가한다.
- `canEnterRecapApp()`은 기존처럼 `Ready`만 앱 진입 가능으로 판단한다. `Loading`과 `ReadError`는 모두 navigation 진입을 차단한다.

## Files to Touch

- `core/data/src/main/java/com/chalkak/recap/core/data/UserPreferencesDataStoreOwner.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/PreferencesDataStoreExt.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/UserPreferencesRecoveryMarker.kt` (new; 실제 이름은 역할이 동일하면 조정 가능)
- `core/data/src/main/java/com/chalkak/recap/core/data/StartupDataRecoveryCoordinator.kt` (new; 실제 이름은 역할이 동일하면 조정 가능)
- `core/data/src/main/java/com/chalkak/recap/core/data/LocalAppDataResetter.kt` (필요한 최소 변경만)
- `app/src/main/java/com/chalkak/recap/app/RecapStartupViewModel.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/popup/RecapPopup.kt`
- `core/design/src/main/res/values/strings.xml`
- `core/data/src/test/java/com/chalkak/recap/core/data/PreferencesDataStoreExtTest.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/UserPreferencesCorruptionHandlerTest.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/StartupDataRecoveryCoordinatorTest.kt` (new)
- `app/src/test/java/com/chalkak/recap/app/RecapStartupViewModelTest.kt`
- `app/src/test/java/com/chalkak/recap/app/RecapStartupGateTest.kt`
- 위 변경으로 직접 영향을 받는 기존 테스트 파일만 추가 허용

## Acceptance Criteria

- 정상 DataStore 읽기는 데이터 삭제 없이 기존 온보딩 상태로 `Ready`가 된다.
- 일반 `IOException`이 첫 시도 또는 첫 재시도에서 끝나고 이후 읽기가 성공하면 총 3회 한도 안에서 정상 시작하며 DB, 원본 이미지, 썸네일을 삭제하지 않는다.
- 일반 `IOException`이 총 3회 모두 발생하면 `ReadError`가 되고 DB, 원본 이미지, 썸네일, 세션을 삭제하거나 빈 Preferences를 정상값처럼 방출하지 않는다.
- `ReadError`에서 `다시 시도` 후 DataStore 읽기가 성공하면 `Loading -> Ready`로 회복한다.
- 비 `IOException`과 코루틴 취소는 빈 Preferences로 대체되지 않고 호출자에게 전파된다.
- 실제 Preferences corruption은 handler에 의해 marker가 포함된 유효한 Preferences로 교체된다.
- marker가 있는 시작에서는 앱 navigation/prefetch 전에 Room 전체 테이블, `recap/images`, `recap/thumbnails`, 세션, 최근 검색어를 초기화하고 온보딩을 미완료로 만든다.
- corruption 연계 초기화가 성공한 뒤 marker가 제거되며, 다음 시작에서는 초기화를 반복하지 않는다.
- corruption 연계 초기화가 실패하면 marker가 남고 `ReadError`가 되며, 재시도에서 복구를 다시 수행한다.
- corruption replacement 쓰기 자체가 실패한 경우 DB와 이미지 저장소를 초기화하지 않는다.
- `ReadError` 중에는 Home/Collection prefetch가 호출되지 않는다.
- 시작 오류는 dismiss 불가능한 단일 액션 `RecapPopup`으로 표시되고, raw exception을 노출하지 않으며 `다시 시도` action을 제공한다.
- 기존 2버튼 `RecapPopup` 호출부의 소스 호환성, 기본 색상, 레이아웃 동작이 유지되고 단일 액션 variant의 앱 테마 Preview가 존재한다.
- 새 production dependency가 추가되지 않는다.

## Validation

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

필수 자동 테스트 시나리오:

- `safeData`: 즉시 성공, 1회/2회 `IOException` 후 성공, 총 3회 실패, 비 `IOException` 즉시 전파.
- corruption handler: 손상 파일 교체 결과에 marker가 존재함.
- recovery coordinator: marker 없음은 무삭제, marker 있음은 reset 후 marker 제거, reset 실패는 marker 유지, 다음 호출 성공 시 복구 완료.
- `RecapStartupViewModel`: 정상 `Loading -> Ready`, 지속 I/O 오류 `Loading -> ReadError`, retry 성공, 오류/복구 중 prefetch 없음, 복구 완료 후 온보딩 완료일 때만 prefetch.
- startup gate: `ReadError`는 앱 진입 불가.

## Out of scope

- 일반 `IOException`을 횟수만으로 corruption으로 승격하거나 자동으로 사용자 데이터를 삭제하는 정책.
- 사용자에게 수동 `데이터 초기화` 버튼 제공.
- Room 자체 corruption/migration 실패 복구 정책 변경.
- DataStore 파일을 직접 삭제하거나 권한/디스크 공간을 앱이 수정하는 처리.
- `RemoteCaptureThumbnailCache`의 런타임 in-flight 취소 구조 변경.
- DataStore 분리, Proto DataStore 이전, 다중 DataStore 마이그레이션.
- Crashlytics 이벤트/커스텀 키 추가.

## Technical Debt

- none

## Cursor Result

- Changed files: `core/data/.../UserPreferencesDataStoreOwner.kt`, `core/data/.../PreferencesDataStoreExt.kt`, `core/data/.../UserPreferencesRecoveryMarker.kt` (new), `core/data/.../StartupDataRecoveryCoordinator.kt` (new), `app/.../RecapStartupViewModel.kt`, `app/.../RecapApp.kt`, `core/design/.../popup/RecapPopup.kt`, `core/design/.../values/strings.xml`, `core/data/.../PreferencesDataStoreExtTest.kt`, `core/data/.../UserPreferencesCorruptionHandlerTest.kt`, `core/data/.../StartupDataRecoveryCoordinatorTest.kt` (new), `app/.../RecapStartupViewModelTest.kt`, `app/.../RecapStartupGateTest.kt`
- Build/test: `:core:data:testDebugUnitTest :app:testDebugUnitTest` GREEN; `assembleDebug` GREEN
- Open questions: none
- Notes: Windows JVM unit tests cannot reliably exercise Preference DataStore file rename after corruption (`Unable to rename ...tmp`); covered handler contract via `ReplaceFileCorruptionHandler.handleCorruption` + marker file round-trip instead of end-to-end corrupt-file replacement.

## Codex Review

- Blocking: none
- Nits: Windows JVM에서는 Preferences DataStore의 실제 corrupt-file replacement rename 경로를 안정적으로 재현하지 못해, 설정된 handler wiring은 정적 검토하고 `ReplaceFileCorruptionHandler.handleCorruption` 계약 및 marker 파일 round-trip 테스트로 보완했다.
- Verdict: DONE
