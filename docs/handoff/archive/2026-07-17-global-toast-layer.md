# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- `RecapToastHost`와 `RecapToastHostState`가 `RecapMainScreen`, 온보딩, 스크린샷 상세, 최근 정리 화면, 데이터 관리, Component Garden 등 각 화면 composition 안에서 개별 생성되고 있다.
- 현재 `RecapToastHostState.showToast()`는 호출한 coroutine에서 표시 시간만큼 `delay`한 뒤 `finally`에서 현재 toast를 제거한다. 화면의 `LaunchedEffect`가 navigation으로 취소되면 toast도 함께 제거되므로 화면 전환 후 남은 표시 시간이 유지되지 않는다.
- 스크린샷 삭제 성공은 이 제약을 피하기 위해 `RecapNavHost`의 `pendingMainScreenshotDeletedToast` / `pendingRecentScreenshotDeletedToast`를 거쳐 이전 화면에서 toast를 새로 표시한다.
- 목표는 Android 시스템 toast처럼 앱의 root route(`Onboarding`, `Main`, `Developer`)와 Main 내부 route가 바뀌어도 동일 toast가 남은 시간 동안 계속 보이는 앱 범위 단일 toast layer다.
- 현재 Haze 버전은 `2.0.0-alpha03`이다. 전역 toast의 blur source는 root `NavDisplay` 콘텐츠이며, 실제 `hazeEffect` 출력 영역은 보이는 toast bounds로 제한한다.

## Spec

### 1. 앱 범위 Toast Host

- `RecapApp`의 `RECAPTheme` 내부에 root `NavDisplay`와 형제인 단일 `RecapToastHost` overlay를 둔다.
- `MainActivity`가 별도의 Activity 범위 `RecapToastViewModel`을 생성해 `RecapApp`에 전달한다. `RecapStartupViewModel`에 toast 책임을 합치지 않는다.
- `RecapToastViewModel`은 Activity configuration change 동안 유지되지만 application singleton이나 process-wide owner가 아니다.
- feature가 app 모듈을 의존하지 않도록 toast 요청 API와 제공 수단은 `:core:design`에 둔다.
- feature route는 앱 범위 `RecapToastDispatcher`를 `CompositionLocal`로 얻어 요청한다. UI `Screen` composable은 toast host/state를 파라미터로 받거나 직접 렌더링하지 않는다.
- provider 밖에서 잘못 호출되는 경우를 조용한 no-op으로 숨기지 않는다. Preview가 provider를 요구하게 된다면 Preview에서 명시적으로 test state/provider를 공급한다.

### 2. 요청과 표시 lifecycle 분리

- feature에서 호출하는 toast 요청 API는 요청자의 coroutine이 표시 시간 동안 suspend되지 않는 enqueue 방식이어야 한다. 호출부 API 이름은 기존과 같은 `showToast`를 유지해도 되지만 동작은 즉시 enqueue 후 반환해야 한다.
- 표시 시간, 현재 toast 변경, 자동 제거, 다중 요청 순차 처리는 Activity 범위 `RecapToastViewModel`의 `viewModelScope`가 소유한다. `RecapToastHost`는 현재 presentation을 렌더링하는 UI 역할만 담당한다.
- 요청한 route의 `LaunchedEffect`가 navigation으로 취소되어도 이미 enqueue된 현재 toast는 제거되지 않고 남은 duration을 채운다.
- 여러 요청은 FIFO로 한 개씩 표시하며 동일 메시지도 제거하지 않는다. queue 개수에 인위적인 상한이나 drop 정책을 두지 않는다.
- 각 toast는 기존 `Short(2,000ms)` / `Long(3,500ms)`를 기본 duration으로 사용한다. 현재 toast의 200ms exit animation이 완료된 다음 FIFO의 다음 toast를 표시한다.
- 현재 toast뿐 아니라 대기 중인 toast도 요청한 screen/root route를 떠난 뒤 계속 처리한다. `Onboarding`, `Main`, `Developer` 사이 전환에서도 queue를 비우지 않는다.
- 앱이 background로 이동해도 현재 duration과 FIFO 처리는 계속 진행한다. 복귀 시 이미 만료된 toast를 다시 표시하지 않는다.
- Activity configuration change 동안 ViewModel의 현재 presentation, 남은 시간, FIFO queue 처리는 계속 진행한다. 새 composition에서는 현재 메시지를 다시 렌더링하되 duration을 처음부터 재시작하지 않는다.
- Activity 재생성 중에도 시간과 queue는 멈추지 않는다. 재생성 사이에 만료된 toast는 새 화면에서 보이지 않을 수 있다.
- process death 후에는 current/queue를 복원하지 않는다. `SavedStateHandle`, `rememberSaveable` 또는 영속 저장소에 toast queue를 저장하지 않는다.
- queue 처리 구현은 앱 singleton, application-scope coroutine 또는 기존 feature ViewModel 간 전역 이벤트 버스를 만들지 않는다.

### 3. 접근성 및 사용자 조작

- Toast가 나타날 때 TalkBack이 메시지를 읽을 수 있도록 host/content에 `LiveRegionMode.Polite` semantics를 적용한다.
- 접근성 서비스가 권장하는 timeout을 app UI 경계에서 계산해 queue에 effective duration으로 전달한다. Toast는 text와 icon을 포함하고 control은 포함하지 않는 것으로 계산한다.
- 접근성 timeout 연장이 없는 일반 환경에서는 기존 `Short(2,000ms)` / `Long(3,500ms)`를 그대로 사용한다.
- Toast 탭 또는 스와이프 dismiss 기능은 추가하지 않는다.

### 4. Haze 범위와 성능 정책

- `RecapApp`에서 공유 `HazeState`를 만들고 root `NavDisplay` 전체에 `hazeSource`를 한 번 적용한다.
- `hazeSource`를 toast 표시 여부에 따라 동적으로 붙였다 떼지 않는다. 표시 시작 시 graphics layer 생성/등록이 몰리지 않게 source는 root content에 안정적으로 유지한다.
- `hazeEffect`와 blur 작업은 `AnimatedVisibility`의 실제 toast content에만 존재하게 하여 effect bounds를 toast 영역으로 제한한다.
- 전역 toast blur에는 Haze 2.x의 `HazeInputScale.Auto`를 적용해 기본적인 downscale 최적화를 사용한다. 기존 blur radius, tint, noise, shape 등 시각 토큰은 변경하지 않는다.
- 화면별 toast를 위해서만 존재하던 `HazeState` / `hazeSource`는 제거한다. bottom bar나 다른 glass UI가 사용하는 기존 Haze는 제거하지 않는다.
- 이번 작업에서 별도 성능 벤치마크는 추가하지 않는다. 이후 실제 성능 문제가 측정된 경우에만 blur 제거나 더 낮은 input scale을 별도 검토한다.

### 5. 기존 호출부 마이그레이션

- 다음 production toast 요청을 모두 앱 범위 dispatcher/state로 전환한다.
  - 온보딩 로그인 실패/취소
  - 컬렉션 삭제 성공
  - 데이터 관리 삭제 성공
  - 스크린샷 즐겨찾기 추가/해제
  - 스크린샷 삭제 성공
  - Component Garden의 동적 success/error toast 버튼
- `OnboardingScreen`, `DataManagementScreen`, `ScreenshotRoute`, `RecentOrganizedScreenshotsRoute`, `RecapMainScreen`, `ComponentGardenScreen`의 화면별 `RecapToastHost`를 제거한다.
- `CollectionRoute`에 전달하던 `RecapToastHostState` 파라미터와 `RecapMainTabNavHost`의 전달 경로를 제거한다.
- Component Garden에 정적으로 나열된 `RecapToast` variant 샘플과 `RecapToast.kt` Preview는 전역 Host와 별개인 컴포넌트 예시이므로 유지한다.

### 6. 스크린샷 삭제 흐름 정리

- `ScreenshotEvent.DeleteSucceeded` 처리 시 삭제 성공 toast를 앱 범위 queue에 먼저 enqueue한 다음 기존 `onDeleteSucceeded()` navigation callback을 호출한다.
- `RecapNavHost`의 `pendingMainScreenshotDeletedToast`, `pendingRecentScreenshotDeletedToast`와 관련 route/screen 파라미터 및 `LaunchedEffect` 우회 로직을 제거한다.
- 삭제 후 돌아갈 대상이 MainTabs인지 최근 정리 화면인지와 관계없이 동일 toast 인스턴스가 navigation 전후로 이어져야 한다.

### 7. 위치와 window 정책

- bottom bar 유무와 현재 route에 관계없이 모든 화면에서 현재 MainTabs toast와 동일한 고정 하단 위치를 사용한다.
- 기본 bottom offset은 `RecapBottomBarDefaults.Height + RecapBottomBarDefaults.BottomPadding + WindowInsets.navigationBars bottom + 8.dp`다. horizontal padding은 기존 `24.dp`를 유지한다.
- IME가 열려 기본 위치를 가릴 때만 예외로 Toast를 키보드 위로 이동한다. 최종 bottom padding은 기본 offset과 `WindowInsets.ime bottom + 8.dp` 중 큰 값을 사용한다.
- 위치 계산을 위해 feature별 placement state/callback을 추가하지 않는다.
- `RecapApp` root overlay는 root 화면과 동일 composition 안의 overlay(예: Main의 Organize content)보다 위에 표시한다.
- 별도 window/layer인 `Dialog`, `Popup`, `ModalBottomSheet`보다 위에 표시하는 것은 보장하지 않는다. 이들이 toast를 가리고 있어도 duration과 FIFO 처리는 계속 진행한다.

## Files to Touch

- `core/design/src/main/java/com/chalkak/recap/core/design/component/toast/RecapToast.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/toast/` 아래 dispatcher, CompositionLocal, presentation 모델 분리가 필요할 경우 신규 파일
- `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapToastViewModel.kt` (신규)
- `app/src/test/java/com/chalkak/recap/app/RecapToastViewModelTest.kt` (신규)
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- `feature/onboarding/src/main/java/com/chalkak/recap/feature/onboarding/OnboardingRoute.kt`
- `feature/onboarding/src/main/java/com/chalkak/recap/feature/onboarding/OnboardingScreen.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionRoute.kt`
- `feature/settings/src/main/java/com/chalkak/recap/feature/settings/DataManagementRoute.kt`
- `feature/settings/src/main/java/com/chalkak/recap/feature/settings/screen/DataManagementScreen.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotRoute.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/RecentOrganizedScreenshotsRoute.kt`
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/ComponentGardenScreen.kt`

## Acceptance Criteria

- 일반 앱 composition에서 hosted `RecapToastHost`는 `RecapApp`의 한 곳에만 존재한다. Component Garden의 정적 `RecapToast` 샘플과 Preview는 예외다.
- Onboarding에서 표시를 시작한 toast가 Main으로 전환되어도 재시작하거나 사라지지 않고 남은 duration 동안 이어진다.
- Main 내부 route 전환, MainTabs 전환, Screenshot에서 이전 route로 돌아가는 전환, Developer/Onboarding root 전환 중에도 현재 toast와 FIFO queue가 유지된다.
- Screenshot 삭제 성공 toast를 위해 사용하던 pending boolean 전달 로직이 완전히 제거된다.
- 연속 요청과 동일 메시지 중복 요청은 drop/deduplicate 없이 FIFO이며 동시에 두 toast가 겹쳐 보이지 않는다.
- 현재 toast의 exit animation이 완료된 다음 queue의 다음 toast가 표시된다.
- toast 요청 caller가 취소되어도 enqueue가 완료된 toast의 표시 lifecycle에는 영향을 주지 않는다.
- 앱 background에서도 duration/queue가 계속 진행되고 만료된 toast는 복귀 후 재노출되지 않는다.
- configuration change 후에는 동일 current/queue를 유지하되 현재 toast는 남은 시간만 표시한다. Activity 재생성 사이에 만료된 toast는 다시 표시하지 않는다.
- process death 후에는 current/queue가 복원되지 않는다.
- TalkBack은 새 toast 메시지를 polite live region으로 안내하며, 접근성 권장 timeout이 기본 duration보다 길면 해당 effective duration을 사용한다.
- Toast에 탭/스와이프 dismiss 동작이 추가되지 않는다.
- toast가 보이지 않을 때 toast `hazeEffect`는 composition/draw 대상에 존재하지 않는다.
- root `hazeSource`는 표시 중에 동적으로 재부착되지 않으며 toast effect에 `HazeInputScale.Auto`가 적용된다.
- 기존 toast 문구, type, 일반 환경의 기본 duration, blur/tint/noise/shape, enter/exit animation은 유지된다. 접근성 권장 timeout 연장만 예외다.
- 모든 route에서 기본 toast 위치는 현재 MainTabs의 bottom bar 위 높이와 동일하고, IME가 가릴 때만 keyboard 위로 이동한다.
- `Dialog`, `Popup`, `ModalBottomSheet`가 toast를 가릴 수 있으며, 이때도 toast duration/queue는 중단되지 않는다.
- 새로운 production dependency, 앱 singleton toast bus, application-scope coroutine을 추가하지 않는다.
- 기존 snackbar, popup, bottom sheet 동작에 회귀가 없다.

## Validation

1. `RecapToastViewModelTest`에서 virtual time으로 최소 다음을 검증한다.
   - enqueue된 단일 요청이 소비되어 지정 duration 후 제거된다.
   - 두 요청과 동일 메시지 중복 요청이 drop 없이 FIFO로 표시되고 exit animation 시간 이후 다음 항목으로 진행한다.
   - 요청 producer coroutine 종료/취소와 queue 소비 lifecycle이 분리된다.
   - host/UI collector가 교체되어도 current와 queue가 재시작되지 않고 남은 시간만 진행된다.
   - background/collector 부재 중에도 virtual time이 진행되면 toast가 만료된다.
   - ViewModel이 새로 생성되면 이전 current/queue가 복원되지 않는다.
2. 접근성 timeout 계산을 순수 정책으로 분리할 경우 기본 timeout 유지와 시스템 권장 timeout 연장을 단위 테스트한다.
3. 정적 확인:
   - `rg -n "RecapToastHost\\(" app feature --glob "*.kt"` 결과에서 hosted renderer는 `RecapApp.kt` 한 곳만 남아야 한다.
   - `rg -n "pending(Main|Recent)ScreenshotDeletedToast|pendingScreenshotDeletedToast" app feature --glob "*.kt"` 결과가 없어야 한다.
4. PowerShell 단위 테스트:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

5. PowerShell debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

6. 런타임 검증은 사용자 결정에 따라 이번 handoff에서 수행하지 않는다.

## Out of scope

- Android 플랫폼 `Toast`로 교체
- Snackbar를 전역 toast queue로 통합
- Toast 디자인, 문구, 일반 환경의 기본 duration 또는 animation 변경(접근성 권장 timeout 연장은 예외)
- Haze 버전 변경 및 신규 production dependency 추가
- Macrobenchmark 모듈/성능 벤치마크 인프라 추가
- `Dialog`, `Popup`, `ModalBottomSheet`처럼 별도 window/layer로 표시되는 컴포넌트보다 toast를 항상 위에 그리도록 window 구조를 변경하는 작업
- process death 이후 toast current/queue 복원
- Toast 탭/스와이프 dismiss
- ADB, mobile-mcp 또는 수동 화면 조작을 통한 런타임 검증
- Toast와 무관한 기존 화면 Haze 구조 리팩터링

## Technical Debt

- none

## Cursor Result

- Changed files: `core/design/.../toast/RecapToast.kt`, `RecapToastDispatcher.kt`, `RecapToastModels.kt`, `RecapToastDurationPolicy.kt`, `RecapToastDurationPolicyTest.kt`, `app/.../MainActivity.kt`, `RecapApp.kt`, `RecapToastViewModel.kt`, `RecapToastDurationResolver.kt`, `RecapToastViewModelTest.kt`, `RecapNavHost.kt`, `RecapMainScreen.kt`, `feature/onboarding/OnboardingRoute.kt`, `OnboardingScreen.kt`, `feature/collection/CollectionRoute.kt`, `feature/settings/DataManagementRoute.kt`, `DataManagementScreen.kt`, `feature/screenshot/ScreenshotRoute.kt`, `feature/home/RecentOrganizedScreenshotsRoute.kt`, `feature/developer/ComponentGardenScreen.kt`
- Build/test: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN; static checks: hosted `RecapToastHost(` only in `RecapApp.kt`, pending screenshot deleted toast symbols absent
- Open questions: none

## Codex Review

- Blocking: none
- Nits: none
- Verdict: DONE
- Note: `RecapCaption1`은 사용자가 확인한 올바른 Toast 디자인 토큰이므로 의도된 변경으로 승인했다.
