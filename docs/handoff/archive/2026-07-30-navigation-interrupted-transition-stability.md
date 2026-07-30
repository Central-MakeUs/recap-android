# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 앱 전역 push/pop은 Navigation3 `1.1.4` 위의
  `core/design/.../animation/RecapNavDisplay.kt`가 담당한다.
- `RecapNavDisplay`는 `SeekableTransitionState<Scene<T>>`와
  `AnimatedContent`를 직접 조합하며, 일반 push/pop과 predictive back
  scrub/cancel/commit을 같은 호스트에서 처리한다.
- 최근 연속 back 보완으로 `RecapBackCommitQueue`와 누적형
  `RecapSceneZIndexTracker`가 추가됐다. 이 보완은 같은 방향의 연속 pop은
  다루지만, 화면 버튼에서 발생하는 push/pop은 여전히 `NavBackStack`을 즉시
  변경하므로 진행 중인 전환이 반대 방향으로 재타기팅될 수 있다.
- 현재 `navigationKind`는 최신 `sceneState.entries`로 계산하지만,
  `targetContentZIndex`는 `LaunchedEffect(scene)`가 새 scene을
  `animateTo`/`snapTo`하기 전의 `transition.currentState`와
  `transition.targetState`로 계산한다. 빠른 push → pop 또는 pop → push 시
  한 프레임 안에서 서로 다른 navigation revision의 방향과 scene pair가
  섞일 수 있다.
- `reuseExistingTarget = transition.targetState != scene` 휴리스틱은 위
  불일치 상태에서 취소될 이전 target의 z-index를 재사용한다. 그 결과
  사라지는 화면이 다시 최상단에 나타나거나, 전환 중 content가 비면서
  Activity window가 노출될 수 있다.
- `Theme.RECAP`의 `android:windowBackground`는 splash용 파란색
  `#FF596AFF`를 그대로 사용한다. 따라서 Compose content가 한 프레임이라도
  전체 화면을 덮지 못하면 파란 배경이 보인다.
- `mobile-mcp`에서 `SM-S948N`의 Home/Settings 화면과 접근성 좌표를 확인하고
  빠른 교차 탭을 전송했다. 최종 화면은 정상 Settings로 정착했지만 문제는
  전환 중의 일시적인 합성 오류이므로, 구현 후 화면 녹화 기반 반복 검증이
  필요하다.

## Spec

### 1. 전환 방향과 레이어 결정을 하나의 scene pair에 고정

- `RecapNavDisplay` 안에 순수 Kotlin으로 단위 테스트 가능한 전환 계획
  모델/트래커를 둔다. 이름은 구현자가 현재 파일 관례에 맞게 정할 수 있지만,
  한 전환 계획은 최소한 아래 값을 함께 소유해야 한다.
  - 실제 initial scene key
  - 실제 target scene key
  - `Forward` / `Pop` / `Replace`
  - target content z-index
- transform과 `targetContentZIndex`는 반드시 동일한 전환 계획에서 가져온다.
  최신 back stack으로 계산한 방향을 아직 갱신되지 않은
  `transition.currentState`/`targetState`에 적용하지 않는다.
- `AnimatedContent.transitionSpec`이 평가하는 actual initial/target scene pair와
  일치하는 계획만 사용한다. pair가 일치하지 않는 stale 계획을 재사용하지
  않는다.
- 기존 `reuseExistingTarget = transition.targetState != scene` 휴리스틱은
  제거하거나, 동일한 pair/revision임이 증명될 때만 동작하는 명시적 상태로
  대체한다.
- 레이어 불변식:
  - forward: incoming target은 outgoing initial보다 위에 있다.
  - pop/predictive pop: incoming target은 outgoing initial보다 아래에 있다.
  - replace: 전환 없음 정책을 유지하며 이전 scene의 누적 z-index가 새 root로
    전파되지 않는다.

### 2. 진행 중 전환의 역전/취소를 명시적으로 처리

- 새 requested scene이 진행 중 전환의 `currentState`와 같다면 반대 방향
  전환이 취소된 것이다. 이 경우 해당 current scene으로 snap하고, 취소된
  target의 레이어 기록과 stale 전환 계획을 다음 전환 전에 제거한다.
- 새 requested scene이 current/target과 모두 다르면 현재 전환을 새 target으로
  재타기팅하되, 새 actual pair에 대한 전환 계획을 생성한다. 이전 target의
  방향/z-index를 새 target에 복사하지 않는다.
- 전환이 idle이 되면 현재 scene 하나만 남도록 레이어 상태를 정규화한다.
- 빠른 역전을 막기 위한 전역 debounce, 고정 시간 입력 잠금, 화면별
  `enabled = false` 처리는 추가하지 않는다. 사용자의 마지막 navigation
  intent에 즉시 반응하는 interruptible 전환을 유지한다.
- 일반 화면 버튼의 push/pop을 지연 큐에 넣지 않는다. 기존
  `RecapBackCommitQueue`는 predictive/완료 back commit 중 중복 back을
  직렬화하는 현재 역할만 유지한다.

### 3. predictive back 회귀 방지

- predictive scrub은 기존처럼 `RecapNavigationMotion.pop()` 계열과
  `PredictiveMaxFraction`을 사용한다.
- predictive cancel은 원래 scene으로 snap한 뒤 preview progress와 전환
  계획/레이어 상태가 idle 기준으로 정리되어야 한다.
- predictive commit 및 commit 중 queued back은 각각 정확히 한 번씩
  back stack을 pop해야 한다.
- 기존 `awaitBackHandlerRefresh()` 및 replay 입력의 의미를 변경해야 한다면
  같은 동작을 보장하는 테스트 근거를 남긴다.

### 4. 파란 Activity 배경 노출 방어

- `Theme.RECAP.Starting`의 splash 배경은 현재 파란색을 유지한다.
- splash 이후 적용되는 `Theme.RECAP`의 `android:windowBackground`는 앱의
  화이트모드 배경과 맞는 흰색 resource로 분리한다. splash 전용 color를
  post-splash window background로 재사용하지 않는다.
- `RecapAppReadyContent`의 root navigation content 뒤에도
  `RecapBackground` 기반의 불투명한 full-size 배경을 둔다. 색을 Kotlin이나
  XML에 중복 하드코딩하지 말고 기존 디자인 토큰/전용 resource를 사용한다.
- 이 배경은 레이어 버그를 가리는 주 해결책이 아니라, window가 노출되는
  프레임의 시각적 fallback이다.

### 5. 회귀 테스트

- 기존 `RecapBackCommitQueueTest`, `RecapNavigationMotionTest`,
  `RecapNavigationMotionOffsetsTest`를 유지한다.
- `RecapSceneZIndexTrackerTest`는 새 전환 계획 모델에 맞게 수정하거나 새
  테스트 파일로 대체하고, 최소한 다음 sequence를 검증한다.
  1. idle A → forward A/B → 완료 전 reverse to A → forward A/B
  2. idle B → pop B/A → 완료 전 reverse to B → pop B/A
  3. A → B → C 연속 forward
  4. C → B → A 연속 pop
  5. replace 시 이전 root의 레이어 상태 제거
- 각 역전 테스트는 다음을 확인한다.
  - transform 방향과 z-index 방향이 같은 actual pair를 사용한다.
  - 취소된 target이 다음 전환의 최상단 레이어로 남지 않는다.
  - idle 정규화 후 현재 scene 외의 tracker 상태가 남지 않는다.
- 가능하면 scene/Compose API와 무관한 순수 planner 테스트로 핵심 상태 머신을
  검증한다. 시각 동작은 아래 runtime validation으로 보완한다.

## Files to Touch

- `core/design/src/main/java/com/chalkak/recap/core/design/animation/RecapNavDisplay.kt`
- `core/design/src/test/java/com/chalkak/recap/core/design/animation/RecapSceneZIndexTrackerTest.kt`
- 필요 시
  `core/design/src/test/java/com/chalkak/recap/core/design/animation/RecapSceneTransitionPlannerTest.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/colors.xml`

## Acceptance Criteria

- Home → Settings → Home과 Home → Settings → Home → Settings를 slide
  `350ms` 안쪽 간격으로 반복해도 파란 window background가 보이지 않는다.
- 빠른 push/pop 역전 중 사라지는 화면이 새 target 위로 pop하며 나타나지
  않는다.
- 전환이 끝난 뒤 실제 표시 화면과 back stack top이 항상 일치한다.
- Settings → NotificationSettings → Settings처럼 depth가 2 이상인 경로에서도
  빠른 pop/push 교차 시 동일한 레이어 불변식이 유지된다.
- A → B → C 연속 back과 기존 빠른 back 2회 이상 동작이 회귀하지 않는다.
- 일반 push/pop의 slide/parallax 방향, duration, predictive progress 감각은
  변경되지 않는다.
- predictive back cancel은 원래 화면으로 복귀하고, commit은 한 번만 pop하며,
  commit 중 추가 back은 기존 정책대로 순차 처리된다.
- 앱 시작 splash는 기존 파란색을 유지하고, splash가 끝난 뒤 window fallback은
  흰색이다.
- 새 production dependency를 추가하지 않는다.

## Validation

1. 순수 로직 및 기존 회귀 테스트:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:design:testDebugUnitTest :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

2. Debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

3. `mobile-mcp` runtime 검증:
   - 화면 녹화를 시작하고 Home의 설정 버튼과 Settings의 back 버튼을
     각 전환 완료 전 간격으로 10회 이상 교차 입력한다.
   - Settings → NotificationSettings에서도 동일하게 교차 입력한다.
   - 녹화를 프레임 단위로 확인해 파란 배경, 빈 프레임, stale 화면의 최상단
     재등장이 없는지 확인한다.
   - predictive back을 중간 cancel 3회, commit 3회 수행한다.
   - 빠른 system/top-bar back 2회 이상을 반복해 기존 연속 back 수정이
     유지되는지 확인한다.
   - 보고 시 확인한 화면, 수행한 조작, 최종 back stack과 관찰 결과를 요약한다.

## Out of scope

- Navigation3를 Navigation2 또는 다른 navigation library로 교체
- 전환 duration, parallax fraction, easing의 디자인 변경
- 전역 입력 debounce/잠금 또는 모든 navigation command 큐잉
- `RecapMainTabNavHost`의 Home ↔ Collection 전환 변경. 이 호스트는 stock
  `NavDisplay`를 사용하므로 동일 증상이 별도로 재현되지 않는 한 수정하지 않는다.
- 화면별 UI/비즈니스 로직 변경
- predictive back UX 재설계

## Technical Debt

- 없음. 이번 범위에서 발견한 후속 항목은 모두 현재 수정/검증 범위에 포함한다.

## Cursor Result

- Changed files: `core/design/.../RecapNavDisplay.kt` (`RecapSceneTransitionPlanner` + interruptible cancel/retarget), `core/design/.../RecapSceneTransitionPlannerTest.kt`, `core/design/.../RecapSceneZIndexTrackerTest.kt`, `app/.../RecapApp.kt`, `app/.../values/themes.xml`, `app/.../values/colors.xml`
- Build/test: `.\gradlew.bat :core:design:testDebugUnitTest :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1` GREEN; `.\gradlew.bat assembleDebug` GREEN
- Runtime (`SM-S948N` / mobile-mcp + adb): Home↔Settings 12회·Settings↔NotificationSettings 12회를 ~200ms 간격으로 교차 입력. 녹화(`recap-nav-validation.mp4`, 241 frames@2fps)에서 splash blue(`#596AFF`) 비율 >8% 프레임 **0**. 교차 후 화면은 Settings로 정상 정착(흰 배경). top-bar back commit 3회 OK. 빠른 system BACK 연속 입력은 Notification→Settings→Home 경로로 동작. 이 기기에서 좌측 edge swipe는 Gemini Assistant로 가로채여 predictive cancel/commit 제스처는 실기기에서 재현 불가 → planner cancel/idle·`RecapBackCommitQueue` 단위 테스트로 보완.
- Open questions: none

## Codex Review

- Blocking: none
- Nits: predictive back cancel은 실기기에서 독립 재현하지 못했다. 왼쪽 edge는
  Gemini가 가로채고, 오른쪽 edge는 `mobile-mcp`의 120px 짧은 swipe도 commit으로
  판정했다. cancel 시 원래 scene으로 snap하고 planner를 정리하는 코드 경로와
  cancel/idle 순수 단위 테스트로 보완했다.
- Validation: `:core:design:testDebugUnitTest :app:testDebugUnitTest` GREEN,
  `git diff --check` GREEN. `SM-S948N` 오른쪽 edge predictive commit 3회에서
  Notification Settings → Settings가 매번 정확히 한 번 pop되고 정상 정착했다.
  Cursor의 Home↔Settings 및 Settings↔NotificationSettings 각 12회 교차 입력
  녹화 결과도 파란 배경/빈 프레임/stale 최상단 화면 없이 정상이다.
- Verdict: DONE
