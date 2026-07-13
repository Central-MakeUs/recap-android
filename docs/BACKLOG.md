# BACKLOG.md - RECAP

Cursor는 Codex의 개인 메모리를 볼 수 없다. 두 에이전트가 공유해야 하는 후속 항목은 이 문서에 남긴다.

작업에 착수할 때는 항목을 `docs/handoff/HANDOFF.md`로 옮기고, 이 문서에는 상태만 갱신한다.

## 작성 규칙

- 한 항목은 가능한 한 한 줄 요약으로 시작한다.
- 구현 스펙 수준의 긴 내용은 `HANDOFF.md`로 옮긴다.
- 구현 중 발견된 기술 부채는 이 문서에 저장한다.
- 개인 메모리, 임시 생각, 이미 해결된 디버깅 로그는 남기지 않는다.
- 날짜는 `YYYY-MM-DD` 형식으로 쓴다.
- 상태는 섹션으로 관리하고, 항목 자체는 목록으로 누적한다.

## Open

- [ ] 2026-07-07 - 테스트 전략을 TDD 중심으로 구체화
  - Context: 현재 특별한 테스트 코드 체계 없음
  - Handoff: not started

- [ ] 2026-07-08 - `:core:data` Robolectric 테스트를 JUnit5로 통일
  - Context: `ScreenshotCardDaoTest`, `ScreenshotImageStorageTest`는 Robolectric JUnit5 확장(`RobolectricExtension`)이 클래스패스에서 해석되지 않아 JUnit4 `@RunWith(RobolectricTestRunner::class)` + Vintage 엔진으로 작성됨. 같은 모듈에 JUnit5 테스트와 JUnit4 테스트가 혼재함
  - Next: Robolectric JUnit5 연동 의존성/설정을 정리한 뒤 해당 테스트를 JUnit5 스타일로 이전하고, `docs/TESTING.md`에 Android Context/Room 단위 테스트 러너 기준을 명시
  - Handoff: not started

- [ ] 2026-07-08 - 정리 플로우 스크린샷 그리드 스크롤 시 이미지 재로딩 체감 개선
  - Context: `feature/organize` 선택/확인 화면이 Coil 3 `AsyncImage` + MediaStore `content://` URI를 기본 `ImageLoader`로 렌더링함. 메모리 캐시 miss·재디코딩·placeholder 부재로 스크롤 복귀 시 이미지가 다시 불러와지는 것처럼 보일 수 있음
  - Handoff: not started

- [ ] 2026-07-08 - 정리 화면에서 이미지 권한 없음과 실제 빈 목록 상태 구분
  - Context: `OrganizeViewModel`이 `LocalScreenshotDataSource.queryAllScreenshots()` 결과만 보고 상태를 구성해, 이미지 권한이 거부된 사용자도 "스크린샷 없음" 빈 상태로 보임. 권한 요청/설정 이동 경로가 정리 플로우 안에 필요함
  - Handoff: not started

- [ ] 2026-07-08 - Coroutine dispatcher DI 패턴 도입
  - Context: 현재 `Dispatchers.IO` 직접 사용과 `@VisibleForTesting` 테스트 훅이 섞여 있어 비동기 코드 테스트 제어 방식이 일관되지 않음. `@IoDispatcher` 등 qualifier 기반 Hilt provider를 추가하고 ViewModel/Repository/Storage의 blocking work dispatcher를 생성자 주입으로 점진 이전할 필요가 있음
  - Handoff: not started

- [ ] 2026-07-10 - minSdk 30에서 Haze 미지원 fallback 구현
  - Context: 현재 `minSdk = 30`인데 `dev.chrisbanes.haze` glass/blur 효과가 API 30 기기에서 정상 적용되지 않음. `RecapBottomBar`, `RecapHazeFolderCard`, 홈/보관함 `hazeSource` 연동 등 Haze 사용 UI에 대체 렌더링(반투명 배경·단색 tint 등) fallback이 필요함
  - Handoff: not started

- [ ] 2026-07-10 - 민감한 스크린샷·OCR 데이터의 Android 백업 정책 강화
  - Context: `allowBackup=true`이고 backup/data extraction rules에 제외 규칙이 없어 앱 내부 스크린샷 파일과 Room DB의 OCR 원문·분석 결과가 클라우드 백업 및 기기 이전 대상이 될 수 있음
  - Next: 제품 백업 정책을 확정한 뒤 민감 파일·DB를 명시적으로 제외하거나 앱 백업을 비활성화하고, 백업/복원 시나리오를 검증
  - Handoff: not started

- [ ] 2026-07-10 - Navigation3 entry별 ViewModel 수명 범위 구성
  - Context: 각 `NavDisplay`에 ViewModelStore entry decorator가 없어 화면의 Hilt ViewModel이 Activity 범위에 남고, pop 이후에도 화면 상태·Room Flow·대용량 이미지 목록이 유지되거나 재진입 시 재사용될 수 있음
  - Next: `lifecycle-viewmodel-navigation3`과 `rememberViewModelStoreNavEntryDecorator()`를 내비게이션 계층에 적용하고, 의도적으로 공유할 ViewModel만 상위 범위로 분리
  - Handoff: not started

- [ ] 2026-07-10 - DataStore 읽기·손상 오류의 앱 시작 복구 경로 추가
  - Context: `UserPreferencesRepository`의 `dataStore.data`에 오류 복구가 없고 startup state가 `Loading`에서 시작해, 파일 손상이나 읽기 실패 시 프로세스 crash 또는 splash 고착으로 앱 진입이 불가능할 수 있음
  - Next: `IOException` 기본값 복구, `ReplaceFileCorruptionHandler`, 명시적인 startup error/fallback 상태와 관련 테스트를 추가
  - Handoff: not started

- [ ] 2026-07-12 - `:core:design` → `:core:model` 의존성 재검토
  - Context: `ScreenshotContentType` 라벨과 `RecapCategoryType` 매핑을 공통화하면서 `:core:design`이 도메인 모델에 의존함. 현재는 관련 로직이 작아 별도 모듈 대신 실용적인 구조로 허용
  - Next: 카테고리 매핑이 늘거나 다른 도메인 모델 의존성이 추가될 때 presentation/UI mapping 계층 분리 또는 feature별 매핑 재배치를 검토
  - Priority: Low
  - Handoff: not started

## In Progress

- 없음

## Done

- [x] 2026-07-13 - 보관함 통합 overview 개편과 기타 카테고리 정식화
  - Result: 즐겨찾기/유형별/기타 탭을 즐겨찾기 진입 카드와 카테고리 grid/list overview로 통합하고, `OTHER` taxonomy·Room 3→4 migration·전역 정리 일자 포맷과 detail metadata 정책을 적용
  - Handoff: `docs/handoff/archive/2026-07-13-collection-overview-redesign.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

- [x] 2026-07-10 - 스크린샷 상세·수정·전체화면 구현
  - Result: Home/보관함 카드의 상세 진입, 스크린샷 정보 수정·유형 변경·즐겨찾기·삭제·전체화면, Room body 저장 및 v2->v3 migration 완료
  - Handoff: `docs/handoff/archive/2026-07-10-screenshot-detail-edit-fullscreen.md`
  - Validation: `:core:data:testDebugUnitTest :feature:screenshot:testDebugUnitTest` GREEN, `assembleDebug` GREEN

- [x] 2026-07-08 - 스크린샷 분석 저장 실패와 취소 처리 보강
  - Result: mock 분석 저장 경로를 IO dispatcher로 이동하고, `CancellationException` 재전파 및 저장 실패 progress/error state 처리를 보강
  - Handoff: `docs/handoff/archive/2026-07-08-screenshot-analysis-persistence-fixes.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

- [x] 2026-07-08 - Room 기반 보관함 화면 구현
  - Result: 보관함 개요/유형 상세/즐겨찾기 상세, 정리 결과 Room 저장, 개발자 옵션 스크린샷 정리 데이터 초기화 완료
  - Handoff: `docs/handoff/archive/2026-07-08-collection-library-screen.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

- [x] 2026-07-08 - 스크린샷 분석 카드 저장 기반 구현
  - Result: 분석 결과 `isFavorite`, Room 카드/키필드 저장소, `user_preferences` DataStore provider, 앱 private 이미지 경로 구성 완료
  - Handoff: `docs/handoff/archive/2026-07-08-screenshot-card-storage-foundations.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

- [x] 2026-07-08 - 정리 시작 후 홈 진행률 기반 mock 분석 흐름 구현
  - Result: 정리 시작 placeholder snackbar 제거, 선택 이미지 mock 분석 시작, 홈 탭 이동, 홈 상단 분석 progress 표시 완료
  - Handoff: `docs/handoff/archive/2026-07-08-screenshot-analysis-progress.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

- [x] 2026-07-08 - cleanup 내부명을 organize로 마이그레이션
  - Result: `:feature:organize`, `Organize*`, `OcrOrganizeRange`, organize 리소스/알림 설정명으로 전환 완료
  - Handoff: `docs/handoff/archive/2026-07-08-cleanup-to-organize-rename.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN, `rg -n "cleanup|Cleanup|cleanUp" app core feature settings.gradle.kts build.gradle.kts` no hits

- [x] 2026-07-08 - 스크린샷 분석 mock repository 구성
  - Result: 스크린샷 파일명 기반 mock 분석 결과, deterministic unit test, mock 계약 문서 추가
  - Handoff: `docs/handoff/archive/2026-07-08-screenshot-mock-data.md`
  - Validation: `.\gradlew.bat :core:data:testDebugUnitTest :app:assembleDebug` GREEN

- [x] 2026-07-07 - 멀티모듈 MVVM + UDF/MVI 스타일 전환
  - Result: `:core:*`, `:feature:*` 모듈 분리 완료
  - Handoff: `docs/handoff/archive/2026-07-07-multimodule-migration.md`
  - Validation: `.\gradlew.bat assembleDebug` GREEN, `.\gradlew.bat testDebugUnitTest` GREEN
