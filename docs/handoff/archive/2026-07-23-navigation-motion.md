# HANDOFF.md - RECAP

> Archived task: Navigation push/pop and predictive back motion

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- RECAP은 AndroidX Navigation3 `1.1.4`를 사용하며 `NavDisplay`가 root, app, main tab,
  onboarding, developer, collection, screenshot 계층에 각각 존재한다.
- 현재 `RecapMainTabNavHost`만 자체 forward/pop transition을 가진다. 이 transition은
  화면 폭의 `1/6` 이동과 fade를 조합하며, predictive pop은 화면 transition을 끄고
  Collection -> Home일 때 bottom bar highlight만 최대 `1/3` 이동한다.
- 사용자는 push/pop 화면 전환을 iOS 기본 전환 방향처럼 공통 컴포넌트화하려 한다.
  push 시 새 화면은 오른쪽에서 나타나고 현재 화면은 왼쪽으로 일부 parallax 이동한다.
  pop 시 현재 화면은 오른쪽으로 사라지고 이전 화면은 왼쪽 parallax 위치에서 복귀한다.
- predictive back gesture 중 화면 이동은 일반 push/pop 최대 이동 거리의 40%까지만
  gesture progress에 따라 움직이고, commit 후에는 일반 pop 완료 동작으로 이어져야 한다.
  cancel 시에는 현재 화면 위치로 복귀해야 한다.
- root의 Onboarding -> Main(`RecapMainScreen`)은 back stack push/pop이 아니라 `clear` 후
  교체되는 상태 전환이며, 사용자가 명시적으로 slide 적용 대상에서 제외했다.
- Screenshot Edit는 미저장 변경이 있으면 back이 즉시 pop되지 않고 확인 팝업을 연다.
  따라서 이 상태에서 목적지 preview를 끝까지 보여준 뒤 pop으로 확정하는 일반 predictive
  back 모델을 그대로 적용하면 실제 back 결과와 preview가 불일치할 수 있다.
- Organize는 `NavDisplay` 목적지가 아니라 MainTabs 위 overlay이며 이번 navigation
  transition 컴포넌트의 직접 적용 대상이 아니다.
- Android 공식 Navigation3 API는 `transitionSpec`, `popTransitionSpec`,
  `predictivePopTransitionSpec`을 별도로 제공한다. 공통 motion policy는 이 세 경로를
  서로 분리해 제공해야 한다.

## Spec

### Motion policy

- `:core:design`에 navigation motion policy를 나타내는 단일 공통 파일을 추가한다.
  UI를 그리는 Composable이 아니라, `ContentTransform`을 반환하는 공통 transition spec
  집합으로 둔다.
- 공통 spec은 적어도 다음 세 동작을 별도 함수/값으로 노출한다.
  - forward push transition
  - committed/non-gesture pop transition
  - predictive pop transition (일반 slide 최대 이동 거리의 40%)
- 일반 push:
  - 새 전면 화면은 `+100% width -> 0`으로 이동한다.
  - 기존 뒤 화면은 `0 -> -30% width`로 parallax 이동한다.
  - fade를 조합하지 않는다.
- 일반 pop:
  - 현재 전면 화면은 `0 -> +100% width`로 이동한다.
  - 이전 화면은 `-30% width -> 0`으로 parallax 복귀한다.
  - fade를 조합하지 않는다.
- 공통 일반 slide duration은 400ms를 기본값으로 하고,
  기존 Compose `tween` easing을 사용한다. duration/easing/parallax fraction/predictive
  max fraction은 공통 policy 내부 named constant로 한 곳에서 관리한다.
- predictive transition은 gesture progress가 `0f..1f`일 때 각 요소의 일반 pop 이동량 중
  `0%..40%`만 preview한다.
  - 현재 전면 화면: 최대 `+40% width`
  - 이전 화면: 일반 parallax 이동 거리 30%의 최대치인 `-30% -> -18% width`
  - gesture 시작 edge가 왼쪽/오른쪽 어느 쪽이든 제품 정책상 pop 방향은 오른쪽으로 고정한다.
- predictive cancel은 back stack을 변경하지 않고 두 화면을 gesture 시작 위치로 복귀시킨다.
- predictive commit은 back stack을 정확히 한 번 pop하고, preview 마지막 위치에서 일반
  pop의 끝 위치까지 이어져야 한다. commit 직후 화면이 시작 위치로 순간 이동하거나
  predictive 40% 지점에서 사라지면 안 된다.
- Navigation3 기본 predictive transition만으로 위 두 단계 연결이 끊기는 경우, 공통
  policy에서 gesture progress와 commit animation을 분리해 보유하고 `onBack` pop 시점을
  완료 animation 뒤로 조정한다. 각 feature에서 별도 `Animatable`/offset 로직을 복제하지
  않는다.
- `RecapBottomBar`의 highlight motion은 화면 transition policy와 구현을 섞지 않는다.
  다만 동일한 predictive progress를 받는 현재 구조는 유지하고, max fraction은 사용자
  확정값에 맞춰 화면과 별도 named constant로 관리한다.
- 단순 push/pop에 공통 spec을 적용할 대상:
  - root Main <-> Developer
  - app MainTabs <-> Settings/Search/Recent/Screenshot 및 각 app 하위 push/pop
  - Developer Options <-> ComponentGarden
  - Onboarding Flow <-> AddToFavoriteGuide
  - Collection Overview <-> detail
  - Screenshot Detail/Edit/Fullscreen의 실제 back stack push/pop
  - Main tab Home <-> Collection
- Onboarding 내부 step `AnimatedContent`는 Navigation3 push/pop이 아니므로 기존
  slide+fade 동작을 유지한다. Onboarding Flow <-> AddToFavoriteGuide는 실제
  Navigation3 push/pop이므로 공통 slide를 적용한다.
- root Onboarding -> Main(`RecapMainScreen`) 교체에는 `EnterTransition.None togetherWith
  ExitTransition.None` 또는 동등한 명시적 무전환을 적용한다. 이 예외 때문에 root의
  Main <-> Developer push/pop까지 무전환이 되면 안 된다.
- Organize overlay 표시/닫기, dialog/bottom sheet, 외부 Activity 전환은 공통 push/pop
  적용 대상에서 제외한다.
- Screenshot Edit에서 미저장 변경이 있는 동안에는 predictive preview를 비활성화한다.
  system back commit은 기존 확인 팝업만 표시하며 back stack을 변경하지 않는다. 팝업의
  "나가기"를 확정한 시점에 Edit를 pop하고 일반 pop slide를 실행한다. 미저장 변경이
  없으면 다른 단순 pop과 동일하게 predictive preview/cancel/commit을 지원한다.
- 새 production dependency는 추가하지 않는다. `:core:design`에 필요한 기존 Compose
  animation API 노출 여부를 확인하고, Navigation3 전용 타입을 공통 API에 노출해야만
  하는 경우에만 기존 version catalog의 Navigation3 UI 의존성을 사용한다.

### Structural safeguards

- 중첩 NavDisplay에서는 가장 안쪽의 활성 back handler가 gesture를 소유해야 한다.
  child back stack에서 pop 가능한 상태가 root/app pop과 동시에 처리되지 않도록 현재
  `NavigationEventDispatcherOwner` 및 handler enable 조건을 보존하거나 보완한다.
- route 교체를 push로 오인하지 않도록 root transition은 route/entry 단위 override 또는
  동등하게 명시적인 분기 방식으로 적용한다. Onboarding -> Main 무전환과
  Main <-> Developer slide를 서로 다른 transition policy로 선택해야 한다.
- transition 함수는 route 상태, ViewModel, coroutine을 소유하지 않는 stateless policy로
  유지한다.

## Files to Touch

- `core/design/src/main/java/com/chalkak/recap/core/design/animation/` 아래 공통 navigation
  transition spec 신규 파일
- `core/design/build.gradle.kts` (공통 API 구현에 실제로 필요한 경우에만)
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt` (predictive progress 연결을
  보완해야 하는 경우)
- `feature/developer/src/main/java/com/chalkak/recap/feature/developer/DeveloperRoute.kt`
- `feature/onboarding/src/main/java/com/chalkak/recap/feature/onboarding/OnboardingRoute.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionRoute.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotRoute.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/bottombar/RecapBottomBar.kt`
  (공통 화면 spec과 섞인 값/명명 정리 또는 확정값 반영이 필요한 경우에만)
- 관련 모듈의 `src/test/java/...` 또는 `src/androidTest/java/...` (검증 가능한 순수 계산
  또는 핵심 transition 동작 테스트를 추가하는 경우)

## Acceptance Criteria

- 모든 대상 단순 push에서 새 화면이 오른쪽 전체 폭에서 진입하고 기존 화면은 왼쪽으로
  화면 폭의 30%만 parallax 이동한다.
- 모든 대상 단순 pop에서 현재 화면이 오른쪽 전체 폭으로 퇴장하고 이전 화면은 왼쪽
  30% 위치에서 원위치로 parallax 복귀한다.
- 일반 push/pop에는 fade가 포함되지 않는다.
- gesture progress 100%에서 predictive 시각 이동량은 일반 slide 최대 이동량의 정확히
  40%이며, 0%와 중간 progress도 비례한다.
- predictive cancel 시 back stack은 변경되지 않고 화면은 원위치로 복귀한다.
- predictive commit 시 대상 back stack entry로 정확히 한 번 pop되고 일반 pop 완료
  애니메이션으로 자연스럽게 이어지며 위치 snap이나 중간 소실이 없다.
- nested navigation에서 한 gesture가 child와 parent back stack을 동시에 pop하지 않는다.
- Onboarding 완료 후 `RecapMainScreen`으로 교체될 때 slide/fade가 없고 즉시 교체된다.
- Onboarding 내부 step animation, Organize overlay, dialog/bottom sheet, 외부 Activity
  전환은 기존 동작을 유지한다. Onboarding AddToFavoriteGuide push/pop에는 공통
  slide가 적용된다.
- Screenshot Edit에 미저장 변경이 있으면 predictive preview가 보이지 않고 back
  commit은 확인 팝업만 표시한다. 팝업에서 나가기를 확정해야 일반 pop slide가 실행된다.
- 기존 bottom bar highlight predictive animation은 화면 transition과 독립적으로
  동작하며 cancel/commit 후 선택 상태와 일치한다.
- 새 animation 숫자/`ContentTransform` 조합이 각 feature에 복제되지 않고 공통
  navigation motion policy를 사용한다.

## Validation

- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
- gesture navigation이 활성화된 API 35+ emulator/device에서 다음을 수동 확인한다.
  - 각 적용 계층의 push, toolbar/system back pop
  - predictive back drag 0%/중간/100%, cancel, commit
  - nested child stack 우선 pop 및 parent stack 보존
  - Collection -> Home 화면과 bottom bar highlight의 동기화
  - Screenshot Edit 미저장 변경 예외의 확정 동작
  - Onboarding -> Main 교체 및 Onboarding 내부 step 기존 동작

## Out of scope

- Navigation3 버전 변경
- route/back stack 구조 전면 개편
- Organize overlay를 NavDisplay route로 마이그레이션
- Onboarding 내부 step animation 재설계
- dialog, bottom sheet, 외부 Activity transition 변경
- shared element transition, scale/blur 효과 추가
- Android 시스템 back-to-home/cross-activity animation 커스터마이징

## Technical Debt

- Stored in `docs/BACKLOG.md`: RecapNavDisplay는 single-pane 전용이며 NavDisplay의 OverlayScene/공유 요소 lifecycle 패리티는 없음

## Cursor Result

- Changed files:
  - `core/design/build.gradle.kts`
  - `core/design/.../animation/RecapNavigationMotion.kt`
  - `core/design/.../animation/RecapNavDisplay.kt`
  - `core/design/.../animation/RecapNavigationMotionOffsetsTest.kt`
  - `core/design/.../component/bottombar/RecapBottomBar.kt`
  - `app/.../RecapApp.kt`
  - `app/.../RecapNavHost.kt`
  - `feature/developer/.../DeveloperRoute.kt`
  - `feature/onboarding/.../OnboardingRoute.kt`
  - `feature/collection/.../CollectionRoute.kt`
  - `feature/screenshot/.../ScreenshotRoute.kt`
  - `docs/BACKLOG.md`
- Build/test: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN
- Open questions: none
- Manual validation (SM-S948N, API 37, `navigation_mode=2` gesture):
  - PASS Home↔Settings push / toolbar·system back pop / edge predictive commit (Settings→Home)
  - PASS nested Settings→계정관리 pop: child만 pop, parent Settings 유지
  - PASS Collection detail→overview nested pop (parent Home 미변경)
  - PASS Collection→Home edge predictive commit + bottom bar Home 선택 일치
  - PASS Screenshot Detail→Edit, 미저장 시 edge swipe로 pop 안 됨; system BACK은
    확인 팝업만; `그만두기` 후 Edit pop
  - PASS Developer / ComponentGarden push·pop
  - PASS Onboarding AddToFavoriteGuide push/pop
  - PASS Onboarding 완료 → RecapMainScreen 교체 (Developer 온보딩 초기화 후 재완료)
  - PASS predictive cancel: 사용자가 finger로 threshold 전 release를 확인했으며
    화면 원위치 복귀와 back stack 유지가 정상 동작함

## Codex Review

- Blocking: none
- Nits: none
- Verdict: DONE
