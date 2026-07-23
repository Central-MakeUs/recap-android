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

- [ ] 2026-07-22 - Remote 정리 진행률 모델 분리 및 역행 버그 수정
  - Context: `RemoteScreenshotAnalysisRepository.organize()`가 업로드(`index + 1 / total`)와 서버 분석 poll(`successCount + failCount / total`)을 동일 `onProgress` 콜백으로 방출함. 마지막 업로드 직후 100%였다가 첫 PROCESSING poll에서 0%로 역행함. Home 상단 progress bar는 mock 분석 흐름용 디버그 UI이며, 사용자용 진행률은 추후 별도 인터페이스로 제공 예정. 현재 백엔드에서 실제 분석을 돌리기 어려워 E2E 검증·UX 확정이 보류됨
  - Next: repository/ViewModel에 업로드·분석 단계를 구분한 progress 모델 도입 또는 단조 증가 overall progress로 환산. Home은 디버그용 최소 표시(단계 텍스트 등) 유지, 사용자 UI는 별도 설계 후 동일 progress 소스 소비
  - Blocked by: 백엔드 organize/status poll 실환경 분석 파이프라인 가용
  - Handoff: not started

- [ ] 2026-07-22 - `ScreenshotAnalysisProgressViewModel` 부분 실패·상태 불일치 처리
  - Context: (1) `RemoteCompleted`가 `PARTIAL_FAILED`여도 `successCount + failCount`를 완료 수로 기록하고 오류 없이 종료함. `outcome.status`/`failCount`를 검사하지 않아 진행 UI가 사라지면 사용자는 누락 사실을 알 수 없음. (2) Local 경로에서 `organize` 콜백이 이미 `completedCount`/`progress`를 2/2로 올린 뒤 Room 저장이 실패하면 `errorMessage`만 설정되고 완료 수는 그대로라 실제 저장 결과와 모순됨. 작업 완료 UI가 전체 성공만 전제하고 단계별·부분 성공 outcome을 구분하지 않음
  - Next: Remote는 `PARTIAL_FAILED`/`failCount`를 검사해 부분 실패 상태 또는 복구 가능한 오류 노출. Local 저장 실패 분기에서 `persisted.size` 기준으로 `completedCount`/`progress` 동기화. 상위 progress 역행 이슈와 함께 단계별 outcome 모델 정리
  - Blocked by: 백엔드 organize/status poll 실환경 분석 파이프라인 가용 (Remote 부분 실패 UX 확정)
  - Handoff: not started

- [ ] 2026-07-22 - 세션×온보딩 2×2 라우팅과 계정 전환 시 로컬 DB wipe
  - Context: 현재 루트 라우팅은 `onboardingCompleted`만 보고, 세션 토큰이 없어도 Main에 남을 수 있음. 계정 고유 ID 미저장이라 세션 만료 후 다른 계정 재로그인 시 이전 계정의 로컬 DB(향후 이미지 썸네일 캐시)가 남을 수 있음. 재로그인 성공 시에도 항상 `PermissionGuide`로 가서 온보딩 완료 후 재로그인과 불일치
  - Desired matrix (`hasSession` × `onboardingCompleted`):
    - 세션 X + 온보딩 미완료 → 첫 실행, Landing부터 전체 온보딩
    - 세션 O + 온보딩 미완료 → 로그인 직후 앱 종료, 저장된 step 복원
    - 세션 X + 온보딩 완료 → 세션 만료, Landing만(재로그인), 가이드 스킵 후 Main
    - 세션 O + 온보딩 완료 → 일반 사용, Main
  - Next: (1) 로그인 시 계정 고유 ID 저장(소스 미정: Kakao `user.id` vs 서버 RECAP userId) (2) 재로그인 시 이전 ID와 비교해 다르면 DB/이미지 캐시만 wipe, 온보딩 완료 플래그는 유지 (3) 콜드스타트·런타임 세션 소실 시 Main→로그인(Landing) 라우팅 (4) 재로그인 후 `onboardingCompleted`면 Main으로 직행
  - Depends: 온보딩 플로우를 먼저 구현한 뒤 착수
  - Handoff: not started

- [ ] 2026-07-18 - `docs/LOCAL_DATA.md`를 CaptureDetailResponse 동기화 스키마에 맞게 갱신
  - Context: 스크린샷 mock 계약이 `captureId: Long` / `typeCode` / `organizedAt` / Room v1로 리셋되었지만 `LOCAL_DATA.md`는 여전히 imageId·key_fields·migration 설명을 담고 있음
  - Next: LOCAL_DATA.md의 screenshot_cards / repository / image storage 섹션을 현재 구현과 맞추고 레거시 migration 문서 제거
  - Handoff: not started

- [ ] 2026-07-16 - 한국어 텍스트 overflow 대응
  - Context: 설정·계정 관리 등 UI에서 긴 한국어 라벨/이메일/버튼 문구가 잘리거나 레이아웃을 깨뜨릴 수 있음. `RecapButton`·설정 row·계정 관리 화면 등 maxLines/ellipsis/가변 폭 정책이 통일되어 있지 않음
  - Next: 공통 텍스트 overflow 규칙(줄 수, ellipsis, softWrap, 버튼 compactText)을 정한 뒤 design/feature 컴포넌트에 일괄 적용
  - Handoff: not started

- [ ] 2026-07-15 - 설정 하위 화면(계정 관리, 문의하기)
  - Context: 설정 top-level UI/명칭과 Gradle 모듈은 `:feature:settings`로 통일됨. 계정 관리·문의하기는 stub. 이용 안내·공유 즐겨찾기 가이드는 `AppRoute`로 연결됨
  - Next: 계정 관리/문의하기 전용 화면 스펙 후 구현
  - Handoff: not started

- [ ] 2026-07-14 - 카카오 로그인 SDK 예외 처리 보강
  - Context: `KakaoLoginClient`는 `ClientErrorCause.Cancelled`만 `AuthError.Cancelled`로 매핑하고, 그 외 Talk 실패는 무조건 Account fallback 또는 `ProviderUnavailable`/`Unknown`으로 뭉개짐. 카카오 문서 예외 플로우(`AccessDenied`, 계정 미로그인 `Unknown` 등)와 케이스별 UX 분기·메시지가 없고, 온보딩 UI도 아직 `signInWithKakao`를 호출하지 않음
  - Next: Kakao SDK `AuthError`/`AuthErrorCause`를 `AuthError` 도메인으로 세분화하고, Cancelled 시 Account fallback 금지·AccessDenied/Unknown별 사용자 메시지·재시도 정책을 정의한 뒤 ViewModel 연동
  - Ref: https://developers.kakao.com/docs/ko/kakaologin/android#exceptions
  - Handoff: not started

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

- [ ] 2026-07-10 - 민감한 스크린샷·분석 데이터의 Android 백업 정책 강화
  - Context: `allowBackup=true`이고 backup/data extraction rules에 제외 규칙이 없어 앱 내부 스크린샷 파일과 Room DB의 분석 결과가 클라우드 백업 및 기기 이전 대상이 될 수 있음
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

- [x] 2026-07-23 - 공식 NavDisplay 기반 공통 navigation motion 안정화
  - Result: single-pane custom `RecapNavDisplay`를 제거하고 공식 Navigation3 `NavDisplay`가 scene lifecycle, transition, predictive back을 소유하도록 복원함. Home↔Collection은 기존 전환을 유지하고, 나머지는 공통 push/pop과 20% parallax를 사용함. 좌·우 edge gesture에는 full-range predictive pop을 적용하고, 3버튼·하드웨어 back(`EDGE_NONE`)에는 predictive preview를 적용하지 않음
  - Replaces: 40% predictive scrub과 custom commit completion 구현 및 `RecapNavDisplay` OverlayScene/공유 요소 lifecycle 패리티 후속 항목
  - Validation: `testDebugUnitTest` GREEN, `assembleDebug` GREEN, `git diff --check` GREEN

- [x] 2026-07-23 - 공통 Navigation3 push/pop 및 predictive back motion 구현
  - Result: root/app/main tab/feature NavDisplay에 iOS 방향의 공통 slide policy를 적용하고, 400ms push/pop, 30% background parallax, 40% predictive preview와 commit completion, Screenshot Edit 미저장 예외 및 Onboarding -> Main 무전환을 구현
  - Superseded by: 공식 NavDisplay 기반 공통 navigation motion 안정화
  - Handoff: `docs/handoff/archive/2026-07-23-navigation-motion.md`
  - Validation: `testDebugUnitTest` GREEN, `assembleDebug` GREEN, API 37 실기기 push/pop·predictive commit/cancel·nested pop·Screenshot Edit 예외·Onboarding 교체 확인

- [x] 2026-07-22 - 전역 스크린샷 backend Mock/Remote 런타임 스위치 및 부분 삭제 처리
  - Result: 분석 전용 mode를 스크린샷 도메인 전역 backend mode로 승격하고, Store/Switcher/Mock reset policy를 분리했으며 Home·최근 정리·Storage·Capture command의 공통 전환, Mock/Remote 명명 정리, Remote 다중 삭제 부분 성공 UX를 구현
  - Handoff: `docs/handoff/archive/2026-07-22-global-screenshot-backend-switch.md`
  - Validation: `:core:data:testDebugUnitTest :feature:collection:testDebugUnitTest :feature:developer:testDebugUnitTest :feature:home:testDebugUnitTest` GREEN, `assembleDebug` GREEN, legacy symbol 정적 검색 및 `git diff --check` GREEN

- [x] 2026-07-19 - Debug 분석 데이터 소스 Mock/Remote 런타임 스위치 구현
  - Result: DataStore 기반 분석 mode 저장, 요청별 Mock/Remote repository 위임, Remote stub 안전 오류 처리, 분석 실행 상태 공유, 기존 로컬 데이터 정리 후 전환하는 개발자 옵션 UX와 관련 단위 테스트를 구현
  - Handoff: `docs/handoff/archive/2026-07-19-analysis-data-source-runtime-switch.md`
  - Validation: `:core:data:testDebugUnitTest :app:testDebugUnitTest :feature:developer:testDebugUnitTest assembleDebug` GREEN (Cursor 결과 확인)

- [x] 2026-07-18 - 스크린샷 mock 계약을 RE-CAP Swagger CaptureDetailResponse와 동기화
  - Result: `captureId: Long`, flat `typeCode`, `originalImageUrl`, `organizedAt` 계약을 mock/domain/Room/navigation/UI에 반영하고 Room fresh-install schema를 version 1로 초기화함. 기본 mock ID는 프로세스 재시작 간 충돌을 피하는 UUID 기반 양수 Long을 사용함
  - Handoff: `docs/handoff/archive/2026-07-18-screenshot-mock-swagger-sync.md`
  - Validation: `.\gradlew.bat testDebugUnitTest --continue --no-daemon --no-configuration-cache` GREEN, `.\gradlew.bat assembleDebug` GREEN, legacy screenshot 계약 정적 검색 및 `git diff --check` GREEN

- [x] 2026-07-17 - 앱 범위 전역 Toast layer 구현
  - Result: Onboarding/Main/Developer와 Main 내부 route를 아우르는 단일 root Toast host, Activity 범위 FIFO queue, 접근성 timeout, configuration change 잔여 시간 유지, root Haze source와 toast 영역 effect를 적용
  - Handoff: `docs/handoff/archive/2026-07-17-global-toast-layer.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN, 단일 hosted `RecapToastHost` 및 pending screenshot deleted toast 심볼 제거 정적 확인

- [x] 2026-07-13 - 비전체화면 스크린샷용 영구 썸네일 적용
  - Result: 원본 폭/높이 50%·JPEG 품질 80 썸네일을 decoder 단계 다운샘플링과 교체 실패 복구 방식으로 저장하고, 상세/편집/홈/컬렉션은 썸네일 우선, 전체화면은 원본 우선으로 분리
  - Handoff: `docs/handoff/archive/2026-07-13-screenshot-thumbnail-storage.md`
  - Validation: `.\gradlew.bat testDebugUnitTest` GREEN, `.\gradlew.bat assembleDebug` GREEN

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
