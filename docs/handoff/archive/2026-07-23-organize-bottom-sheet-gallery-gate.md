# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Codex

## Context
- `RecapMainScreen`의 하단 업로드/추가 버튼은 클릭 즉시 `showOrganize = true`로 바뀌며, 클릭 콜백 자체에는 비동기 대기나 `RecapButton`이 없다.
- `if (showOrganize) { OrganizeRoute(...) }` 구조 때문에 진입할 때 `OrganizeRoute`, Material3 `ModalBottomSheet`의 별도 Dialog window, `ScreenshotPickerContent`, `LazyVerticalGrid`, 화면 내 `AsyncImage`가 함께 최초 구성·측정된다.
- Material3 `ModalBottomSheet`는 콘텐츠 측정으로 sheet anchor가 생긴 뒤 내부 `LaunchedEffect { sheetState.show() }`를 실행한다. 현재는 상승 애니메이션 시작 전의 Composition/Layout 비용이 커서 한 박자 늦게 반응하는 것처럼 보인다.
- SM-S948N(`R5KL20GZVAP`) debug 런타임의 warm reopen 기준 ADB `gfxinfo`에서 61 frames 중 8 janky frames(13.11%), 8 slow UI-thread frames, 약 129~150ms 프레임이 관찰됐다. GPU 99th percentile은 6ms이고 slow bitmap upload는 0건이어서 주 병목은 GPU보다 UI 스레드 최초 구성/측정이다.
- 스크린샷 MediaStore 조회는 `LocalScreenshotDataSource.queryAllScreenshots()` 내부 `Dispatchers.IO`에서 실행되므로 메인 스레드 직접 블로킹 원인은 아니다.
- 기존 Organize overlay/navigation/back/confirmation 전환 계약은 `docs/ORGANIZE_OVERLAY_NAVIGATION.md`를 유지한다. 현재 런타임 코드는 `skipPartiallyExpanded = false`이며, 이번 작업에서 partial/expanded 동작을 변경하지 않는다.

## Spec
1. 기존 `ModalBottomSheet`와 `RecapMainScreen`의 조건부 Organize overlay 구조를 유지한다. 커스텀 sheet, `BottomSheetScaffold`, AppRoute destination으로 교체하지 않는다.
2. `ScreenshotPicker`의 최초 sheet Composition을 다음 두 단계로 분리한다.
   - **Lightweight shell:** 현재와 동일한 고정 sheet 높이, surface/shape, toolbar, 선택 개수, loading/empty용 기본 body 영역을 구성한다. 이 단계에서는 `LazyVerticalGrid`와 각 `AsyncImage`를 Composition에 넣지 않는다.
   - **Gallery body:** 최초 sheet shell의 anchor 측정이 끝나 `show()` entrance가 시작되는 시점에 현재 `LazyVerticalGrid` 및 이미지 아이템을 한 번 활성화해 상승 애니메이션과 이미지 로딩을 동시에 진행한다.
3. gallery 활성화 조건은 `SheetState`의 상태를 `snapshotFlow` 등 lifecycle-safe한 방식으로 관찰해 `targetValue != SheetValue.Hidden`이 되는 시점으로 정의한다. entrance animation 종료를 기다리거나 임의의 고정 `delay(...)`로 타이밍을 맞추지 않는다.
4. gallery가 한 번 활성화되면 해당 `ScreenshotPicker`가 Composition에서 제거될 때까지 활성 상태를 유지한다. partial/expanded 드래그나 uiState 갱신마다 grid를 dispose/recreate하지 않는다.
5. production `ScreenshotPicker`에서는 위 gate를 적용하되, `ScreenshotPickerContent` 단독 Preview는 populated/empty/loading 상태를 계속 직접 확인할 수 있어야 한다. 필요하면 gallery 표시 여부를 기본값이 있는 파라미터로 분리한다.
6. sheet shell과 gallery 전환 시 전체 sheet 높이, toolbar 위치, 스크림, corner shape가 바뀌거나 점프하지 않아야 한다. gallery 활성화 전에는 기존 loading indicator 또는 동일 디자인 토큰을 사용하는 가벼운 placeholder를 표시하며 새 사용자 문자열은 추가하지 않는다.
7. `uiState.isLoading`, empty, populated, selection, max-selection toast, confirm button 등장, long press/drag selection 로직은 기존 동작을 유지한다.
8. 다음 Organize 전환 계약을 보존한다.
   - Home/Collection 위 overlay 및 배경 노출
   - partial/expanded sheet 동작과 Material 상승/하강 애니메이션
   - 툴바 X, scrim, swipe, system back 종료 및 선택 초기화
   - 선택 완료 시 Confirmation fade-in과 sheet hide 동시 진행
   - Confirmation의 `더 추가`에서 picker 재오픈
9. `RecapButton`, `RecapBottomBar`, `LocalScreenshotDataSource`의 구현을 변경하지 않는다. 새 production dependency를 추가하지 않는다.

## Files to Touch
- `feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotPicker.kt`
- 필요한 경우에만 `feature/organize/src/androidTest/java/com/chalkak/recap/feature/organize/...`의 targeted Compose UI test

## Acceptance Criteria
- 하단 업로드/추가 버튼을 누르면 빈 입력 지연 뒤 갑자기 움직이는 대신, 가벼운 sheet shell과 scrim의 상승이 즉시 시작된다.
- 최초 shell/anchor 측정 전에는 `LazyVerticalGrid`/`AsyncImage` gallery가 Composition되지 않고, entrance가 시작될 때 한 번만 활성화되어 상승 애니메이션과 이미지 로딩이 동시에 진행된다.
- gallery 표시 전후 sheet 외곽 높이와 toolbar 위치에 시각적 점프가 없다.
- populated, empty, loading 상태가 기존과 동일하게 표시되고 populated 상태에서 선택/해제, long press, drag selection, 최대 선택 toast, 선택 완료 버튼이 정상 동작한다.
- sheet의 partial/expanded, swipe/scrim/back/X dismiss, Confirmation 전환 및 `더 추가` 재오픈 동작이 기존 계약과 동일하다.
- `RecapButton.kt`, `RecapBottomBar.kt`, `LocalScreenshotDataSource.kt`는 변경되지 않는다.
- 동일 기기·동일 debug build의 warm reopen ADB 비교에서 baseline의 129~150ms급 UI-thread 프레임이 재현되지 않고, `gfxinfo` histogram에 100ms 이상 프레임이 없어야 한다. janky frame 비율도 baseline 13.11%보다 낮아야 한다.
- debug build와 기존 unit tests가 통과한다.

## Validation
1. 정적 확인
   - diff가 `Files to Touch` 범위에 한정됐는지 확인한다.
   - gallery gate에 entrance 종료 대기나 임의 `delay`가 없고, `targetValue`가 non-hidden이 될 때 활성화된 gallery가 sheet lifetime 동안 유지되는지 확인한다.
2. 빌드/테스트
   - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`
   - `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :feature:organize:testDebugUnitTest`
3. mobile-mcp 런타임 확인
   - Home과 Collection 각각에서 하단 업로드/추가 버튼을 눌러 sheet shell의 상승 시작, gallery 표시, 배경 overlay를 확인한다.
   - partial↔expanded drag, 이미지 선택/해제, X/scrim/swipe/system back, 선택 완료→Confirmation, Confirmation→더 추가 재오픈을 확인한다.
4. ADB 프레임 비교 (`R5KL20GZVAP`; 다른 단일 연결 기기를 쓰면 해당 serial로 대체)
   - `adb devices`
   - sheet를 닫고 warm 상태에서 `adb -s R5KL20GZVAP shell dumpsys gfxinfo com.chalkak.recap reset`
   - mobile-mcp로 하단 업로드/추가 버튼을 한 번 누르고 entrance/gallery 표시가 안정될 때까지 기다린다.
   - `adb -s R5KL20GZVAP shell dumpsys gfxinfo com.chalkak.recap`
   - janky frame 비율과 histogram의 100ms 이상 bin을 baseline과 비교해 Cursor Result에 기록한다.

## Out of scope
- Dialog 없는 custom anchored bottom sheet 또는 `BottomSheetScaffold`로의 구조 전환
- Organize를 Navigation3 `AppRoute` destination으로 변경
- sheet motion spec/duration, partial 높이, expanded 높이 또는 디자인 변경
- 스크린샷 MediaStore query, Coil decode/cache 정책 최적화
- `RecapButton`/Popup 성능 개선 또는 공통 design component 리팩터링
- Baseline Profile/Macrobenchmark 모듈 도입

## Technical Debt
- none

## Cursor Result
- Changed files: `feature/organize/src/main/java/com/chalkak/recap/feature/organize/ScreenshotPicker.kt`
- Implementation notes:
  - 최초 Composition은 shell만 유지하고, `snapshotFlow { targetValue != Hidden }.first { it }`로 show() 시작 직 `showGalleryBody`를 한 번만 latch → 상승 중 이미지 로딩 시작
  - (피드백 반영) entrance settle(`!isAnimationRunning`) 대기 제거. settle 대기는 시트 완전 상승 후 로딩처럼 보임
  - `ScreenshotPickerContent(showGalleryBody: Boolean = true)`로 Preview는 기존처럼 gallery 직접 표시, production만 gate 적용
  - populated이면서 gallery 미활성 시 기존 `CircularProgressIndicator` placeholder 사용 (새 문자열 없음)
- Build/test: `.\gradlew.bat assembleDebug` GREEN, `.\gradlew.bat :feature:organize:testDebugUnitTest` GREEN
- Runtime (mobile-mcp, `R5KL20GZVAP`):
  - Home/Collection 업로드 → sheet shell+scrim+gallery overlay 확인
  - partial↔expanded, 선택(1/20)+확인 CTA, Confirmation 전환, 더 추가 재오픈, system back discard confirm, X/scrim dismiss 확인
- gfxinfo warm reopen (`R5KL20GZVAP`): 72 frames, janky 6 (8.33%, baseline 13.11%), 99th 81ms, histogram 100ms+ bin = 0 (baseline 129~150ms 미재현), Slow UI thread 6
- Follow-up: gallery gate를 settle → show 시작 시점으로 완화. 재측정은 리뷰 전 선택
- Open questions: none

## Codex Review
- Blocking: none
- Nits: none
- Verdict: DONE
- Review notes:
  - 사용자가 최신 요구를 "entrance animation 시작과 gallery 로딩을 동시에 진행"으로 확인하여, 기존 handoff의 settle 이후 활성화 문구를 최신 요구로 정정했다.
  - `showGalleryBody`는 picker Composition lifetime에 귀속된 단방향 latch이고, `snapshotFlow(...).first`가 non-hidden target 최초 1회만 소비하므로 drag/uiState 변화로 grid가 재생성되지 않는다.
  - 변경은 허용된 `ScreenshotPicker.kt`에 한정됐고, Preview는 `showGalleryBody = true` 기본값으로 populated/empty/loading 직접 렌더링을 유지한다.
  - Cursor의 `assembleDebug`, `:feature:organize:testDebugUnitTest`, mobile-mcp 플로우 및 ADB gfxinfo 결과를 검토 근거로 인정했으며 동일 검증은 재실행하지 않았다.
