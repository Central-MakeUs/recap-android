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

## In Progress

- 없음

## Done

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
