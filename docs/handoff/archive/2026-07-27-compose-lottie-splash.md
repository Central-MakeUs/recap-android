# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Cursor

## Context

- RECAP은 minSdk 30, Jetpack Compose 기반이며 `MainActivity`에서
  `androidx.core.splashscreen.SplashScreen.installSplashScreen()`을 사용한다.
- 현재 `Theme.RECAP.Starting`은 흰 배경과 `@mipmap/recap_logo`를 사용하는 정적 시스템
  스플래시다.
- 현재 `MainActivity`는 `RecapStartupUiState.Loading` 동안 시스템 스플래시를 유지하고,
  `RecapApp`은 같은 상태에서 아무 Compose UI도 그리지 않는다.
- Android 시스템 스플래시는 Lottie JSON을 직접 렌더링할 수 없고 API 30의 compat
  시작 화면은 animated vector drawable도 재생하지 못한다. 따라서 모든 지원 API에서
  동일한 애니메이션을 제공하려면 시스템 스플래시 다음 첫 Compose 화면에서 Lottie를
  재생해야 한다.
- Version Catalog에는 `com.airbnb.android:lottie-compose:6.7.1`이
  `libs.lottie.compose`로 이미 등록되어 있지만 `:app` 의존성에는 연결되지 않았다.
- 현재 worktree의 `core/design` 앱 아이콘 및 mipmap 변경은 사용자의 별도 변경이다.
  이 작업에서 수정하거나 되돌리지 않는다.
- 사용자가 승인한 `app/src/main/res/raw/recap_splash.json`이 제공되었다. JSON 파싱
  확인 결과 375×812, 30fps, 약 2.93초이며 이미지 asset 3개는 모두 base64로 내부에
  포함되어 있어 별도 이미지 파일이나 네트워크 로딩이 필요하지 않다.

## Spec

1. 시스템 스플래시는 현재의 정적 구성과 로고를 그대로 유지한다.
   - `Theme.RECAP.Starting`의 흰 배경, `@mipmap/recap_logo`,
     `postSplashScreenTheme` 설정을 변경하지 않는다.
   - API별 drawable/AVD 분기를 새로 만들지 않는다.
2. `:app`에 기존 Version Catalog alias인 `libs.lottie.compose`를 production
   dependency로 추가한다.
   - 새 버전이나 별도 Lottie dependency alias를 추가하지 않는다.
   - 이 의존성은 사용자가 요청한 Lottie JSON의 Compose 렌더링에 직접 필요하다.
3. 사용자가 제공한 기존 로컬 Lottie JSON
   `app/src/main/res/raw/recap_splash.json`을 사용한다.
   - 원본 애니메이션의 그래픽이나 타이밍을 구현자가 임의로 수정하지 않는다.
   - 네트워크 URL이나 런타임 다운로드를 사용하지 않는다.
4. app 모듈에 전용 `RecapLottieSplashScreen` composable을 새 파일로 만든다.
   - `RECAPTheme` 내부에서 전체 화면을 채우고 중앙에 Lottie를 표시한다.
   - 배경은 시스템 스플래시와 시각적으로 이어지도록 기존 디자인 토큰 `White`를 사용한다.
   - 애니메이션은 `R.raw.recap_splash`에서 로드하고 정확히 1회 재생한다.
   - 애니메이션은 비율을 유지하며 화면에 맞추고 임의 crop하지 않는다.
   - 장식 목적이므로 접근성 content description을 노출하지 않는다.
   - composition 로드 또는 파싱이 실패하면 진입을 영구 차단하지 않고 애니메이션 완료로
     취급한다. raw exception message를 사용자에게 표시하지 않는다.
   - 화면 composable Preview를 추가하고 `RECAPTheme`로 감싼다. Preview에서 Lottie
     런타임 재생이 불가능하면 동일한 흰 배경과 배치 영역을 확인할 수 있는 정적 preview
     상태를 제공한다.
5. 앱 진입 gate는 다음 두 조건을 모두 만족할 때 열린다.
   - Lottie 1회 재생 완료 또는 composition 로드 실패
   - `RecapStartupUiState.Ready`
   조건을 별도 테스트 가능한 순수 함수 또는 불변 상태 모델로 표현해 Compose 분기에서
   사용한다. 먼저 완료된 조건은 다른 조건이 완료될 때까지 유지한다.
6. `MainActivity`는 `installSplashScreen()` 호출을 `super.onCreate()` 전에 유지하되,
   `RecapStartupUiState.Loading`에 연결된 `setKeepOnScreenCondition`은 제거한다.
   시스템 스플래시를 startup 전체 동안 붙잡지 않고 첫 Compose Lottie 화면으로
   자연스럽게 넘긴다.
7. `RecapApp`은 루트에서 Lottie 완료 여부를 `rememberSaveable`로 보관한다.
   - Lottie gate가 닫혀 있거나 startup 상태가 Loading이면
     `RecapLottieSplashScreen`만 표시한다.
   - 두 조건이 모두 완료되면 기존 Onboarding/Main/Developer navigation과 toast UI를
     지금과 동일하게 구성한다.
   - configuration change로 Activity가 재생성될 때 이미 완료된 Lottie를 다시 재생하지
     않는다.
   - 새 앱 실행으로 새 Activity 상태가 시작되면 Lottie를 1회 재생한다.
8. startup 결과에 따른 기존 초기 route 동작을 보존한다.
   - onboarding 미완료: Lottie 이후 Onboarding
   - onboarding 완료: Lottie 이후 Main
   - pending organize/home navigation, toast, analysis progress 동작을 변경하지 않는다.
9. 애니메이션 완료와 startup Ready가 같은 프레임 또는 서로 다른 순서로 도착해도
   navigation root가 중복 초기화되거나 중간 빈 화면이 노출되지 않게 한다.

## Files to Touch

- `gradle/libs.versions.toml` (읽기만; 기존 `libs.lottie.compose` alias 재사용)
- `app/build.gradle.kts`
- `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapLottieSplashScreen.kt` (new)
- `app/src/main/java/com/chalkak/recap/app/RecapStartupGate.kt` 또는 동등한 좁은 이름의
  순수 상태 파일 (new, 필요 시)
- `app/src/main/res/raw/recap_splash.json` (읽기만; 기존 사용자 제공 자산)
- `app/src/test/java/com/chalkak/recap/app/RecapStartupGateTest.kt` (new)

## Acceptance Criteria

- API 30 이상에서 앱 cold start 시 현재 정적 시스템 로고가 먼저 표시되고, 이어서 동일한
  Compose Lottie 애니메이션이 정확히 1회 재생된다.
- 시스템 스플래시에는 Lottie JSON이나 API별 AVD 구현을 넣지 않는다.
- startup이 Lottie보다 먼저 Ready가 되어도 애니메이션 1회가 끝나기 전 실제 앱 화면을
  표시하지 않는다.
- Lottie가 먼저 끝나도 startup이 Ready가 되기 전 실제 앱 화면이나 빈 화면을 표시하지
  않고 마지막 Lottie 화면을 유지한다.
- Lottie composition 로드/파싱 실패 시 startup이 Ready가 된 뒤 기존 초기 route로
  진입하며 무한 스플래시에 머물지 않는다.
- onboarding 완료 여부에 따른 기존 Onboarding/Main 초기 route가 유지된다.
- configuration change 후 완료된 Lottie가 재생되지 않고 기존 앱 상태로 복귀한다.
- 새로 추가한 startup gate 단위 테스트가 최소 다음 조합을 검증한다.
  - animation incomplete + startup Loading → blocked
  - animation complete + startup Loading → blocked
  - animation incomplete + startup Ready → blocked
  - animation complete/failure + startup Ready → enter
- `RecapLottieSplashScreen` Preview가 `RECAPTheme`로 감싸져 빌드된다.
- 현재 worktree의 앱 아이콘/mipmap 변경 내용과 파일은 수정하거나 되돌리지 않는다.
- debug build와 app 로컬 단위 테스트가 통과한다.

## Validation

1. `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :app:testDebugUnitTest`
2. `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
3. 정적 확인:
   - `MainActivity`가 `installSplashScreen()`은 유지하고 startup 기반
     `setKeepOnScreenCondition`은 사용하지 않는지 확인한다.
   - `Theme.RECAP.Starting`과 기존 앱 아이콘/mipmap diff가 변경되지 않았는지 확인한다.
4. 런타임 확인은 사용자가 별도로 mobile-mcp/ADB 검증을 요청하지 않았으므로 필수로
   요구하지 않는다. 가능한 경우 API 30 및 API 31+ 에뮬레이터에서 cold start를 수동
   확인하고, 수행했다면 Cursor Result에 기기/API와 관찰 결과를 기록한다.

## Out of scope

- 현재 diff에 포함된 앱 아이콘, adaptive icon, Play Store icon 수정 또는 되돌리기
- Lottie 애니메이션의 신규 디자인/제작, 외부 자산 검색 또는 라이선스 판단
- Lottie JSON을 AnimatedVectorDrawable로 변환하거나 API별 시스템 스플래시 애니메이션
  추가
- 시스템 스플래시 로고/배경/exit animation 변경
- onboarding, root navigation, toast, organize, screenshot analysis 동작 변경
- 원격 Lottie 다운로드, 캐싱 또는 네트워크 fallback
- 시작 성능 최적화, Baseline Profile 또는 Macrobenchmark 추가

## Technical Debt

- None.

## Cursor Result

- Changed files: `app/build.gradle.kts`, `app/src/main/java/com/chalkak/recap/MainActivity.kt`, `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`, `app/src/main/java/com/chalkak/recap/app/RecapLottieSplashScreen.kt` (new), `app/src/main/java/com/chalkak/recap/app/RecapStartupGate.kt` (new), `app/src/test/java/com/chalkak/recap/app/RecapStartupGateTest.kt` (new)
- Build/test: `.\gradlew.bat :app:testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN
- Open questions: none
## Codex Review

- Blocking: none
- Nits: none.
- Verdict: DONE
- Validation: `.\gradlew.bat :app:testDebugUnitTest` GREEN (including 5
  `RecapStartupGateTest` cases), `.\gradlew.bat assembleDebug` GREEN,
  `git diff --check` GREEN
- Static review: `installSplashScreen()` remains before `super.onCreate()`,
  startup-based `setKeepOnScreenCondition` is removed, `Theme.RECAP.Starting` is
  unchanged, and the pre-existing app icon/mipmap changes were left untouched.
