# HANDOFF.md - RECAP

> Archived: 2026-07-24

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Codex

## Context
- 현재 `MainActivity`가 `MAIN/LAUNCHER`와 `ACTION_SEND`/`ACTION_SEND_MULTIPLE`을 함께 수신하고, `ShareIntakeViewModel`의 결과를 `RecapApp` → `RecapNavHost` → `RecapMainScreen`으로 전달해 MainTabs 위에 `OrganizeRoute`를 오버레이한다.
- 이 결합 때문에 `MainActivity`를 `singleTask`로 두면 실행 중인 앱에 공유 인텐트가 도착해도 confirmation overlay 표시가 안정적이지 않고, `singleTop`으로 두면 `MainActivity` 인스턴스가 중복될 수 있다.
- 공유 이미지 파싱, 형식 필터, 최대 20장 제한, SavedState 복원은 `app/share`의 `ShareImageIntentParser`, `ShareIntakeViewModel`, `PendingShareIntake`에 이미 구현되어 있다.
- 공유 confirmation은 `OrganizeRoute(sharedImages, shareSessionId)`가 `ScreenshotConfirmationScreen`을 표시하고, 필요하면 기존 갤러리 picker로 돌아가 이미지를 추가하도록 구현되어 있다.
- 실제 분석은 현재 `RecapNavHost` 내부에서 얻은 `ScreenshotAnalysisProgressViewModel.startAnalysis(images)`가 시작하며, 시작 직후 Home으로 이동해 진행 상태를 노출한다.
- 이 작업에서 “로그인됨”은 `SessionTokenStore`에 비어 있지 않은 refresh token이 저장된 상태로 정의한다. 공유 수신 Activity에서 네트워크 refresh를 새로 시도하지 않는다.

## Spec
### 1. Activity와 intent-filter 책임 분리

- 외부 공유만 받는 exported `ShareReceiverActivity`를 `:app`에 추가한다.
- `ACTION_SEND`와 `ACTION_SEND_MULTIPLE`의 `image/*` intent-filter를 `MainActivity`에서 제거하고 `ShareReceiverActivity`로 옮긴다.
- `MainActivity`는 `MAIN/LAUNCHER`와 앱 내부 분석 시작 요청만 담당하며 `android:launchMode="singleTask"`로 변경한다.
- `ShareReceiverActivity`는 transient entry point로 사용한다. 동일 Activity가 top인 동안 새 공유가 들어오면 `onNewIntent`에서 새 세션으로 다시 파싱할 수 있도록 `singleTop`으로 선언하고 `setIntent(intent)`도 갱신한다.
- 공유 수신 Activity에는 starting splash theme를 사용하지 않는다. 기존 `Theme.RECAP`, edge-to-edge/light system bar 정책을 적용하되 `MainActivity`의 splash 대기 조건이나 root navigation을 복제하지 않는다.
- 새 production dependency는 추가하지 않는다.

### 2. ShareReceiverActivity 화면과 상태

- 기존 `ShareImageIntentParser`, `ShareIntakeViewModel`, `PendingShareIntake`를 새 Activity 소유로 이동/재사용한다. 이 상태를 더 이상 `MainActivity`, `RecapApp`, `RecapNavHost`, `RecapMainScreen`에 전달하지 않는다.
- Activity의 Compose 진입점은 얇은 route/content 경계로 작성한다.
  - `ShareIntakeViewModel` 상태는 `collectAsStateWithLifecycle()`로 수집한다.
  - 파싱 전/중에는 빈 프레임을 방치하지 말고 최소 loading 상태를 렌더링한다.
  - `PendingShareIntake.Confirmation`이면 기존 `OrganizeRoute(sharedImages, shareSessionId)`를 사용해 바로 `ScreenshotConfirmationScreen`부터 연다.
  - `PendingShareIntake.Unsupported`이면 기존 `UnsupportedShareScreen`을 표시한다.
  - confirmation의 뒤로가기, 상단 back, unsupported 닫기는 pending 세션을 정리하고 `ShareReceiverActivity.finish()`로 외부 공유 진입을 종료한다.
  - “추가”를 누르면 현재 `OrganizeRoute`의 picker/merge 동작을 그대로 유지한다.
- 현재 공유 플로우의 “이미지가 아닌 파일 N개 제거” 및 “최대 20개” 안내는 새 Activity에서도 세션당 한 번만 유지한다. 기존 `RecapToastViewModel`/`RecapToastHost`를 새 Activity의 theme 안에 제공해 기존 앱 토스트 표현을 재사용한다.
- Activity 생성 시 initial intent를 한 번 제출하고, `onNewIntent`에서는 `forceNewSession = true`로 제출한다. 같은 intent가 configuration change로 다시 처리되어 confirmation/분석이 중복되지 않도록 현재 fingerprint/session dedupe와 SavedState 복원을 유지한다.

### 3. 인증 게이트와 시스템 Toast

- confirmation의 “정리 시작”에서 선택 이미지가 확정되면 즉시 Activity를 닫거나 MainActivity를 열지 말고 먼저 `SessionTokenStore.getRefreshToken()`으로 로그인 상태를 확인한다.
- refresh token이 null/blank이면:
  - Android `Toast.makeText` 기반 `Toast.LENGTH_SHORT` 시스템 Toast로 정확히 `로그인을 해주세요.`를 표시한다.
  - 문자열은 `core/design/src/main/res/values/strings.xml`에 새 resource로 추가한다.
  - MainActivity를 열지 않고 분석도 시작하지 않으며 confirmation과 현재 선택을 그대로 유지한다.
- refresh token이 있으면 선택 이미지를 내부 분석 요청 intent로 변환해 MainActivity를 연 뒤 ShareReceiverActivity를 종료한다.
- 인증 확인 중 중복 탭으로 MainActivity 실행/분석 요청이 두 번 발생하지 않도록 submitting 상태 또는 동등한 직렬화 방어를 둔다.
- 시스템 Toast와 `startActivity`/`finish`는 composition 본문에서 직접 실행하지 않는다. ViewModel의 one-shot event(`SharedFlow` 또는 동등한 단발 이벤트)와 Activity의 lifecycle-aware collector, 또는 명시적인 UI callback 경계를 통해 실행한다.

### 4. ShareReceiverActivity → MainActivity 내부 분석 요청 계약

- 외부 `ACTION_SEND*` intent 자체를 MainActivity로 다시 전달하지 않는다. 앱 내부 전용 action과 명시적 component를 쓰는 `SharedAnalysisIntentContract`(명칭은 프로젝트 스타일에 맞게 조정 가능)를 추가한다.
- 내부 요청에는 다음을 포함한다.
  - 중복 소비를 막기 위한 고유 request/session ID
  - 선택 순서를 보존한 `LocalImage`의 `uri`, `displayName`, `dateAddedMillis`
- `LocalImage`에 Parcelable/plugin 의존성을 추가할 필요는 없다. 최대 20장인 현재 제약 안에서 String array/list와 `LongArray` 같은 Bundle 지원 타입으로 encode/decode하고, 배열 길이 불일치·빈 목록·잘못된 URI는 요청 전체를 안전하게 거부한다.
- content URI read grant가 MainActivity의 분석/로컬 복사까지 유지되도록 내부 intent에 `FLAG_GRANT_READ_URI_PERMISSION`과 선택 URI를 담은 `ClipData`를 구성한다.
- 기존 MainActivity를 재사용하기 위해 explicit intent에 `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`을 사용하고 `FLAG_ACTIVITY_NEW_TASK`는 Activity context에서 추가하지 않는다.
- MainActivity는 `onCreate`와 `onNewIntent` 모두에서 내부 action만 decode한다. 일반 launcher intent와 외부 `ACTION_SEND*`는 분석 요청으로 취급하지 않는다.

### 5. MainActivity의 분석 소유권과 Home 복귀

- `ScreenshotAnalysisProgressViewModel`을 `MainActivity`의 Activity scope(`by viewModels()`)에서 한 번 소유하고 `RecapApp`/`RecapNavHost`에 명시적으로 전달한다. `RecapNavHost` 내부의 암묵적 `hiltViewModel()` 획득은 제거해 MainActivity가 받은 내부 요청과 앱 내부 organize가 같은 분석 인스턴스를 사용하게 한다.
- MainActivity의 내부 분석 요청 소비와 “Home으로 이동” 신호를 담당하는 작은 Activity entry state holder/ViewModel을 둔다.
  - request/session ID 기준으로 `onCreate`/`onNewIntent` 및 configuration change 중복 소비를 막는다.
  - 새 요청만 `ScreenshotAnalysisProgressViewModel.startAnalysis(images)`에 전달한다.
  - 새 요청마다 monotonic request ID 또는 동등한 상태를 발행해 root/app/main-tab navigation을 Home으로 복귀시킨다.
- `RecapApp`은 유효한 새 분석 이동 요청이 있고 정상 Main root를 표시할 수 있을 때 root back stack을 `RecapRootRoute.Main`으로 맞춘다.
- `RecapNavHost`는 같은 요청 ID를 기준으로 app back stack을 `AppRoute.MainTabs`로 맞추고, `RecapMainScreen`은 기존 `homeNavigationRequestId` 동작으로 `MainTabRoute.Home`을 표시한다.
- 안정적인 request ID를 key로 한 `LaunchedEffect` 등 단발 effect를 사용하고, recomposition 자체가 분석 재시작이나 navigation 반복을 유발하지 않게 한다.
- 앱 내부 picker에서 정리를 시작하는 기존 경로도 Activity-scoped `ScreenshotAnalysisProgressViewModel`을 사용하고 Home으로 이동하는 현재 동작을 유지한다.
- 정상 로그인/온보딩 완료 사용자의 cold start와 warm start 모두에서:
  - MainActivity가 없으면 하나 생성되어 분석이 시작된다.
  - MainActivity가 이미 실행 중이면 해당 인스턴스의 `onNewIntent`로 요청이 전달되고 새 MainActivity가 생성되지 않는다.
  - Home의 기존 분석 진행 UI가 새 분석 상태를 관찰한다.

### 6. 기존 Main tree의 공유 결합 제거

- `MainActivity`에서 `ShareIntakeViewModel.submitShareIntent`와 관련 field를 제거한다.
- `RecapApp`, `RecapNavHost`, `RecapMainScreen`의 `pendingShareIntake`/`onPendingShareIntakeFinished` parameter와 관련 `LaunchedEffect`, share session overlay, unsupported overlay를 제거한다.
- 일반 앱 내부 organize overlay(`pendingOpenOrganize`, bottom bar/Home/Collection에서 여는 picker)는 그대로 유지한다.
- `ShareImageIntentParser`, 이미지 형식 필터, 최대 선택 수, 공유 세션 SavedState 로직 및 기존 테스트는 새 Activity 흐름에서 계속 재사용하고 불필요하게 다시 작성하지 않는다.

## Files to Touch
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- `app/src/main/java/com/chalkak/recap/ShareReceiverActivity.kt` (new)
- `app/src/main/java/com/chalkak/recap/app/RecapApp.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- `app/src/main/java/com/chalkak/recap/app/MainActivityEntryViewModel.kt` 또는 동등한 좁은 entry state holder (new)
- `app/src/main/java/com/chalkak/recap/app/share/ShareReceiverRoute.kt` 또는 Activity용 route/content 파일 (new)
- `app/src/main/java/com/chalkak/recap/app/share/SharedAnalysisIntentContract.kt` (new)
- `app/src/main/java/com/chalkak/recap/app/share/ShareIntakeViewModel.kt`
- `app/src/main/java/com/chalkak/recap/app/share/PendingShareIntake.kt` (필요한 상태/mapper 조정만)
- `app/src/main/java/com/chalkak/recap/app/ScreenshotAnalysisProgressViewModel.kt` (Activity scope 전달 또는 navigation request 연동에 필요한 최소 조정)
- `core/design/src/main/res/values/strings.xml`
- `app/src/test/java/com/chalkak/recap/app/**` 및 `app/src/test/java/com/chalkak/recap/app/share/**`의 관련 신규/기존 테스트
- `feature/organize/**`는 기존 `OrganizeRoute` 재사용으로 해결되지 않는 작은 API 경계 조정이 있을 때만 수정한다.

## Acceptance Criteria
- 외부 앱의 단일/다중 이미지 공유 대상은 ShareReceiverActivity이며 MainActivity의 manifest에는 `ACTION_SEND*` filter가 없다.
- MainActivity는 `singleTask`이고 launcher 또는 내부 분석 요청으로만 열리며, 실행 중 외부 공유 후 정리를 시작해도 MainActivity 인스턴스가 중복되지 않는다.
- cold start와 warm start 모두 공유 이미지 confirmation이 ShareReceiverActivity에서 표시된다.
- 기존 허용 형식, unsupported 화면, non-image 제거 수, 최대 20장 trim, 이미지 추가/삭제/뒤로가기 동작이 유지된다.
- 미로그인 상태에서 “정리 시작”을 누르면 `로그인을 해주세요.` 시스템 Toast가 나타나고, MainActivity 실행과 분석 시작은 모두 발생하지 않으며 선택 상태가 유지된다.
- 로그인 상태에서 “정리 시작”을 누르면 선택 순서를 유지한 이미지가 정확히 한 번 MainActivity로 전달되고 ShareReceiverActivity는 종료된다.
- 실행 중인 MainActivity는 `onNewIntent`로 내부 요청을 받아 동일 Activity-scoped 분석 ViewModel에서 분석을 시작한다.
- 분석 시작 시 root/app/main-tab navigation이 Main/Home으로 정리되고 기존 Home 분석 진행 UI가 진행 상태를 표시한다.
- configuration change/recomposition 또는 동일 request/session ID 재전달만으로 confirmation 표시, MainActivity 실행, 분석 시작이 중복되지 않는다.
- 일반 앱 내부 organize picker/confirmation/분석 시작 경로에는 회귀가 없다.
- 새 production dependency, raw exception 사용자 노출, 하드코딩 UI 문자열, `Icons.*`/Canvas/텍스트 아이콘 대체가 추가되지 않는다.

## Validation
- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
- 기존 share parser/intake/mapper 테스트를 유지·조정하고 다음 순수 로직 테스트를 추가한다.
  - 내부 분석 intent payload round trip이 이미지 순서를 보존한다.
  - malformed/empty payload를 거부한다.
  - 동일 request ID는 Main entry state holder에서 한 번만 소비된다.
  - 서로 다른 request ID는 각각 새 Home navigation request를 만든다.
  - 미로그인 start 요청은 login-required event만 내고 launch event를 내지 않는다.
  - 로그인 start 요청은 선택 이미지를 보존한 launch event를 한 번만 낸다.
- ADB/mobile-mcp 런타임 검증은 실행하지 않는다. 사용자가 해당 도구 사용을 요청하지 않았으므로, cold/warm task-stack 시나리오는 구현 결과 보고의 수동 확인 항목으로 남긴다.

## Out of scope
- 외부 공유 UI의 시각적 리디자인
- 분석 repository, backend switching, 저장 포맷 또는 Home 진행 컴포넌트 변경
- 로그인 화면으로 자동 이동하거나 ShareReceiverActivity에서 로그인 플로우를 직접 시작하는 기능
- refresh token 유효성을 네트워크로 재검증하거나 만료 세션 복구 정책을 변경하는 작업
- 일반 앱 내부 organize overlay를 별도 Activity로 이전하는 작업
- 분석 작업의 process-death 복구/WorkManager 이전
- 이미지 최대 선택 수 또는 지원 MIME/확장자 정책 변경

## Technical Debt
- none (스코프 밖 항목 없음)

## Cursor Result
- Changed files (Codex CHANGES_REQUESTED 반영):
  - `OrganizeRoute.kt`: `clearSelectionOnComplete` API 추가 (기본 true / 공유 경로 false)
  - `SharedAnalysisRequestStore.kt` (new): 앱 내부 전용 요청 등록소
  - `ShareIntakeViewModel.kt`: Channel 기반 단발 이벤트 + store.register
  - `MainActivityEntryViewModel.kt`: store.consume 없이 온 요청 거부
  - `ShareReceiverRoute.kt`: modifier 경계, `clearSelectionOnComplete = false`
  - tests: `ShareIntakeViewModelTest`, `MainActivityEntryViewModelTest`, `SharedAnalysisRequestStoreTest`, `OrganizeViewModelTest` 재진입/우회/재연결 보강
- Build/test: `.\gradlew.bat :app:testDebugUnitTest` GREEN, `.\gradlew.bat :feature:organize:testDebugUnitTest` GREEN, `.\gradlew.bat :app:assembleDebug` GREEN
- Open questions: none
- Addressed Codex Blocking:
  1. 인앱 성공 경로만 selection clear / 공유 미로그인 유지
  2. 미등록 request ID 거부 (외부 forged intent 우회 차단)
  3. Channel buffer로 config change 중 launch/login 이벤트 보존 + 재시도 가능

## Codex Review
- Blocking: none
- Nits:
  - `SharedAnalysisRequestStore`는 Activity 전환 전에 프로세스가 종료되면 요청을 복원하지 않는다. 이 작업의 out-of-scope인 process-death 복구와 일치하며 현재 cold/warm task-stack 요구에는 영향이 없다.
- Validation:
  - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest` GREEN (`BUILD SUCCESSFUL in 4s`)
  - Cursor의 `:app:assembleDebug` GREEN 결과 확인
  - `git diff --check` GREEN
  - ADB/mobile-mcp는 handoff 지침과 사용자 요청 범위에 따라 실행하지 않음
- Verdict: DONE
