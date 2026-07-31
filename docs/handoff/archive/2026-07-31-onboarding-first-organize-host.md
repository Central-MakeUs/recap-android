# HANDOFF.md - RECAP

<!-- Archived: 2026-07-31 -->

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 현재 `OnboardingRoute`의 `OpenScreenshotPicker`는
  `onOnboardingComplete(true)`를 호출한다.
- `RecapStartupViewModel.completeOnboarding(openOrganize = true)`는
  `onboardingCompleted=true`를 즉시 저장하고 `pendingOpenOrganize`를 남긴다.
  이 때문에 root가 `Main`으로 전환된 뒤 Home 위에서 `OrganizeRoute`가 열린다.
- 변경 후에는 `RecapRootRoute.Onboarding`이 유지된 상태에서
  `OnboardingRoute`의 `StartFirstAnalyze` 화면 위에 `OrganizeRoute`
  (피커 -> 확인)를 오버레이한다. 피커의 스크림 뒤에는 Home이 아니라
  `OnboardingStartFirstAnalyzeScreen`이 그대로 보여야 한다.
- 스크린샷 분석은 기존 app-scoped
  `ScreenshotAnalysisProgressViewModel`과
  `OrganizeAnalysisStatusRoute`를 재사용한다.
- 사용자 결정:
  - 피커/확인 취소는 온보딩 미완료 상태로 `StartFirstAnalyze`에 남는다.
  - 분석 진행 화면도 온보딩 위에서 표시한다.
  - 분석 성공, 부분 성공, 전체 실패의 terminal 결과 화면에서 사용자가
    기본 `완료/닫기` 버튼을 누른 뒤에만 온보딩을 완료하고 Home으로 간다.
  - 진행 중 취소는 온보딩 완료로 간주하지 않는다.

## Spec

1. `RecapRootRoute.Onboarding` entry를 첫 정리 플로우의 host로 사용한다.
   - `OnboardingRoute`를 배경으로 계속 composition한다.
   - `OpenScreenshotPicker` action은 더 이상 온보딩 완료 콜백을 호출하지 않고,
     이 entry의 로컬 첫 정리 overlay 상태를 연다.
   - 열린 동안 같은 `Box`의 상위 레이어에 기존 `OrganizeRoute`를 composition해
     피커와 확인 화면을 제공한다.
   - `:feature:onboarding`이 `:feature:organize`에 직접 의존하지 않게 하고,
     두 feature의 조합은 `:app`에서 담당한다.

2. 피커/확인 취소 정책을 적용한다.
   - 선택 없이 피커 X, 스크림, swipe, 시스템 back으로 dismiss하면
     `OrganizeRoute`만 닫고 `StartFirstAnalyze` 화면을 다시 노출한다.
   - 선택이 있어 기존 discard confirmation이 뜨는 경우에는 사용자가 폐기를
     확정한 뒤 동일하게 `OrganizeRoute`만 닫는다.
   - 피커에서 확인 화면으로 이동한 뒤 시스템/툴바 back을 누르면 기존
     `OrganizeRoute`의 selection clear 정책을 유지하면서 Organize 전체를 닫고
     `StartFirstAnalyze`로 복귀한다. 피커를 자동 재오픈하지 않는다.
   - 위 모든 경로에서 `onboardingCompleted`를 저장하거나 Main/Home으로
     navigation하지 않는다.

3. 첫 분석 시작과 상태 화면을 온보딩 entry 안에서 처리한다.
   - `OrganizeRoute.onOrganizeComplete(candidates)`가 호출되면 첫 정리 overlay를
     닫고 `ScreenshotAnalysisProgressViewModel.startAnalysis(candidates)`를
     정확히 한 번 호출한다.
   - 해당 온보딩 분석 세션이 활성 상태임을 별도로 추적한다. 일반 Main 분석의
     오래된 terminal state나 개발자 온보딩 reset 전 상태가 온보딩 결과 화면으로
     오인되지 않게 한다.
   - 활성 세션 동안 `ScreenshotAnalysisProgressUiState`를 기존
     `toOrganizeAnalysisStatusUiState()`로 매핑하고,
     `OrganizeAnalysisStatusRoute`를 `OnboardingRoute` 위의 최상위 레이어로
     표시한다.
   - 기존 분석 완료 알림 설정 값과 변경 callback도 Main에서와 동일하게
     `OrganizeAnalysisStatusRoute`에 전달한다.

4. 분석 진행/종료 interaction을 다음과 같이 고정한다.
   - `Progress`에서 취소 버튼 또는 시스템 back:
     `ScreenshotAnalysisProgressViewModel.cancelAnalysis()`를 호출하고
     온보딩 분석 세션을 종료한 뒤 `StartFirstAnalyze`로 복귀한다.
     `onboardingCompleted`는 false로 유지한다.
   - `Success`, `PartialFailed`, `Failed` terminal state:
     결과 화면을 유지하며 자동으로 Main/Home으로 이동하지 않는다.
   - terminal 결과 화면의 기본 `완료/닫기` 버튼:
     먼저 `dismissResult()`로 app-scoped terminal state를 비우고 온보딩 분석
     세션을 종료한 뒤 `RecapStartupViewModel.completeOnboarding()`을 호출한다.
     DataStore의 완료 값 반영으로 root가 Main/Home으로 전환되어야 한다.
   - terminal 결과 화면의 시스템 back은 완료 동작으로 간주하지 않고 소비하여
     결과 화면에 머문다. Home 전환은 화면의 기본 `완료/닫기` 버튼으로만 한다.

5. `나중에 하기`는 즉시 완료 경로로 유지한다.
   - `SkipStartFirstAnalyze`에서만 분석 없이
     `RecapStartupViewModel.completeOnboarding()`을 호출한다.
   - 완료 저장 후 Main의 Home으로 이동하며 Organize 피커나 분석 상태 route를
     자동으로 열지 않는다.

6. 기존 Main Organize 진입과 분석 플로우를 보존한다.
   - Home/Collection/최근 정리 화면에서 여는 피커는 계속
     `RecapMainScreen` 위 overlay로 동작한다.
   - Main에서 분석을 시작하면 기존처럼 `AppRoute.OrganizeAnalysisStatus`를
     사용한다.
   - 온보딩 전용 `pendingOpenOrganize` 전달 경로는 제거한다.
     `RecapNavHost` 내부의 `requestOpenOrganize` 등 Main 내부 진입 요청은
     유지한다.

7. 완료 상태 API와 문서를 현재 정책에 맞춘다.
   - `RecapStartupViewModel.completeOnboarding`에서 `openOrganize` 인자와
     `_pendingOpenOrganize`/consume API를 제거하고 완료 저장만 담당하게 한다.
   - reset은 기존 session/token/완료 상태 reset 동작을 유지한다.
   - `docs/PROJECT.md`의 현재 앱 흐름에서 “온보딩 완료 후 Main에서 피커를
     연다”는 설명을 새 온보딩-host 흐름과 완료 시점으로 교체한다.
   - `docs/ORGANIZE_OVERLAY_NAVIGATION.md`에는 Main overlay 동작을 변경하지
     않으면서, 온보딩 첫 정리에서는 별도의 Onboarding root entry가 같은
     `OrganizeRoute`를 host한다는 예외/계층과 취소·완료 정책을 추가한다.

## Files to Touch

- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapStartupViewModel.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt` (obsolete external pending-open contract 정리 시)
- `feature/onboarding/src/main/java/com/chalkak/recap/feature/onboarding/OnboardingRoute.kt`
- `app/src/test/java/com/chalkak/recap/app/RecapStartupViewModelTest.kt`
- 첫 정리 overlay/session 상태를 별도 coordinator/reducer로 추출하는 경우
  대응하는 `app/src/main/...` 및 `app/src/test/...` 파일
- `docs/PROJECT.md`
- `docs/ORGANIZE_OVERLAY_NAVIGATION.md`

## Acceptance Criteria

- [ ] `StartFirstAnalyze`에서 스크린샷 선택을 누르면 root route는
  `Onboarding`으로 유지되고, 피커 스크림 뒤에 해당 온보딩 화면이 보인다.
- [ ] 피커를 펼쳤다가 선택 없이 취소하면 Home으로 이동하지 않고
  `StartFirstAnalyze`에 남으며 `onboardingCompleted=false`이다.
- [ ] 선택 폐기 확인을 포함한 피커 dismiss와 확인 화면 back 모두 Organize
  전체를 닫고 `StartFirstAnalyze`로 복귀하며 완료 값을 저장하지 않는다.
- [ ] 확인 화면에서 정리를 시작하면 Main으로 전환하지 않은 채 기존 분석 진행
  화면이 온보딩 위에 표시된다.
- [ ] 진행 중 취소 버튼과 시스템 back은 분석을 취소하고
  `StartFirstAnalyze`로 복귀하며 완료 값을 저장하지 않는다.
- [ ] 성공, 부분 성공, 전체 실패 terminal 결과가 도착해도 결과 화면이 유지되고
  자동으로 Home으로 이동하지 않는다.
- [ ] terminal 결과 화면의 시스템 back은 Home 이동이나 완료 저장을 발생시키지
  않는다.
- [ ] terminal 결과 화면의 `완료/닫기` 버튼을 누르면 terminal state가 정리되고
  `onboardingCompleted=true` 저장 후 Main의 Home으로 이동한다.
- [ ] `나중에 하기`는 분석 없이 `onboardingCompleted=true` 저장 후 Home으로
  이동하며 피커를 열지 않는다.
- [ ] 스크린샷 선택, 피커/확인 취소, 분석 시작 시점에는
  `onboardingCompleted=true`가 저장되지 않는다.
- [ ] Home/Collection 등 기존 Main Organize picker와 Main 분석 상태 route의
  동작이 회귀하지 않는다.
- [ ] feature 간 신규 직접 의존성이나 신규 production dependency를 추가하지
  않는다.
- [ ] 변경된 완료 API/상태에 대한 단위 테스트가 통과하고 프로젝트가 debug
  build된다.

## Validation

1. 단위 테스트:

   ```powershell
   $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
   ```

   최소 확인:
   - `RecapStartupViewModel.completeOnboarding()`이 완료 값을 true로 저장한다.
   - reset의 기존 token/완료 초기화가 유지된다.
   - 첫 정리 상태 로직을 별도 coordinator/reducer로 추출했다면
     picker dismiss, confirmation back, analysis cancel, terminal dismiss,
     skip 전이를 각각 테스트한다.

2. debug build:

   ```powershell
   $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
   ```

3. 수동 런타임 확인:
   - 온보딩 `첫 정리 시작` -> 피커 열기 -> 뒤 배경이 온보딩인지 확인 -> 취소
     -> 같은 화면 유지.
   - 선택 -> 확인 -> back -> 같은 온보딩 화면 유지.
   - 선택 -> 분석 시작 -> 진행 중 취소/back -> 같은 온보딩 화면 유지.
   - 성공, 부분 성공, 전체 실패 각각 terminal 화면이 자동 종료되지 않는지 확인;
     시스템 back으로도 유지되고 `완료/닫기` 후에만 Home으로 가는지 확인.
   - `나중에 하기` -> 피커 없이 Home 이동.
   - Main Home/Collection에서 기존 Organize picker/분석 플로우 smoke test.

## Out of scope

- 피커, 확인, 분석 진행/성공/실패 화면의 시각 디자인 변경
- 분석 repository, 업로드/저장 로직, terminal 결과 매핑 변경
- Main Organize overlay의 animation, selection, permission, consent 정책 변경
- process death 중 실행 중이던 네트워크 분석 작업의 복원/재시작 보장
- 신규 navigation framework 또는 production dependency 도입

## Technical Debt

- none

## Cursor Result
- Changed files: app/.../RecapApp.kt, app/.../RecapStartupViewModel.kt, app/.../RecapNavHost.kt, app/.../OnboardingFirstOrganizeHost.kt, app/.../OnboardingFirstOrganizePhase.kt, feature/onboarding/.../OnboardingRoute.kt, app/src/test/.../RecapStartupViewModelTest.kt, app/src/test/.../OnboardingFirstOrganizePhaseTest.kt, docs/PROJECT.md, docs/ORGANIZE_OVERLAY_NAVIGATION.md
- Build/test: .\gradlew.bat testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN
- Review fixes:
  - `docs/ORGANIZE_OVERLAY_NAVIGATION.md` code fence/경로 제어문자 손상 복구
  - `OnboardingFirstOrganizeHost`에 `modifier: Modifier = Modifier` 추가 후 root `Box`에 적용
- Manual runtime (Validation 3, device SM-S948N):
  - PASS: StartFirstAnalyze → 피커 열기 → 스크림 뒤 온보딩(`첫 정리를 시작해볼까요?`) 유지
  - PASS: 선택 없이 피커 X dismiss → StartFirstAnalyze 복귀 (Home 미이동)
  - PASS: 선택 → 확인 → 시스템 back → StartFirstAnalyze 복귀
  - PASS: 선택 → 동의/정리 → Success terminal 유지(자동 Home 미이동)
  - PASS: terminal에서 시스템 back → 결과 화면 유지(완료 미저장)
  - PASS: terminal `완료` → Main Home 전환
  - PASS: Main Home `업로드` Organize 피커가 Home 위 overlay로 동작
  - SKIPPED (사용자 요청으로 수동 조작 중단): 진행 중 취소/back, PartialFailed/Failed terminal, `나중에 하기` skip, Collection Organize
- Open questions: none

## Codex Review

- Blocking:
  - none
- Nits:
  - 진행 중 취소/back, PartialFailed/Failed, `나중에 하기`, Collection
    Organize의 수동 확인은 사용자 요청으로 중단됐다. 다만 취소는 테스트된
    reducer와 공통 cancel callback, terminal 종류는 공통
    `OrganizeAnalysisStatusRoute`, `나중에 하기`는 단일 완료 callback,
    Collection은 변경되지 않은 Main 내부 overlay 경로를 사용하므로 현재
    코드 검토상 별도 blocking으로 보지 않는다.
- Verdict: DONE
