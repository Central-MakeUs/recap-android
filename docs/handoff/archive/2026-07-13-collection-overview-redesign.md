# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status
DONE

## Owner
Codex

## Context
- 사용자는 제공된 보관함 그리드/리스트 시안처럼 `CollectionScreen`의 `즐겨찾기 / 유형별 보기 / 기타` 탭을 제거하고, 하나의 통합 overview에서 즐겨찾기 진입 카드와 카테고리 목록을 함께 보여주길 요청했다.
- 시안의 공통 구조는 `CollectionTopBar(보기 전환) -> 검색 -> 즐겨찾기 카드 -> 카테고리 그리드 또는 리스트`다. 그리드 순서는 `쇼핑·상품, 장소·맛집, 일정·예약, 정보·지식, 책·콘텐츠, 혜택·이벤트, 기록·캡처, 채용·취업, 기타`로 읽힌다.
- 현재 `CollectionUiState`/`CollectionViewModel`은 `CollectionTab`, `selectedTab`, `othersSort`, `favoriteItems`, `otherItems`를 별도 상태로 관리하고, `CollectionScreen`도 탭별로 서로 다른 본문을 그린다. 기타는 `ScreenshotContentType.OTHER` 카드만 별도 리스트로 렌더링한다.
- 현재 `CollectionRoute`에는 즐겨찾기 상세와 유형 상세 Navigation3 destination이 이미 있으며, 카테고리 상세는 `CollectionDetailFilter.ByType`으로 필터링된다.
- 현재 `ScreenshotCard`는 `categoryType != null`이면 상단 `RecapCategoryTextChip`, `categoryType == null`이면 하단 정리 일자를 표시한다. 기타를 실제 `RecapCategoryType`으로 추가하면 이 null 기반 분기는 더 이상 상세 화면의 요구사항을 표현할 수 없다.
- 현재 절대 정리 일자는 두 구현으로 나뉜다.
  - `core/design/.../ScreenshotCard.kt`: 항상 `M월 d일`.
  - `feature/screenshot/ScreenshotFormatters.kt`: 항상 `yyyy. MM. dd`, 화면 문자열은 `정리됨 %1$s`.
  - 요청된 전역 정책은 올해 `M월 d일 정리`, 이전 연도 `yyyy.MM.dd 정리`다.
- `ScreenshotContentType.OTHER`는 이미 `:core:model` enum, mock random selection(`ScreenshotContentType.entries`), Room의 `primaryContentType` 문자열 저장/복원 경로에 포함된다. 기타의 새 DB 컬럼은 필요하지 않지만, 사용자가 `DESIGN_REFERENCE`를 프로젝트 taxonomy에서 전역 제거하기로 확정했으므로 과거 Room row 호환을 위한 DB migration은 필요하다.
- `RecapCategoryType`에는 현재 8개 시각 카테고리만 있고 `Other`가 없다. `ScreenshotContentType.DESIGN_REFERENCE`와 `OTHER`는 모두 `toRecapCategoryType()`에서 null이다.
- `DESIGN_REFERENCE`는 현재 domain enum, label mapper/string, AI prompt의 `design_reference`, `docs/SCREENSHOT_MOCK_DATA.md`에 남아 있다. 과거 mock은 enum entries 전체에서 무작위 선택했으므로 기존 version 3 DB의 `screenshot_cards.primaryContentType`에 `DESIGN_REFERENCE`가 저장됐을 수 있다. enum만 제거하면 `ScreenshotContentType.valueOf(primaryContentType)`에서 크래시하므로 `MIGRATION_3_4`에서 해당 legacy 값을 `OTHER`로 치환한 뒤 enum을 제거한다.
- 사용자 제공 `core/design/src/main/res/drawable/ic_other_16.xml`은 현재 staged 신규 파일이며, intrinsic 크기가 `16dp x 3.636dp`, viewport가 `22 x 5`인 가로 캡슐이다. 구현자는 이 사용자 파일을 보존하고, 카테고리 아이콘 컨테이너에서 중앙 정렬/지각 크기가 맞지 않을 때만 vector viewport/path 여백을 조정한다.
- 구현에는 새 production dependency가 필요하지 않다. 기존 `CollectionTopBar`, `RecapSearchBar`, `RecapHazeFolderCard`, `RecapCategoryIcon`, `ScreenshotCard`, drawable과 theme token을 재사용한다.
- 사용자가 카테고리 노출, 즐겨찾기 0건, `DESIGN_REFERENCE` 제거, 기타 색상, 홈 즐겨찾기 진입 정책을 모두 확정했으므로 Cursor는 아래 스펙대로 구현한다.

## Spec
### 1. 확정 정책
1. 카테고리 노출
   - Room에 저장 카드가 있는 overview에서는 count가 1개 이상인 카테고리만 그리드/리스트에 표시한다.
   - 검색 중에도 검색 결과 count가 1개 이상인 카테고리만 표시한다.
   - Room 전체 저장 데이터가 0건이면 카테고리 overview를 렌더링하지 않고 기존 `CollectionEmptyContent`와 스크린샷 정리 진입 버튼을 유지한다.
2. 즐겨찾기 0건
   - 저장 카드가 있는 overview에서는 즐겨찾기 카드를 항상 표시한다.
   - 0건이면 `0 recaps`를 표시하고 클릭 가능하게 유지하며, 클릭하면 기존 즐겨찾기 상세 empty state로 이동한다.
3. `DESIGN_REFERENCE`
   - product/domain taxonomy에서 제거된 항목이므로 project-wide active code/resource/document에서 제거한다.
   - 기존 Room 데이터만 안전하게 보존하기 위해 migration SQL과 migration test fixture에서 legacy 문자열 `DESIGN_REFERENCE`를 참조하는 것은 허용한다.
   - 완료된 historical handoff archive는 과거 기록이므로 rewrite하지 않는다.
4. 기타 색상
   - `RecapCategoryOther300 = Color(0xFFF4F4F4)`.
   - `RecapCategoryOther500 = Color(0xFFD9D9D9)`.
   - `RecapCategoryOther700 = Color(0xFF717171)`.
5. 홈 즐겨찾기 진입
   - 홈의 즐겨찾기 전체 보기 요청은 보관함 root만 표시하지 않고 `FavoriteDetail`로 바로 이동한다.

### 2. 보관함 overview 통합
- `CollectionTab`, `selectedTab`, `SelectTab`, `othersSort`, `SetOthersSort`, 별도 기타 overview 리스트 상태/액션을 제거한다.
- 검색 바 아래에 새 즐겨찾기 카드 컴포넌트를 배치하고, 그 아래에 카테고리 overview 한 개만 렌더링한다.
- `CollectionTopBar`의 grid/list 보기 전환은 저장 카드가 있는 통합 overview에서 항상 노출한다. 탭 선택 여부로 노출을 제어하지 않는다.
- grid/list 모드는 기존 `CollectionTypeViewMode`와 `SetTypeViewMode`를 그대로 사용한다.
- 카테고리는 다음 고정 표시 순서를 사용한다.
  1. `SHOPPING_PRODUCT`
  2. `PLACE_RESTAURANT`
  3. `SCHEDULE_RESERVATION`
  4. `INFO_KNOWLEDGE`
  5. `BOOK_CONTENT`
  6. `BENEFIT_EVENT`
  7. `RECORD_CAPTURE`
  8. `JOB_CAREER`
  9. `OTHER`
- 위 순서 목록에서 실제 count가 1개 이상인 항목만 남긴다. count 0 category placeholder는 만들지 않는다.
- overview의 카테고리 label은 시안의 띄어쓰기/용어와 기존 자산을 재사용하도록 `RecapCategoryType.labelResId`(`쇼핑 · 상품`, `책 · 컨텐츠`, `채용 · 취업` 등)를 사용한다. detail title은 현재 `ScreenshotContentType.toLabelResId()` 정책을 유지한다.
- 그리드 모드:
  - 기존 `RecapHazeFolderCard`를 재사용한다.
  - 폴더 아래에 카테고리 label과 `%1$d recaps` count를 각각 표시한다.
  - stable category key를 사용한다.
- 리스트 모드:
  - 기존 `RecapCategoryIcon(size = Compact)`를 leading으로 재사용한다. 축소한 folder card를 사용하지 않는다.
  - 중앙에는 category label과 최신 카드 제목 예시를 기존 separator 규칙으로 표시한다.
  - trailing에는 `%1$d recaps` count를 표시한다.
  - 행 전체가 해당 카테고리 상세로 이동하는 최소 48dp 이상 터치 영역이어야 한다.
- 검색은 기존 title/summary 필터 로직을 유지한다. 검색 결과에 따라 즐겨찾기 count, 카테고리 count/example이 동일한 filtered set에서 계산되도록 한다. 별도 검색 결과 카드 화면은 추가하지 않는다.
- loading, Room 전체 empty state, bottom navigation content padding, search query 상태는 기존 동작을 유지한다.
- 하나의 세로 축에 이중 vertical scroll을 만들지 않는다. 고정 header 영역과 하나의 lazy grid/list 조합으로 구성하고, 각 lazy item에는 stable key를 제공한다.

### 3. 즐겨찾기 진입 카드 컴포넌트
- 시안 Image #3의 즐겨찾기 카드를 하나의 stateless molecule 컴포넌트로 분리한다. ViewModel을 직접 참조하지 않고 `count`, `onClick`, `modifier`를 받는다.
- 기존 lower-level 요소를 재사용한다. 새 공통 atom이나 새 외부 dependency를 만들지 않는다.
- 카드 구성:
  - `RecapBlue50` 계열 container와 기존 shape/theme token.
  - 흰색 rounded icon container + 기존 `ic_star_24`.
  - title `즐겨찾기`, count `%1$d recaps`, 기존 `ic_chevron_right_24`.
  - 카드 전체 클릭 및 적절한 button semantics/content description.
- 컴포넌트와 overview grid/list 화면 Preview를 `RECAPTheme(dynamicColor = false)`로 감싼다.
- 현재 thumbnail stack 기반 `CollectionOverviewCard`/`CollectionThumbnailStack`이 통합 화면에서 더 이상 사용되지 않으면 참조와 dead code를 함께 제거하되, unrelated design 컴포넌트까지 리팩터링하지 않는다.

### 4. 기타 정식 카테고리 추가와 `DESIGN_REFERENCE` 제거
- `RecapCategoryType.Other`를 추가한다.
  - icon: `R.drawable.ic_other_16`.
  - label: 기존/정리된 `기타` string resource.
  - color roles: `RecapCategoryOther300/500/700` 확정 token.
- `ScreenshotContentType.OTHER.toRecapCategoryType()`이 `RecapCategoryType.Other`를 반환하게 한다.
- `ScreenshotContentType.DESIGN_REFERENCE` enum entry를 제거한다.
- `ScreenshotContentTypeLabels.kt`의 `DESIGN_REFERENCE` label/mapping 분기와 `collection_content_type_design_reference` string을 제거한다.
- `RecapAnalysisPrompt.kt`의 allowed content types에서 `design_reference`를 제거한다.
- `docs/SCREENSHOT_MOCK_DATA.md`의 enum 개수와 목록을 9개 taxonomy로 갱신하고 `DESIGN_REFERENCE` 항목을 제거한다.
- `ScreenshotContentType.entries`를 사용하는 type picker와 mock randomizer는 enum 제거 결과를 그대로 사용한다. 별도 filtering이나 숨겨진 legacy UI option을 남기지 않는다.
- `RecapDatabase` version을 4로 올리고 `MIGRATION_3_4`를 추가/등록한다.
  - migration은 `screenshot_cards.primaryContentType = 'DESIGN_REFERENCE'`인 기존 row만 `'OTHER'`로 update한다.
  - 다른 card column, key field relation, favorite, timestamp, image ref를 변경하지 않는다.
  - migration 후 mapper의 `ScreenshotContentType.valueOf(primaryContentType)`가 legacy row 때문에 실패하지 않아야 한다.
- `docs/LOCAL_DATA.md`의 DB version/migration 목록과 설명을 version 4 / `MIGRATION_3_4` 기준으로 갱신한다.
- 기타를 `CollectionDetailFilter.ByType` 기반의 일반 category detail로 열고, 별도 `OpenOtherItem`/기타 탭 전용 정렬·선택 UI는 제거한다.
- `ic_other_16.xml`은 `RecapHazeFolderCard`, `RecapCategoryIcon`, category chip/top bar에서 중앙 정렬되고 다른 16dp 카테고리 자산과 지각 크기가 과도하게 다르지 않아야 한다. 필요할 때만 vector 자체에 투명 viewport 여백/중앙 정렬을 반영한다.
- mock repository는 변경된 9개 enum entries 전체를 반환할 수 있으므로 production 분기 추가를 피한다. injected index가 `OTHER`를 만드는 테스트를 추가한다.
- `OTHER` 저장 후 observe/get round-trip에서 그대로 복원되는 회귀 테스트를 추가한다.
- migration test는 version 3 DB에 legacy `DESIGN_REFERENCE` row와 정상 `OTHER` row를 함께 준비하고 `MIGRATION_3_4` 후 둘 다 `OTHER`로 읽히며 나머지 column/relation이 보존됨을 검증한다.

### 5. 상세 `ScreenshotCard` metadata 정책
- `categoryType == null` 여부로 chip/date를 결정하지 않는다. `ScreenshotCard` 또는 collection wrapper에 `CategoryChip` / `OrganizedDate`처럼 명시적인 metadata 표시 mode를 둔다.
- 즐겨찾기 상세:
  - 각 row 상단에 해당 캡처의 `RecapCategoryTextChip`을 표시한다.
  - 각 카드가 서로 다른 category를 가질 수 있으므로 `CollectionCardItemUiModel.categoryType`을 사용한다.
  - 하단 정리 일자는 표시하지 않는다.
- 모든 카테고리 상세(기타 포함):
  - 상단 category text chip을 표시하지 않는다.
  - title/summary 아래 하단에 정리 일자를 표시한다.
- Home favorites, 최근 정리 화면, component garden 등 기존 `ScreenshotCard` 호출부의 category chip 동작은 기본값으로 보존한다.
- `CollectionDetailUiModel`에는 상세 종류/metadata mode를 명시적으로 전달해 UI가 `categoryType == null` 같은 우연한 표현에 의존하지 않게 한다.
- 선택 모드, favorite star, divider, thumbnail shape animation과 click/selection semantics는 기존 동작을 유지한다.

### 6. 전역 절대 정리 일자 포맷
- 절대 정리 일자용 순수 formatter를 `:core:design`의 재사용 가능한 파일로 단일화한다. `ScreenshotCard.kt` 내부 formatter와 `feature:screenshot/ScreenshotFormatters.kt`의 중복 날짜 formatter를 제거/대체한다.
- device default `ZoneId` 기준으로 `organizedAtMillis`의 calendar year와 현재 calendar year를 비교한다.
  - 같은 해: `M월 d일`.
  - 이전 해(또는 현재 해가 아닌 방어적 fallback): `yyyy.MM.dd`.
- UI label은 string resource를 통해 date text 뒤에 `정리`를 붙여 정확히 다음처럼 렌더링한다.
  - `7월 13일 정리`
  - `2025.12.31 정리`
- 현재 `정리됨 2026. 07. 13` 형태인 `ScreenshotDetailScreen`과 `ScreenshotEditScreen`도 같은 formatter/label 정책으로 변경한다.
- `OrganizedRelativeTimeFormatter`를 사용하는 `방금 전 / N일 전` 상대 시간 UI는 이 작업 범위에서 변경하지 않는다.
- formatter는 unit test를 위해 `nowMillis`와 `zoneId`를 주입 가능하게 하고, 같은 해/이전 해/연도 경계/zone boundary를 JUnit5로 검증한다.

### 7. Navigation, state, Preview 정리
- 즐겨찾기 카드 click은 기존 `OpenFavoriteDetail`과 `FavoriteDetail` destination을 사용한다.
- 카테고리 click은 기존 `OpenTypeDetail`/`TypeDetail` 경로를 사용하되 기타도 동일 경로를 탄다.
- `favoritesNavigationRequestId`는 보관함 internal back stack을 root로 reset하고 `OpenFavoriteDetail` state를 만든 뒤 `FavoriteDetail` destination을 한 번만 push한다. 중복 destination push/기존 detail stack 잔존이 없어야 한다.
- 탭 제거로 불필요해진 tab Preview/test를 통합 grid/list/empty/favorite-detail/type-detail Preview/test로 갱신한다.
- 모든 새 user-facing text/content description은 `core/design/src/main/res/values/strings.xml`에 둔다.
- 기존 theme/color/drawable/component를 재사용하고 새 `Icons.*`, Canvas, text icon substitute, production dependency를 추가하지 않는다.

## Files to Touch
- `docs/handoff/HANDOFF.md` (Cursor는 구현 후 Result만 갱신)
- `docs/LOCAL_DATA.md`
- `docs/SCREENSHOT_MOCK_DATA.md`
- `core/model/src/main/java/com/chalkak/recap/core/model/screenshot/ScreenshotContentType.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabase.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/RecapDatabaseMigrations.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/DatabaseModule.kt`
- `core/data/src/main/java/com/chalkak/recap/core/data/ai/RecapAnalysisPrompt.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/RecapDatabaseMigration3To4Test.kt` (new)
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/MockScreenshotAnalysisRepositoryTest.kt`
- `core/data/src/test/java/com/chalkak/recap/core/data/screenshot/persistence/ScreenshotCardDaoTest.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionContract.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionViewModel.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionScreen.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionOverviewComponents.kt` 또는 즐겨찾기 카드용 narrow new component file
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionSelectionComponents.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionDetailScreen.kt`
- `feature/collection/src/main/java/com/chalkak/recap/feature/collection/CollectionRoute.kt`
- `feature/collection/src/test/java/com/chalkak/recap/feature/collection/CollectionViewModelTest.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/category/RecapCategoryType.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/category/ScreenshotContentTypeLabels.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/theme/Color.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/card/ScreenshotCard.kt`
- `core/design/src/main/java/com/chalkak/recap/core/design/component/card/OrganizedDateFormatter.kt` (new, suggested)
- `core/design/src/test/java/com/chalkak/recap/core/design/component/card/OrganizedDateFormatterTest.kt` (new)
- `core/design/src/main/res/drawable/ic_other_16.xml` (사용자 제공 파일, 시각 보정이 필요한 경우만 수정)
- `core/design/src/main/res/values/strings.xml`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotFormatters.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotDetailScreen.kt`
- `feature/screenshot/src/main/java/com/chalkak/recap/feature/screenshot/ScreenshotEditScreen.kt`

## Acceptance Criteria
- 보관함 overview에 `즐겨찾기 / 유형별 보기 / 기타` 탭 chip이 더 이상 없다.
- 저장 카드가 있는 overview는 검색 아래에 독립 즐겨찾기 카드와 하나의 category grid/list를 표시한다.
- top bar 보기 전환으로 동일 category data가 grid/list 간 전환되며, 탭 상태는 존재하지 않는다.
- 카테고리는 고정 순서 중 현재/search-filtered count가 1개 이상인 항목만 표시하며 count 0 placeholder는 없다.
- Room 전체 저장 데이터가 0건이면 기존 스크린샷 정리 empty 화면과 organize 진입 버튼이 표시된다.
- 즐겨찾기 카드는 저장 카드가 있는 overview에서 항상 표시되고, 0건이어도 `0 recaps`로 클릭 가능하며 즐겨찾기 상세 empty state로 이동한다.
- 즐겨찾기 카드는 독립 stateless component이며 `count`, 전체 click, star/chevron, string resource, theme token, Preview를 갖춘다.
- grid item은 기존 haze folder와 label/count를, list item은 기존 compact category icon과 label/example/trailing count를 사용한다.
- 기타는 `RecapCategoryType.Other`, `ic_other_16`, 확정된 Other 색 토큰을 사용하고 일반 카테고리와 같은 detail navigation을 탄다.
- 기타 아이콘은 folder/list/detail/chip 사용처에서 중앙 정렬되고 다른 category icon과 지각 크기가 일관된다.
- active domain enum/type picker/mock/label/resource/AI prompt/docs에서 `DESIGN_REFERENCE`/`design_reference` option이 제거된다.
- DB version 4의 `MIGRATION_3_4`는 기존 `DESIGN_REFERENCE` stored row를 `OTHER`로 치환하고 다른 card/key-field 데이터를 보존한다.
- mock 분석은 변경된 enum 집합에서 `OTHER`를 생성할 수 있고, Room 저장/복원은 `OTHER`를 손실하지 않음이 테스트로 고정된다.
- 즐겨찾기 상세 `ScreenshotCard`는 category text chip을 표시하고 정리 일자는 표시하지 않는다.
- 모든 카테고리 상세 `ScreenshotCard`는 category text chip 대신 하단 정리 일자를 표시한다.
- 절대 정리 일자는 device time zone 기준 올해 `M월 d일 정리`, 다른 해 `yyyy.MM.dd 정리`로 표시되며 Collection, Screenshot detail/edit가 같은 formatter를 사용한다.
- 기존 상대 시간 UI, selection/delete/favorite toggle, detail search/sort, bottom padding, screenshot navigation 동작은 회귀하지 않는다.
- 기존 Home/Recent/ComponentGarden의 `ScreenshotCard` category chip 기본 동작이 유지된다.
- 새 user-facing text는 string resource이며, 새 Compose component/screen state Preview는 `RECAPTheme`로 감싼다.
- 새 production dependency, `Icons.*`, Canvas/text icon substitute를 추가하지 않는다.

## Validation
- unit test:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
  ```
- debug build:
  ```powershell
  $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
  ```
- focused coverage:
  - 통합 overview category order/count/example/search mapping, count 0 category 제외, `OTHER` grouping.
  - 즐겨찾기 detail vs category detail metadata mode.
  - 탭 제거 후 selection/detail state 회귀.
  - 올해/이전 해/연도 경계/zone 경계 정리 일자 formatter.
  - mock `OTHER` 생성, Room `OTHER` round-trip, version 3 legacy `DESIGN_REFERENCE` row의 3→4 migration.
- Preview 정적 확인:
  - 통합 overview grid/list.
  - 즐겨찾기 카드 0건/다건.
  - 즐겨찾기 detail category chip, category/기타 detail organized date.
  - `ic_other_16`의 folder/list/top bar/chip 지각 크기와 중앙 정렬.

## Out of scope
- `MIGRATION_3_4`의 legacy content type 치환 외 새 DB column/table/index 또는 unrelated schema 변경.
- 완료된 `docs/handoff/archive/**` 역사 문서의 `DESIGN_REFERENCE` 기록 rewrite.
- 정리 일자를 위한 별도 DB column 추가; 기존 `createdAtMillis`를 정리 시각으로 계속 사용한다.
- 보관함 검색을 별도 검색 결과 screen이나 screenshot row 목록으로 재설계하는 작업.
- `OrganizedRelativeTimeFormatter`의 30일 노출/상대 시간 정책 변경.
- thumbnail 생성, OCR/Firebase 분석, cloud sync 변경.
- 제공 시안을 벗어난 전역 design system 리팩터링 또는 unrelated component rename.

## Technical Debt
- 없음. `DESIGN_REFERENCE` legacy 문자열은 기존 사용자 DB를 안전하게 `OTHER`로 옮기기 위한 migration SQL/test fixture와 과거 handoff archive에만 남는다.

## Cursor Result
- Changed files (CHANGES_REQUESTED follow-up): CollectionOverviewComponents.kt (favorites title/count vertical stack), ScreenshotCard.kt (non-null metadataMode default CategoryChip; OrganizedDate preview explicit), CollectionSelectionComponents.kt (non-null metadataMode default), CollectionScreen.kt (lazy contentType for favorites/category items)
- Prior impl files unchanged in intent: docs/LOCAL_DATA.md, docs/SCREENSHOT_MOCK_DATA.md, core/model/.../ScreenshotContentType.kt, core/data migration/prompt/tests, feature/collection contract/VM/detail/route/tests, core/design Other category/colors/OrganizedDateFormatter, feature/screenshot formatters/detail, feature/home mappers
- Build/test: :core:design:testDebugUnitTest GREEN, :feature:collection:testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN
- Open questions: none
- Addressed Codex blocking: favorites card vertical title/count; ScreenshotCard no longer infers mode from categoryType nullability. Nit: lazy contentType added.

## Codex Review
- Blocking: none.
- Nits: none.
- Positive patterns: 즐겨찾기 진입 카드가 stateless molecule과 theme Preview로 분리됐고, title/count 세로 배치가 시안과 일치한다. `ScreenshotCardMetadataMode`는 non-null 기본값으로 기존 Home/Recent 동작을 보존하면서 Collection detail이 명시적으로 chip/date 정책을 선택한다. 통합 overview의 lazy item은 stable key와 `contentType`을 함께 제공한다.
- Validation: Codex가 `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest --console=plain` 실행 — GREEN (`309 actionable tasks`, failures/errors 0). `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug --console=plain` 실행 — GREEN (`402 actionable tasks`). `git diff --check HEAD`도 통과했으며 line-ending warning만 확인했다. Manual runtime smoke는 미실행(기기/에뮬레이터 미사용).
- Verdict: DONE.
