# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 현재 `ScreenshotSelectionScreen`은 `OrganizeUiState`와 `OrganizeAction`을 받아 로딩/빈 상태, 3열 스크린샷 그리드, 1~20장 선택, 선택 순서 배지, 취소와 다음 진행을 제공한다.
- `OrganizeViewModel`은 `LocalScreenshotDataSource.queryAllScreenshots()` 결과를 그대로 `availableScreenshots`에 제공한다. 데이터 소스는 `MediaStore.Images` 중 `RELATIVE_PATH = "DCIM/Screenshots/"`인 항목만 `DATE_ADDED` 내림차순으로 조회하므로, 새 UI를 위해 데이터 계층을 변경할 필요가 없다.
- 사용자가 제공한 레퍼런스 이미지는 iOS 26 사진 선택 시트 형태다. 화면 상단 약 12~13%를 남긴 부분 높이 시트, 큰 상단 모서리, 좌측 닫기 원형 버튼, 중앙 `사진 / 모음` 캡슐, 우측 완료 원형 버튼, 헤더 바로 아래부터 시작하는 간격이 매우 좁은 3열 정사각 썸네일 그리드로 구성된다.
- [Apple HIG Sheets](https://developer.apple.com/design/human-interface-guidelines/sheets)의 지침은 단일 화면 시트에서 Close/Cancel을 선행 가장자리, Done을 후행 가장자리에 배치하고, 부분 높이 시트는 detent로 표현하도록 안내한다. [WWDC25 Build a SwiftUI app with the new design](https://developer.apple.com/videos/play/wwdc2025/323/)과 [Apple HIG Materials](https://developer.apple.com/design/human-interface-guidelines/materials)는 iOS 26 부분 높이 시트와 기능/내비게이션 레이어의 Liquid Glass 사용을 안내하지만 정확한 고정 point 수치는 공개하지 않으므로, 동작/계층은 공식 지침을 따르고 치수는 제공 이미지를 시각 기준으로 삼는다.
- 이 컴포넌트는 screen 전체나 ViewModel을 소유하지 않는 feature-level organism이다. 기존 `core:design` 컴포넌트/테마/아이콘 자산을 재사용하고 데이터와 callback만 받는다.

## Spec

1. 별도 파일에 공개 composable `ScreenshotPicker`를 추가한다.
   - 권장 API: `uiState: OrganizeUiState`, `onAction: (OrganizeAction) -> Unit`, `onDismissRequest: () -> Unit`, `onConfirmClick: () -> Unit`, `modifier: Modifier = Modifier`를 받는다.
   - 기존 `RecapActionBottomSheet`를 변형하지 말고 Material 3 `ModalBottomSheet` 기반의 독립 컴포넌트로 구현한다.
   - 호출자가 표시 여부를 소유한다. 바깥 영역 탭, 시스템 Back, 아래로 스와이프, 좌측 닫기 버튼은 모두 `onDismissRequest`로 귀결한다.
   - `dragHandle = null`로 레퍼런스처럼 별도 grabber를 노출하지 않는다.
   - 부분 높이 시트로 시작하며 compact portrait 기준 화면 높이의 약 88%를 차지한다. 상단 모서리는 약 40.dp의 큰 radius를 사용하고 하단은 화면 아래에 이어지게 한다. 시스템/navigation bar inset을 침범하지 않도록 한다.
2. 피커 내부를 `ScreenshotPickerContent` 같은 순수 content composable로 분리해 Preview에서 modal host 없이 렌더링할 수 있게 한다.
   - content composable도 `modifier: Modifier = Modifier`를 제공하고 ViewModel/Context 데이터 조회를 직접 하지 않는다.
   - `Box` 안에 그리드와 상단 toolbar를 겹쳐 구성한다. 그리드는 toolbar 높이만큼 top content padding을 가져 첫 행이 toolbar 아래에서 시작하되, 스크롤 시 이미지가 glass toolbar 아래로 지나가며 blur source가 되게 한다.
3. 상단 toolbar는 레퍼런스를 다음과 같이 재현한다.
   - 좌측: 기존 `R.drawable.ic_close_24`를 사용하는 최소 48.dp 원형 닫기 버튼.
   - 중앙: `사진`이 선택된 상태인 `사진 / 모음` pill. `사진`은 내부 선택 캡슐로 강조한다.
   - 우측: 기존 `R.drawable.ic_check_24`를 사용하는 최소 48.dp 원형 완료 버튼.
   - 완료 버튼은 `uiState.canProceed == false`이면 disabled 모양과 semantics를 갖고 callback을 호출하지 않는다. true이면 `onConfirmClick`을 한 번 호출한다.
   - Close/Done 배치는 Apple sheet 관례에 맞추되 RTL에서도 leading/trailing 의미가 유지되도록 `start`/`end` 기반으로 구현한다.
   - `사진 / 모음`은 이번 작업에서는 iOS 26 레퍼런스를 재현하기 위한 정적 표시다. `사진`만 선택 상태이며 `모음` 탭 전환, 앨범/컬렉션 탐색, 별도 상태는 만들지 않는다. 동작하지 않는 텍스트를 버튼 semantics로 노출하지 않는다.
4. Liquid Glass/Haze는 기능 레이어에만 제한적으로 적용한다.
   - 시트 본문/썸네일에는 glass 효과를 입히지 않는다.
   - `rememberHazeState`, 그리드의 `hazeSource`, toolbar 및 원형/캡슐 control의 `hazeEffect`를 사용해 뒤의 이미지가 비치고 blur/tint/noise가 적용되게 한다.
   - 기존 `RecapBottomBar`의 Haze 구현 방식(`CupertinoMaterials`, `blurEffect`, theme 기반 tint, border/shadow)을 참고하되 bottom bar token을 직접 재사용하지 말고 이 컴포넌트의 private token object에 필요한 수치만 둔다.
   - hardcoded hex color를 추가하지 않는다. `MaterialTheme.colorScheme`, 기존 `core:design/theme` token, shape/typography를 사용한다.
5. 본문 상태와 선택 기능은 기존 화면과 동등하게 유지한다.
   - `isLoading`: 중앙 progress indicator.
   - 빈 목록: 기존 `organize_selection_empty` 안내 문구.
   - 목록: `LazyVerticalGrid(GridCells.Fixed(3))`, 정사각형 셀, `ContentScale.Crop`, URI key 사용.
   - 레퍼런스처럼 그리드는 시트 좌우 끝까지 차고 셀 간격은 약 2.dp만 둔다. 썸네일 자체에는 큰 카드 radius나 외곽 여백을 두지 않는다.
   - 셀 탭은 `OrganizeAction.ToggleSelection(uri)`를 전달한다.
   - 선택된 셀은 기존 화면과 동일하게 `uiState.selectionOrder(uri)`의 1-based 순서 배지를 표시한다. 최대 20장 제한과 `showMaxSelectionReached` 상태 처리는 기존 ViewModel/host 책임을 유지한다.
   - 이미지 로딩은 기존과 동일하게 Coil `AsyncImage`를 사용한다.
6. 접근성과 리소스 규칙을 지킨다.
   - 닫기/완료 버튼 및 이미지 선택 상태에 의미 있는 content description/semantics를 제공한다.
   - 모든 조작 대상은 최소 48.dp touch target을 확보한다.
   - 새 UI 텍스트와 content description은 `core/design/src/main/res/values/strings.xml`에 추가한다.
   - 새 `Icons.*`, Canvas/텍스트 아이콘을 만들지 않는다. 기존 close/check vector drawable을 재사용한다.
7. Preview를 `RECAPTheme(dynamicColor = false)`로 감싸고 iPhone과 유사한 compact portrait 크기(권장 393 x 852)에 작성한다.
   - populated + selected 상태 Preview.
   - empty 상태 Preview.
   - 가능하면 loading 상태도 Preview해 glass toolbar가 모든 상태에서 유지되는지 확인한다.
8. 이 작업에서는 새 피커를 `OrganizeRoute`에 연결하거나 기존 `ScreenshotSelectionScreen`을 교체하지 않는다. 두 구현은 병존하며, 호출부 전환은 후속 작업에서 결정한다.

## Files to Touch

- Add: `feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotPicker.kt`
- Modify: `feature/organize/build.gradle.kts` — 현재 version catalog의 `chrisbanes-haze`, `chrisbanes-haze-blur`, `chrisbanes-haze-blur-materials`를 이 모듈에 추가한다. 새 라이브러리/버전은 도입하지 않는다.
- Modify: `core/design/src/main/res/values/strings.xml` — `사진`, `모음`, 닫기/선택 완료 등 새 접근성 문자열만 추가한다.
- Do not modify: `ScreenshotSelectionScreen.kt`, `OrganizeRoute.kt`, `OrganizeContract.kt`, `OrganizeViewModel.kt`, `LocalScreenshotDataSource.kt` unless compilation requires a minimal signature/import adjustment; such a need must be recorded in Cursor Result.

## Acceptance Criteria

- [ ] 새 `ScreenshotPicker`가 기존 전체 화면과 별개로 컴파일되며, ViewModel을 직접 참조하지 않고 `OrganizeUiState`/callbacks로 동작한다.
- [ ] 시트가 compact portrait에서 화면의 약 88% 높이로 표시되고, 약 40.dp의 큰 상단 모서리와 no-drag-handle 형태로 레퍼런스의 iOS 26 partial sheet 실루엣을 재현한다.
- [ ] toolbar가 좌측 close, 중앙 `사진 / 모음` pill, 우측 check 순서이며 각 액션의 touch target이 48.dp 이상이다.
- [ ] `사진`만 선택된 정적 segment이고 `모음`을 눌러 생기는 가짜/미완성 동작이 없다.
- [ ] check는 선택 0장일 때 비활성화되고, 1~20장일 때 활성화되어 `onConfirmClick`을 호출한다.
- [ ] loading/empty/populated 상태가 렌더링되며 populated 상태는 `DCIM/Screenshots/`에서 기존 데이터 소스가 제공한 항목만 최신순으로 3열 표시한다.
- [ ] 썸네일 탭이 `ToggleSelection`을 전달하고 선택 순서 배지가 기존 화면과 동일하게 표시된다.
- [ ] grid는 좌우 outer padding 없이 약 2.dp 간격의 정사각 Crop 썸네일로 표시되며 스크롤된다.
- [ ] Haze source/effect가 toolbar/controls의 기능 레이어에만 적용되고, 스크롤하는 이미지가 glass 뒤에서 blur/tint되며 목록 본문 자체에는 glass가 적용되지 않는다.
- [ ] 새 텍스트가 string resource에 있고, 새 Material `Icons.*`/Canvas/텍스트 아이콘이나 hardcoded hex color가 없다.
- [ ] populated/empty Preview가 `RECAPTheme`로 감싸져 있으며 가능하면 loading Preview도 제공된다.
- [ ] 기존 `ScreenshotSelectionScreen`과 `OrganizeRoute` 동작은 변경되지 않는다.

## Validation

1. `rg -n "Icons\\.|Color\\(0x" feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotPicker.kt` — 새 Material icon/hardcoded hex color가 없어야 한다.
2. `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest`
3. `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
4. Android Studio Preview에서 populated/empty/loading 상태를 확인한다: 시트 높이/상단 radius, toolbar 정렬, disabled/enabled check, 3열 grid, 선택 순서 배지, Haze가 이미지 위 toolbar에만 적용되는지 시각 확인한다.

## Out of scope

- `OrganizeRoute`에 새 피커를 연결하거나 기존 선택 화면을 제거/교체하는 작업.
- `모음` 탭 전환, 앨범/컬렉션 목록, 다른 이미지 폴더 탐색.
- 사진 권한 요청/거부/부분 권한 UI와 권한 예외 처리. 이번 작업은 사용자가 요청한 대로 권한이 허용되었다고 가정한다.
- MediaStore query, 정렬 정책, 최대 선택 수, confirmation screen, OCR/정리 흐름 변경.
- iPad/tablet 전용 page/form sheet 또는 landscape 전용 레이아웃.
- Apple의 실제 PhotosPicker/System UI를 호출하는 플랫폼 연동. Android Compose로 시각/동작을 재현한다.

## Technical Debt

- None for this task. 권한 예외 처리는 사용자 요청에 따라 명시적으로 후속 범위로 남긴다.

## Cursor Result

- Changed files: `feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotPicker.kt`
- Build/test: `rg Icons.|Color(0x` on file — no matches; `.\gradlew.bat :feature:organize:testDebugUnitTest` GREEN; `.\gradlew.bat assembleDebug` GREEN
- Open questions: none
- Codex CHANGES_REQUESTED 반영:
  1. 미선택 썸네일 content description에 1-based grid index 전달 (`스크린샷 N`). 선택 시에는 기존처럼 selection order (`선택된 스크린샷 N`).
  2. populated Preview를 `mock_home_screenshot_*` drawable model로 교체해 Coil이 Preview에서 실제 이미지를 로드하도록 함. empty/loading Preview 유지. Studio Preview에서 3열 crop·선택 배지·toolbar 겹침·Haze blur source를 확인할 수 있는 근거를 마련함.
- Notes: 중앙 타이틀은 사용자 override(`스크린샷 선택`) 유지. `ScreenshotSelectionScreen` / `OrganizeRoute` 미변경.

## Codex Review

- Blocking: none
- Nits: none
- Accepted user override: 중앙 `사진 / 모음` pill 대신 `organize_screenshot_selection_title`을 표시한 변경은 사용자가 직접 변경한 의도된 최종 UI로 승인했다.
- Validation: `git diff --check` GREEN; static scan for `Icons.*`/`Color(0x` no matches; `:feature:organize:testDebugUnitTest` GREEN (Cursor evidence); latest `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug` GREEN.
- Positive patterns: `itemsIndexed`의 1-based index로 미선택 썸네일 접근성을 수정했고, drawable 기반 Preview로 3열 crop/선택 배지/Haze source 확인이 가능하다. ViewModel 비의존 organism, URI key, theme/resource/vector asset 규칙을 유지했다.
- Verdict: DONE
