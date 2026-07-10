# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

## Owner

Codex

## Context

- 사용자가 제공한 4개 레퍼런스 이미지를 기준으로 스크린샷 상세정보, 스크린샷 정보 수정, 스크린샷 전체화면의 3개 화면과 2개 바텀시트(더보기 액션, 유형 선택)를 추가한다.
- 앱은 Navigation3를 사용하며 `RecapNavHost`의 app-level back stack 아래에 Home/Collection main-tab back stack이 중첩되어 있다. 상세 화면은 Home과 Collection 모두에서 진입해야 하므로 main-tab 내부 목적지가 아니라 app-level 목적지여야 한다.
- `RecentOrganizedScreenshotCard`, `FavoriteCategoryCard`, `ScreenshotCard`는 이미 `onClick`을 받는다. 실제 미연결 지점은 `HomeRoute`, `RecentOrganizedScreenshotsRoute`, `CollectionRoute`가 선택된 `imageId`를 목적지로 전달하지 않는 TODO/No-op이다.
- production Collection UI는 현재 core `ScreenshotCard`가 아니라 `CollectionCaptureListItem`/`FavoriteCategoryCard`를 사용한다. `ScreenshotCard`의 클릭 API는 그대로 유지하고, 현재 production의 모든 스크린샷 아이템 진입점을 연결한다.
- 저장 데이터는 `ScreenshotCardRepository`/Room에 title, summary, primary content type, favorite, 이미지 참조 등을 보관하지만 레퍼런스의 `본문` 필드는 없다. 편집 결과를 실제로 유지하려면 모델/Room schema 확장이 필요하다.
- 기존 `RecapInputField`는 single/multiline, max length, counter, error를 지원하므로 수정 화면에서 재사용한다. `RecapActionBottomSheet`는 아이콘+설명+2-action 확인용이라 이미지 2의 3-action 메뉴에는 맞지 않으므로 기존 visual pattern을 따르는 별도 컴포넌트를 만든다.

## Spec

### 1. 모듈과 Navigation3 구조

- 새 Android library module `:feature:screenshot`을 만든다. `:core:data`, `:core:design`, `:core:model` 및 이미 version catalog에 있는 Compose/Hilt/Lifecycle/Navigation3/Coil 의존성만 사용하고 새 외부 production dependency는 추가하지 않는다.
- `settings.gradle.kts`, `app/build.gradle.kts`, `docs/PROJECT.md`의 module map/dependency를 갱신한다.
- app-level `AppRoute`에 `@Serializable data class Screenshot(val imageId: String) : AppRoute`를 추가한다. 비어 있는 `imageId`는 만들지 않는다.
- `RecapNavHost`는 이 route에서 `ScreenshotRoute(imageId, onNavigateBack)`를 렌더링한다.
- `ScreenshotRoute` 내부에는 private Navigation3 back stack을 두고 상세를 root로, 수정과 전체화면을 leaf로 관리한다. root에서 back은 app back stack을 pop하고, 수정/전체화면에서 back은 feature 내부 back stack만 pop한다.
- `HomeRoute`, `RecentOrganizedScreenshotsRoute`, `CollectionRoute`, `RecapMainScreen`, `RecapMainTabNavHost`에 `(String) -> Unit` 형태의 `onNavigateToScreenshot` callback을 전달한다.
  - `HomeAction.SelectRecentScreenshot`와 `HomeAction.SelectFavoriteItem`은 callback에 해당 id를 전달한다.
  - `RecentOrganizedScreenshotsAction.SelectItem`도 callback에 해당 id를 전달한다.
  - `CollectionAction.OpenFavoriteItem`과 `OpenOtherItem`은 `CollectionRoute.handleAction`에서 callback으로 처리하고 ViewModel의 destination TODO/no-op을 제거한다.
  - `CollectionDetailScreen`에 전달하는 action handler도 `viewModel::onAction`이 아니라 route의 `handleAction`을 사용해 favorite/type detail 목록의 카드 클릭도 동일하게 동작시킨다.
- 화면 전환은 composable 본문 중 실행하지 않고 사용자 callback 또는 one-shot event 수집에서만 back stack을 변경한다.

### 2. feature state/contract

- `ScreenshotContract`, `ScreenshotViewModel`, `ScreenshotRoute`, 화면 파일 3개를 역할별로 분리한다. 한 파일에 두 screen을 넣지 않는다.
- ViewModel은 `StateFlow` 기반 immutable UiState/Action 계약을 사용하고, `ScreenshotRoute`가 `collectAsStateWithLifecycle()`로 수집한다.
- ViewModel은 최초 `imageId`를 idempotent하게 load/observe하고 다음 상태를 표현한다.
  - loading
  - content(card + edit draft + isSaving/isDeleting/favorite update state)
  - not found/load error
- ViewModel이 소유할 상태: repository에서 관찰한 card, 편집 draft(title/summary/body/content type), 저장/삭제 진행 및 사용자에게 보여줄 복구 가능한 오류.
- Route/Screen에 둘 UI-only 상태: 더보기 sheet 노출, 유형 선택 sheet 노출과 sheet 안에서 아직 확정하지 않은 임시 선택. 필요 시 `rememberSaveable`을 사용하며 ViewModel에 ModalBottomSheet/SheetState를 넣지 않는다.
- 저장 성공과 삭제 성공은 one-shot event로 전달한다. 저장 성공 시 수정 화면만 pop하여 갱신된 상세를 보이고, 삭제 성공 시 feature route를 닫아 원래 Home/Collection으로 돌아간다. raw exception text는 노출하지 않는다.

### 3. 상세정보 화면

- 레퍼런스 이미지 1의 구조를 따른다.
  - 상단에는 원본 스크린샷 hero를 edge-to-edge로 표시한다. 이미지 모델 우선순위는 `storedImagePath -> sourceImageUri -> thumbnailPath`이며, hero는 `ContentScale.Crop`으로 레퍼런스처럼 고정 높이 영역을 채운다.
  - status bar inset 아래/주변으로 drawable 기반 back, favorite, `ic_more_24` 버튼을 overlay하고 각 버튼은 최소 48dp touch target과 string resource content description을 가진다.
  - hero 오른쪽 아래에 새 vector drawable 기반 전체화면 버튼을 overlay한다. `Icons.*`, 텍스트/Canvas 아이콘 대체는 금지한다.
  - hero 아래에는 유형 chip, `정리됨 yyyy. MM. dd`(createdAtMillis를 system timezone으로 변환), title, summary, body 순서로 표시한다.
  - content가 길면 화면 전체가 세로 스크롤된다. 본문이 빈 기존 row에는 빈 공간 대신 `본문이 없습니다.` 계열의 명시적 empty copy를 표시한다.
- favorite 버튼은 현재 `isFavorite`을 표시하고 `ScreenshotCardRepository.updateFavorite`을 호출해 실제 저장 상태를 토글한다.
- 더보기 버튼은 `ScreenshotActionBottomSheet`를 연다. sheet는 기존 ModalBottomSheet들의 둥근 상단/흰 surface/drag handle/inset 처리 패턴을 따르며 다음 3개 full-width action을 가진다.
  - `스크린샷 정보 수정`: sheet를 닫고 edit draft를 현재 card 값으로 초기화한 뒤 수정 화면으로 이동한다.
  - `스크린샷 삭제`: error/destructive 색으로 표시하며 Room row를 삭제하고 앱 private 원본/thumbnail 파일을 best-effort로 정리한다. 제공 flow에 없는 2차 확인 sheet/dialog은 추가하지 않는다.
  - `닫기`: sheet만 닫는다.
- 삭제 중 action 중복 입력을 막고, DB 삭제 실패 시 상세에 남아 재시도 가능한 메시지를 보여준다. DB 삭제가 성공한 뒤 private file 삭제 실패는 기존 Collection 정책처럼 삭제 완료를 되돌리지 않는다.
- card 없음/조회 실패 상태는 back과 retry가 가능한 별도 content를 보여준다. 이미지 모델이 없거나 Coil load가 실패하면 hero/fullscreen에 앱 테마 기반 placeholder와 사용자용 load-failure copy를 보여준다.

### 4. 수정 화면과 유형 선택 sheet

- 레퍼런스 이미지 3의 구조를 따른다.
  - 상단 bar: `스크린샷 정보 수정`, 왼쪽 `취소`, 오른쪽 `완료`.
  - 정리 날짜, 수정 불가능한 원본 이미지 preview, `원본 이미지는 수정할 수 없으며, 텍스트 정보만 편집 가능합니다.` 안내.
  - 유형 row와 `변경` action.
  - `RecapInputField`를 재사용한 title(최대 30자), 한 줄 요약(최대 80자), body(multiline, 별도 hard max 없음).
  - 작은 화면/IME에서도 모든 필드에 접근하도록 세로 scroll 및 IME/navigation bar padding을 처리한다.
- title은 trim 후 비어 있으면 완료를 비활성화하고 field error를 표시한다. summary/body는 비어 있어도 저장 가능하다. 저장 시 title/summary/body 양끝 공백을 정리하되 body 내부 줄바꿈은 유지한다.
- 완료는 title, summary, body, primary content type을 단일 repository update로 저장하며 isFavorite, confidence, keyFields, image refs, createdAtMillis는 보존하고 updatedAtMillis만 갱신한다. 저장 중 field/완료를 비활성화한다.
- 저장 실패 시 수정 화면과 draft를 유지하고 복구 가능한 오류를 표시한다. 다시 `완료`를 누르면 재시도한다. `취소`/system back은 이번 draft를 폐기하고 상세로 돌아가며, 별도 unsaved-changes 확인 UI는 이번 scope에 추가하지 않는다.
- 유형 `변경`은 레퍼런스 이미지 4의 `ScreenshotTypePickerBottomSheet`를 연다.
  - 2-column selectable chip grid, 현재 선택 강조, 하단 `선택 완료` 버튼, navigation bar inset을 구현한다.
  - chip 탭만으로 ViewModel draft를 즉시 바꾸지 않고 sheet 내부 임시 선택만 바꾼다. `선택 완료` 시 draft 유형을 갱신하고 sheet를 닫는다. dismiss 시 기존 draft 유형을 유지한다.
  - 데이터 손실을 피하기 위해 현재 domain의 `ScreenshotContentType.entries` 전체 10개를 제공한다. 레퍼런스의 9개 항목에 기존 `DESIGN_REFERENCE`도 포함하며, 기존 enum/string resource label을 우선 재사용한다. grid는 내용이 화면보다 높으면 sheet 내부에서 스크롤 가능해야 한다.
- `ScreenshotActionBottomSheet`, `ScreenshotTypePickerBottomSheet`, 선택 chip은 stateless data/callback API와 `modifier`를 받고, `RECAPTheme` Preview를 제공한다. 새 컴포넌트는 feature 전용으로 두고 한 화면만을 위해 core API를 과도하게 확장하지 않는다.

### 5. 전체화면 화면

- 레퍼런스 이미지 1의 전체화면 버튼으로 진입하는 세 번째 screen이다.
- 검은 background에 원본 이미지 모델을 `ContentScale.Fit`으로 가능한 전체 영역에 표시하고, status/navigation bar inset을 고려한 drawable back/close button을 제공한다.
- system back과 overlay back 모두 상세로 돌아간다. 이미지가 없거나 load 실패해도 사용자가 돌아갈 수 있는 error content를 제공한다.
- pinch zoom/pan, 공유, 다운로드, 회전 잠금, 시스템 bar 강제 숨김은 이번 scope에 포함하지 않는다.

### 6. persistence/model 변경

- `ScreenshotAnalysisResult`에 editable `body: String`을 추가한다. 기존 named constructor가 깨지지 않도록 안전한 기본값 `""`을 두고, mock 분석 결과에는 detail 화면을 확인할 수 있는 비어 있지 않은 body를 채운다.
- `ScreenshotCardEntity`/mapper에 non-null `body` column을 추가하고 Room version을 3으로 올린다.
- `MIGRATION_2_3`은 `ALTER TABLE screenshot_cards ADD COLUMN body TEXT NOT NULL DEFAULT ''`로 기존 row를 보존한다. `DatabaseModule`에 1->2와 2->3 migration을 모두 등록하고 `docs/LOCAL_DATA.md`를 version/API/schema에 맞게 갱신한다.
- DAO/repository에 다음 API를 추가한다(이름은 프로젝트 naming에 맞게 조정 가능하나 동작은 동일해야 한다).
  - `observeCard(imageId): Flow<StoredScreenshotCard?>`: 수정 화면에서 저장한 결과가 back 후 상세에 자동 반영되도록 single-card Flow 제공.
  - `updateCardContent(imageId, title, summary, body, primaryContentType, updatedAtMillis)`: editable column만 update.
- update는 없는 `imageId`를 새 row로 만들지 않는다. ViewModel은 update 결과/재조회로 missing row를 not-found 처리한다.
- `saveAnalysisResults` round-trip에서도 body와 기존 image refs가 유지되어야 한다. 편집 저장에 `saveAnalysisResults(..., empty image refs)`를 재사용해 이미지 경로를 지우지 않는다.
- 삭제는 기존 `deleteCard`와 `ScreenshotImageStorage.deleteStoredImages(setOf(imageId))` 정책을 재사용한다.

### 7. 디자인/리소스/접근성 규칙

- UI text/content description/error/empty copy는 string resource로 추가한다. feature별 문자열이 app module에서 참조 불가능한 현재 멀티모듈 구조를 고려해 기존 feature들이 사용하는 `core/design` string resource 관례를 따른다.
- 색/타이포/shape는 `MaterialTheme` 또는 기존 `core/design/theme` token을 사용한다. 레퍼런스의 spacing/높이는 local named token object로 관리하고 raw hex/font size를 screen body에 흩뿌리지 않는다.
- `RecapInputField`, `RecapButton`, `RecapCategoryChip` 등 맞는 기존 컴포넌트는 재사용한다. selectable type chip처럼 interaction/state 계약이 다른 경우에만 새 컴포넌트를 만든다.
- 새 아이콘은 `core/design/src/main/res/drawable` vector asset으로 추가하며 새 `Icons.*`, Canvas 또는 텍스트 대체는 금지한다.
- 모든 interactive control은 최소 48dp touch target, role/selected semantics, 의미 있는 content description을 가진다. 순수 장식 이미지는 description을 null로 둔다.
- 세 screen과 두 bottom sheet는 주요 content/loading/error/selected state Preview를 `RECAPTheme(dynamicColor = false)`로 제공한다.

### 8. tests

- `ScreenshotCardDaoTest`에 body round-trip, single-card observe, editable-column update, update 시 image refs/favorite/confidence/keyFields/createdAt 보존 검증을 추가한다.
- migration SQL은 최소한 기존 v2 row가 body `""`로 유지되는지 검증 가능한 테스트를 추가한다. 현재 Room schema export가 없어 정식 MigrationTestHelper를 바로 쓸 수 없다면 Robolectric/SupportSQLiteDatabase 기반 최소 migration 검증을 사용하고 테스트 불가 사유를 Result에 남기지 말고 구현 가능한 형태로 구성한다.
- 새 `ScreenshotViewModelTest`는 다음을 검증한다: load success/not-found/failure, favorite toggle, edit draft 초기화/변경/취소, valid save와 repository 전달값, save failure에서 draft 유지, delete success/failure와 one-shot event.
- Home/Recent/Collection의 mapper/ViewModel 동작은 변경하지 않는다. callback propagation은 가능하면 작은 Route/Compose test로 검증하되, 과도한 navigation test harness를 새로 도입하지 않는다.

## Files to Touch

- `settings.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/java/com/chalkak/recap/app/AppRoute.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapNavHost.kt`
- `app/src/main/java/com/chalkak/recap/app/RecapMainScreen.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/HomeRoute.kt`
- `feature/home/src/main/java/com/chalkak/recap/feature/home/RecentOrganizedScreenshotsRoute.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionRoute.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionViewModel.kt` (destination TODO/no-op 정리만)
- `feature/screenshot/build.gradle.kts` (new)
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/**` (new; contract/route/viewmodel/3 screens/component/formatter)
- `feature/screenshot/src/test/java/com/chalkak/recap/feature/screenshot/**` (new)
- `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotAnalysisResult.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabase.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabaseMigrations.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/DatabaseModule.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepository.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardEntities.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardMappers.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardDao.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardRepository.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardDaoTest.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/**Migration*Test.kt` (new if separated)
- `core/design/src/main/res/values/strings.xml`
- `core/design/src/main/res/drawable/ic_*fullscreen*.xml` 및 필요한 상태 vector asset (new, narrow)
- `docs/PROJECT.md`
- `docs/LOCAL_DATA.md`
- 기존 `ScreenshotAnalysisResult(...)`를 positional argument로 생성하는 파일이 발견되면 compile fix 범위에서만 수정한다.

## Acceptance Criteria

- Home의 최근 스크린샷 카드와 즐겨찾기 카드, 최근 정리 전체 목록 카드, Collection의 즐겨찾기/기타/유형 상세 목록 카드가 모두 올바른 `imageId`의 스크린샷 상세로 이동한다.
- app route에는 `imageId`만 전달되며 상세/수정/전체화면은 새 `:feature:screenshot` module에 분리되어 있다.
- 상세 화면이 레퍼런스 이미지 1의 hero overlay, 유형/정리일/title/summary/body 구조를 표현하고 loading/not-found/image-failure/empty-body 상태에서 빠져나오거나 재시도할 수 있다.
- 상세 favorite 버튼이 실제 Room 상태를 토글하고 Home/Collection 목록에도 Flow를 통해 반영된다.
- `ic_more_24`가 레퍼런스 이미지 2 형태의 컴포넌트화된 바텀시트를 열며 수정/삭제/닫기가 각각 동작한다.
- 수정 화면이 레퍼런스 이미지 3의 구조를 따르고 title 30자/summary 80자 제한, body multiline, 필수 title validation, 저장 중 중복 방지를 제공한다.
- 유형 변경 sheet가 2-column selectable chip, 현재 선택, 임시 선택, `선택 완료` semantics를 제공하고 전체 `ScreenshotContentType`을 선택/저장할 수 있다.
- 저장 후 상세로 돌아왔을 때 별도 재진입 없이 수정된 유형/title/summary/body가 보이고, 앱을 재시작해도 Room에서 유지된다. image refs/favorite/confidence/keyFields/createdAt은 편집 전과 동일하다.
- 삭제 성공 시 상세가 닫히고 Home/Collection에서 항목이 사라지며, managed private 원본/thumbnail 정리는 best-effort로 실행된다. 삭제 실패 시 상세에 남아 재시도할 수 있다.
- 전체화면 버튼이 검은 배경의 `ContentScale.Fit` 화면을 열고 overlay/system back으로 상세에 복귀한다.
- Room v2 데이터가 v3으로 마이그레이션될 때 기존 카드/이미지 참조/key fields를 잃지 않고 body는 빈 문자열로 초기화된다.
- 새 UI text는 resource이며 theme token/기존 컴포넌트/Preview/accessibility 규칙을 지키고 새 `Icons.*` 또는 불필요한 production dependency가 없다.
- 관련 unit tests와 debug build가 모두 GREEN이다.

## Validation

1. Local unit/Robolectric tests:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

2. Debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

3. Manual runtime smoke check(에뮬레이터/기기가 가능한 경우):
   - Home 최근/즐겨찾기, 최근 정리 전체 목록, Collection 즐겨찾기/기타/유형 상세에서 각각 card -> 동일 id 상세 진입.
   - 상세 favorite toggle, more sheet dismiss/close, 수정 진입, 유형 선택 dismiss/confirm, title/summary counter와 validation, 저장 후 상세 갱신.
   - 상세 -> 전체화면 -> back, 긴 body scroll, 이미지 없음/error 상태 확인.
   - 삭제 후 이전 목록 복귀 및 항목 제거 확인.

4. Result에는 실행한 명령, GREEN/실패 결과, 수동 확인 여부와 남은 제약을 기록한다. 같은 목적의 build/test를 반복하지 않는다.

## Out of scope

- pinch zoom/pan, 이미지 편집/교체, 공유/다운로드, 회전 잠금, 시스템 bar 강제 immersive mode.
- delete action 뒤의 별도 2차 확인 dialog/sheet, edit 취소 시 unsaved-changes 확인.
- OCR/Firebase AI prompt/schema에 body 생성 필드를 새로 요구하거나 기존 두 분석 pipeline을 통합하는 작업. 이번 작업은 screenshot persistence model의 body를 추가하고 현재 mock 저장 경로만 갱신한다.
- `ScreenshotCard`/`FavoriteCategoryCard`/`RecentOrganizedScreenshotCard`의 unrelated visual refactor 및 Collection selection UX 변경.
- 전체 앱 navigation 구조, tab back-stack 정책, theme system의 대규모 refactor.
- 신규 외부 production dependency 추가.

## Technical Debt

- none. 이 작업에서 새로 발견되는 scope 밖 후속 항목만 `docs/BACKLOG.md`에 기록하고 여기에 저장 여부를 요약한다.

## Cursor Result

- Changed files (review fixes): `ScreenshotCardDao.kt`, `ScreenshotCardMappers.kt` (saveAnalysisResults body/imageRefs merge), `ScreenshotViewModel.kt` (cancel save on discard, LoadError duplicate message), `ScreenshotRoute.kt`, `ScreenshotEditScreen.kt`/`ScreenshotTokens.kt`/`ScreenshotTypePickerBottomSheet.kt`/`ScreenshotDetailScreen.kt` (48dp targets + favorite toggle semantics), `ScreenshotCardDaoTest.kt`, `ScreenshotViewModelTest.kt`
- Build/test: `.\gradlew.bat :core:data:testDebugUnitTest :feature:screenshot:testDebugUnitTest assembleDebug` GREEN; new regression tests (`saveAnalysisResults_preservesExistingBodyAndImageRefsOnBlankResave`, `discard during save cancels update and does not emit success`) GREEN
- Manual smoke: not run
- Open questions: none
- Review fixes addressed: blocking 1/2/3 + LoadError nit

## Codex Review

- Blocking: none.
- Nits:
  - public `ScreenshotRoute`가 다른 Route composable들과 달리 `modifier: Modifier = Modifier`를 노출하지 않는다. 현재 app-level 호출에는 기능상 문제가 없으므로 선택적 API 일관성 개선으로만 남긴다. (`ScreenshotRoute.kt:21`)
- Positive patterns: 기존 body/image refs 병합과 회귀 테스트, 저장 취소 시 job cancellation, 48dp action/chip target, favorite toggle semantics, LoadError 단일 문구가 이전 리뷰 이슈를 정확히 해소했다. app-level `imageId` route, feature 내부 Navigation3, Room 2->3 migration, 전용 update API와 theme Preview도 handoff와 일치한다.
- Validation: Codex가 `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat :core:data:testDebugUnitTest :feature:screenshot:testDebugUnitTest` 실행 — GREEN (`feature:screenshot` 11 tests, `ScreenshotCardDaoTest` 13 tests 및 `core:data` 관련 suites failures/errors 0). Cursor의 최신 `assembleDebug` GREEN 결과와 현재 APK 산출물을 확인했고, `git diff --check`도 통과했다. Manual smoke는 미실행(기기/에뮬레이터 미사용).
- Verdict: DONE

