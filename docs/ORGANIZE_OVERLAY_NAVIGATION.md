# ORGANIZE_OVERLAY_NAVIGATION.md - Organize 오버레이 네비게이션

이 문서는 스크린샷 피커 → 확인 화면 구간의 **의도**, **현재 구현 구조**, **back/애니메이션 동작**을 정리한다.
관련 handoff 아카이브(`docs/handoff/archive/2026-07-11-ios26-screenshot-picker.md`)는 피커 UI 스펙 중심이며, 본 문서는 **네비게이션/전환 계층**에 초점을 둔다.

## 사용자가 원했던 것

1. `ScreenshotPicker`가 Home / Collection을 **가리지 않고 그 위에 오버랩**되어야 한다.
   (전체 화면 route로 밀어내면 안 된다.)
2. 시트에서 체크(확인) 시 **시트가 내려가는 동안** 뒤에서 `ScreenshotConfirmationScreen`이 보여야 한다.  
   (시트 hide가 끝난 뒤에야 Confirmation이 뜨는 순차 전환이 아니다.)
3. 체크 순간에 Home/Collection이 **불투명 컷으로 사라지면 안 된다.**  
   Confirmation은 fade-in으로 부드럽게 덮는다.
4. 시트의 **올라오기 / 내려가기** Material 애니메이션이 유지되어야 한다.
5. Confirmation에서 **시스템 back / 툴바 back**은 시트를 다시 열지 않고 **바로 Home/Collection으로** Organize를 종료한다.
6. 시트가 dismiss되어 Organize가 닫히면 **선택 항목을 초기화**한다.  
   (Confirmation으로 넘어갈 때는 선택을 유지한다.)

## 시스템 구조 (현재)

Organize는 루트 `AppRoute` destination이 **아니다**.  
`RecapMainScreen` 위의 **조건부 오버레이**로 올라간다.

```text
RecapNavHost (AppRoute NavDisplay)
└── AppRoute.MainTabs
    └── RecapMainScreen
        ├── Scaffold
        │   ├── RecapMainTabNavHost  (Home / Collection)
        │   └── RecapBottomBar
        └── if (showOrganize) OrganizeRoute   ← 오버레이
            ├── ScreenshotConfirmationScreen  (showConfirmation)
            └── ScreenshotPicker (showScreenshotPicker, ModalBottomSheet Dialog)
```

### 계층별 역할

| 계층 | 파일 | 역할 |
|------|------|------|
| 루트 Nav | `app/.../RecapNavHost.kt`, `AppRoute.kt` | MainTabs 등 앱 전역 route. **Organize route 없음** |
| 메인 셸 | `app/.../RecapMainScreen.kt` | `showOrganize` 플래그로 Organize 오버레이 on/off. 탭 스택은 유지 |
| Organize host | `feature/organize/.../OrganizeRoute.kt` | 시트/Confirmation 로컬 UI 상태, 전환, back, 선택 초기 |
| 피커 | `.../ScreenshotPicker.kt` | Material3 `ModalBottomSheet` (별도 Dialog 윈도우) |
| 확인 | `.../ScreenshotConfirmationScreen.kt` | 선택 확인 UI. 자체 Nav 없음 |
| 상태 | `OrganizeViewModel` / `OrganizeAction` | 선택 목록. `ClearSelection`으로 초기화 |

### Organize 내부 상태 (NavDisplay 아님)

예전에는 Organize 내부에 `rememberNavBackStack(Selection/Confirmation)` + `NavDisplay`가 있었다.  
시트 Dialog + Nav 전환이 겹치며 애니메이션/back이 깨져, 현재는 **복원 가능한 destination + 로컬 전환 상태**로 관리한다.

- `destination`: `rememberSaveable` 기반의 안정 상태 (`Selection`, `Confirmation`, `Exiting`)
- `showScreenshotPicker`: 스크린샷 피커 composition 여부. destination과 분리해 Confirmation fade-in 중에도 hide 완료 전까지 피커를 유지
- `suppressPickerDismiss`: 확인 이동 / 툴바 X animated hide 중 Material `onDismissRequest` 무시
- `isAnimatedExitRunning`: 툴바 X hide coroutine이 현재 composition에서 진행 중인지 표시
- `sheetState`: `rememberModalBottomSheetState(skipPartiallyExpanded = true)`
- 시트 종료 헬퍼:
  - `dismissScreenshotPickerAndExit()` — 툴바 X: hide 후 Organize 종료
  - `exitOrganizeImmediately()` — Confirmation back / Material onDismissRequest(이미 hide됨) / 선택 비움

## 전환 시퀀스

### 진입

1. BottomBar organize / 탭의 organize 콜백 → `showOrganize = true`
2. `OrganizeRoute` composition, host Scaffold는 **항상 `Color.Transparent`**
3. `showScreenshotPicker = true` → `ModalBottomSheet` 상승 애니메이션
4. 시트 뒤로 Home/Collection이 비친다 (host가 불투명하지 않음)

### 체크 → Confirmation (동시 전환)

1. `suppressPickerDismiss = true`
2. `destination = Confirmation` → Confirmation **fade-in** 시작 (activity 레이어)
3. 동시에 `sheetState.hide()` → 시트 **하강** (Dialog 레이어)
4. hide 완료 후 `showScreenshotPicker = false`로 피커 dispose
5. 이 구간에서 시트 뒤로 Confirmation이 보이며, Home은 Confirmation alpha에 따라 덮인다

### Confirmation → 종료 (back)

- 시스템 back (`BackHandler`) / 툴바 back → `ClearSelection` + `showOrganize = false`
- **시트를 다시 열지 않음** → 바로 아래 Home/Collection

### Confirmation → 시트 재오픈

- `더 추가` (`onAddMoreClick`)만 `navigateBackToPicker()`
  → `destination = Selection`, `showScreenshotPicker = true` (피커 재상승)

### 시트 dismiss → 종료

- **툴바 X**: `dismissScreenshotPickerAndExit()` — `sheetState.hide()` 완료 후 `ClearSelection` + `onNavigateBack()`.
  hide 중 Material `onDismissRequest`는 `suppressPickerDismiss`로 무시한다.
  (`onCloseClick`과 Modal `onDismissRequest`를 같은 람다로 두면 hide 없이 composition이 제거되어 하강 애니메이션이 사라진다.)
- **툴바 X hide 중 재생성**: `Exiting` 복원 시 이전 composition에서 취소된 hide를 재생하지 않고 선택을 비운 뒤 Organize를 즉시 종료한다.
- **스크림 / 스와이프 / 시트 back**: Material이 이미 hide 애니메이션을 끝낸 뒤 `onDismissRequest` 호출 → `exitOrganizeImmediately()`  
  (`suppressPickerDismiss`가 아닐 때만. hide를 다시 호출하지 않음.)

### 정리 시작

- 선택 목록을 캡처한 뒤 `ClearSelection` → `onOrganizeComplete`  
- `RecapMainScreen`이 `showOrganize = false` 후 상위(분석 시작 등)로 전달

## Predictive back이 Confirmation에서 안 보이는 이유

**의도적으로 “predictive back를 끈” 설정은 아니다.**  
오버레이 + `BackHandler` 구조의 **부수 효과**다.

| 비교 | Collection 탭 등 | ScreenshotConfirmation |
|------|------------------|-------------------------|
| 스택 | `NavDisplay` destination | 로컬 `showConfirmation` |
| back API | `NavDisplay` / `NavigationBackHandler` (progress 전달) | `androidx.activity.compose.BackHandler` |
| 제스처 preview | `predictivePopTransitionSpec` 등으로 가능 | progress를 받는 경로 없음 → preview 없음 |
| 완료 시 | pop 애니메이션 | `dismissOrganize()`로 플래그 off (한 번에 닫힘) |

즉 Confirmation back은 “이전 Nav entry를 엿보는” 시스템이 아니라,  
**오버레이를 즉시 끄는 콜백**이다.  
나중에 predictive preview가 필요하면 `PredictiveBackHandler` / `NavigationBackHandler`로 progress를 받아 Confirmation fade/scale을 직접 구동해야 한다.

## 선택 시트 predictive back

`ScreenshotPicker`는 Material3 `ModalBottomSheet`를 사용한다.
predictive back 시 Surface/`contentPredictiveBackScaling`이 서로 다른 scale를 적용해,  
풀블리드 그리드 하단에 빈 띠가 생길 수 있다. 이 이슈는 별도 최소 수정으로 다룰 예정이며,  
시트 host 전체를 교체하는 방식으로는 처리하지 않는다.

## 선택 초기 규칙

`OrganizeAction.ClearSelection` 사용 시점:

- 시트 dismiss로 Organize 종료
- Confirmation back으로 Organize 종료
- Confirmation에서 선택이 비어 Organize 종료
- 정리 시작 직후 (전달용 리스트는 먼저 캡처)

유지하는 경우:

- 시트 → Confirmation 전환 중/후
- Confirmation `더 추가`로 시트 복귀

`ComponentGardenScreen.reduceGardenAction`에도 동일 분기가 있다.

## 핵심 파일

- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt` — `showOrganize` 오버레이 host
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt` — Organize를 AppRoute에 두지 않음
- `app/src/main/java/com/chalkak/recap/app/AppRoute.kt` — Organize object 없음
- `feature/organize/.../OrganizeRoute.kt` — 전환/back/시트 생명주기
- `feature/organize/.../ScreenshotPicker.kt`
- `feature/organize/.../ScreenshotConfirmationScreen.kt`
- `feature/organize/.../OrganizeContract.kt` / `OrganizeViewModel.kt`

## 구현 시 주의할 점

1. Organize host Scaffold를 Confirmation 진입 시 불투명 `background`로 바꾸면 Home이 즉시 컷된다. **host는 투명 유지**, 불투명은 Confirmation `Surface`만.
2. 피커 composition을 destination에 묶지 말고 `showScreenshotPicker`로 분리한다. Confirmation을 올린 뒤에도 hide 애니메이션이 끝나기 전까지 피커를 유지해야 한다.
3. `sheetState.hide()` 후 ModalBottomSheet가 `onDismissRequest`를 호출할 수 있다. 확인 이동 / 툴바 X hide 중에는 `suppressPickerDismiss`로 Organize 전체가 닫히지 않게 한다. 툴바 X는 `onCloseClick` → hide 후 종료이고, Modal `onDismissRequest`는 Material hide 완료 후 종료만 담당한다.
4. 강제 `sheetState.show()` `LaunchedEffect`를 destination과 겹치면 피커 등장 애니메이션이 깨질 수 있다. 재오픈은 `showScreenshotPicker = true`로 ModalBottomSheet를 다시 composition하는 방식을 쓴다.
5. Organize를 다시 `AppRoute`로 올리면 Home 위 오버랩 요구가 깨지기 쉽다. 오버레이 host는 MainTabs 쪽에 둔다.
6. `destination` 변경으로 피커 composition을 즉시 제거하지 않는다. 일반 전환에서는 `sheetState.hide()` 완료 후 `showScreenshotPicker = false`로 변경한다.
7. 구성 변경으로 재생성될 때는 복원된 `destination`을 기준으로 시트 초기 composition을 결정한다. `Confirmation`은 시트 없이 확인 화면으로, `Exiting`은 선택을 비우고 Organize 종료로 수렴한다.
