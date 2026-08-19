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

- [ ] 2026-08-05 - 카카오 `me()` 실패가 로그인 전체를 막는 실패 경로의 UX 정리
  - Context: 소유자 해시 판정을 위해 `signInWithKakao`가 `kakaoLoginClient.fetchUserProfile()`을 새로 호출한다. 일시적 네트워크 오류로 `me()`가 실패하면 카카오 로그인 자체는 성공했는데도 `AuthError.ProviderUnavailable`로 로그인이 실패하고, 일반 로그인 실패 토스트만 노출되어 원인·재시도 안내가 없다.
  - Next: `me()` 실패 전용 안내/재시도 UX를 둘지 결정
  - Handoff: not started

- [ ] 2026-07-18 - `docs/LOCAL_DATA.md`를 현재 로컬 저장 구현에 맞게 갱신
  - Context: `screenshot_cards` / repository 섹션은 `captureId`·`typeCode`·`organizedAt`·Room v1로 맞춰졌다.
    image storage는 코드가 `captureId`와 썸네일 생성 API를 쓰는데 문서는 `imageId`·파이프라인 없음으로 남아 있고, 테스트 목록에 없는
    `RecapDatabaseMigration2To3Test`·key fields가 남아 있다. Remote content PATCH는 이미 연결됐다.
  - Next: image storage API/썸네일 설명을 코드에 맞추고, 존재하지 않는 migration·key fields 테스트 설명을 제거한다
  - Handoff: not started

- [ ] 2026-07-08 - `:core:data` Robolectric 테스트를 JUnit5로 통일
  - Context: `ScreenshotCardDaoTest`, `ScreenshotImageStorageTest`는 Robolectric JUnit5 확장(`RobolectricExtension`)이 클래스패스에서 해석되지 않아 JUnit4 `@RunWith(RobolectricTestRunner::class)` + Vintage 엔진으로 작성됨. 같은 모듈에 JUnit5 테스트와 JUnit4 테스트가 혼재함
  - Next: Robolectric JUnit5 연동 의존성/설정을 정리한 뒤 해당 테스트를 JUnit5 스타일로 이전하고, `docs/TESTING.md`에 Android Context/Room 단위 테스트 러너 기준을 명시
  - Handoff: not started

- [ ] 2026-07-08 - Coroutine dispatcher DI 패턴 도입
  - Context: 현재 `Dispatchers.IO` 직접 사용과 `@VisibleForTesting` 테스트 훅이 섞여 있어 비동기 코드 테스트 제어 방식이 일관되지 않음. `@IoDispatcher` 등 qualifier 기반 Hilt provider를 추가하고 ViewModel/Repository/Storage의 blocking work dispatcher를 생성자 주입으로 점진 이전할 필요가 있음
  - Handoff: not started

- [ ] 2026-07-10 - Navigation3 entry별 ViewModel 수명 범위 구성
  - Context: 각 `NavDisplay`에 ViewModelStore entry decorator가 없어 화면의 Hilt ViewModel이 Activity 범위에 남고, pop 이후에도 화면 상태·Room Flow·대용량 이미지 목록이 유지되거나 재진입 시 재사용될 수 있음
  - Next: `lifecycle-viewmodel-navigation3`과 `rememberViewModelStoreNavEntryDecorator()`를 내비게이션 계층에 적용하고, 의도적으로 공유할 ViewModel만 상위 범위로 분리
  - Handoff: not started

- [ ] 2026-07-12 - `:core:design` → `:core:model` 의존성 재검토
  - Context: `ScreenshotContentType` 라벨과 `RecapCategoryType` 매핑을 공통화하면서 `:core:design`이 도메인 모델에 의존함. 현재는 관련 로직이 작아 별도 모듈 대신 실용적인 구조로 허용
  - Next: 카테고리 매핑이 늘거나 다른 도메인 모델 의존성이 추가될 때 presentation/UI mapping 계층 분리 또는 feature별 매핑 재배치를 검토
  - Priority: Low
  - Handoff: not started

## In Progress

- 없음

## Done

- [x] 2026-07-31 - 작은 기기·고배율·3버튼 내비에서 깨지는 고정 레이아웃/패딩 전역 대응
  - Result: `RecapSpacing`/compact 화면 템플릿 등 전역 레이아웃 시스템은 도입하지 않는다.
  - Closed: 2026-08-20 (won't fix)

- [x] 2026-07-10 - minSdk 30에서 Haze 미지원 fallback 구현
  - Result: 실시간 blur 경로만 API 제약 대상이다. BottomBar/Toast는 이미 API 33 미만에서 realtime blur를 끄고,
    `RecapHazeFolderCard`/`RecapHazeFolderIcon` static haze는 영향 없음으로 추가 fallback을 두지 않는다.
  - Closed: 2026-08-20 (won't fix)

- [x] 2026-07-14 - 카카오 로그인 SDK 예외 처리 보강
  - Result: Cancelled만 구분하고 나머지는 Account fallback/`ProviderUnavailable`/`Unknown`으로 처리하는 현재 경로로
    충분하다고 판단해 세분화하지 않는다.
  - Closed: 2026-08-20 (won't fix)

- [x] 2026-08-05 - 계정별 vs 기기 공통 설정 추가 분류
  - Result: `clearAccountScopedPreferences()`와 `docs/LOCAL_DATA.md`에 기기 공통(온보딩·deviceId·알림) vs 계정
    종속(AI 동의) 분류, 신규 키 등록 위치가 있다.
  - Closed: 2026-08-19 (이미 반영된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-08 - 정리 플로우 스크린샷 그리드 스크롤 시 이미지 재로딩 체감 개선
  - Result: 선택 그리드는 Coil `ImageLoader` + `MediaStoreThumbnail` fetcher/keyer로 시스템 썸네일과 캐시 키를 사용한다.
  - Closed: 2026-08-19 (이미 구현된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-07 - 테스트 전략을 TDD 중심으로 구체화
  - Result: `docs/TESTING.md`에 TDD 우선순위가 있고, 모듈별 JUnit5 단위 테스트와 Compose Preview Screenshot Testing이
    운영 중이다.
  - Closed: 2026-08-19 (이미 반영된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-22 (updated 2026-08-05) - 세션 유효성·온보딩·오프라인을 통합한 앱 진입 라우팅과 계정 전환 시 로컬 데이터 격리
  - Result: `hasSession`(refresh token) × `onboardingCompleted`로 `RecapEntryMode` 파생, Reauth, 카카오 owner hash wipe, 로그인 Connectivity gate, Main 오프라인은 캐시 읽기/쓰기 큐 없이 Error+수동 재시도, foreground·validated 복구 시 Error만 자동 refresh 1회(`MainContentRecoveryTrigger`).
  - Validation: 관련 unit test GREEN, `assembleDebug` GREEN
  - Closed: 2026-08-05
  - Handoff: not started (backlog 직접 구현)

- [x] 2026-08-04 - 런타임 Mock/Remote 전환 계층을 BuildConfig 고정 선택으로 교체
  - Result: DataStore mode·Switcher·8개 Switching repository와 개발자 전환 UI를 제거하고, `:core:data`의 `BuildConfig.USE_MOCK_BACKEND` 및 lazy Provider 기반 Hilt 선택으로 교체했다. debug도 `-PUSE_MOCK_BACKEND=false`로 Remote를 선택할 수 있으며 Mock User의 계정 조회·탈퇴는 Remote 위임을 유지한다.
  - Handoff: `docs/handoff/archive/2026-08-04-buildconfig-backend-selection.md`
  - Validation: 관련 전체 unit test GREEN, debug Mock/Remote assemble GREEN, qa assemble GREEN, invalid property expected failure, debug Mock/Remote `:core:data:testDebugUnitTest` GREEN, 잔존 참조 및 `git diff --check` GREEN
  - Closed: 2026-08-04

- [x] 2026-07-16 - 한국어 텍스트 overflow 대응
  - Result: `Type.kt` `recapTextStyle()`에 `localeList = LocaleList("ko")`와 `LineBreak.Paragraph` + `WordBreak.Phrase`를 적용해, 시스템 언어와 무관하게 `RecapTypography` 텍스트가 한국어 phrase 단위 줄바꿈을 사용하도록 함
  - Closed: 2026-08-04

- [x] 2026-07-10 - DataStore 읽기·손상 오류에도 앱 시작 복구 경로 추가
  - Result: 확정된 Preferences corruption만 recovery marker로 교체하고 앱 시작 전에 Room·원본 이미지·썸네일·세션·최근 검색·온보딩 상태를 멱등 초기화한다. 일반 읽기 `IOException`은 100ms/300ms 간격으로 최대 2회 재수집하며, 소진 시 데이터를 삭제하지 않고 dismiss 불가능한 단일 액션 `RecapPopup`에서 재시도를 제공한다.
  - Handoff: `docs/handoff/archive/2026-08-04-datastore-startup-corruption-recovery.md`
  - Validation: `:core:data:testDebugUnitTest :app:testDebugUnitTest` GREEN, `assembleDebug` GREEN, `git diff --check` GREEN
  - Closed: 2026-08-04

- [x] 2026-07-25 - Remote 목록 썸네일 캐싱이 검색/목록 응답을 직렬 차단하지 않도록 개선
  - Result: `resolveThumbnailSources`는 hit→로컬 path, miss→null을 즉시 반환하고 Semaphore(4) 백그라운드 prefetch로 디스크 캐시를 채운 뒤 `thumbnailReady`를 emit한다. Search/Home/Collection/Recent ViewModel이 path로 UiState를 패치하며, Coil은 remote URL을 받지 않아 첫 miss 네트워크는 앱 캐시 한 경로만 탄다
  - Closed: 2026-08-03

- [x] 2026-07-22 - `ScreenshotAnalysisProgressViewModel` 부분 실패·상태 불일치 처리
  - Result: `OrganizeTerminalResultMapper`가 Remote `PARTIAL_FAILED`/`failCount`와 Local 저장 실패 시 `persisted.size`를 `AllSuccess`/`PartialSuccess`/`AllFailed`로 매핑한다. `ScreenshotAnalysisProgressViewModel`은 `terminalResult`를 설정하고 `OrganizePartialFailedScreen` 등 단계별 완료 UI로 연결되며 관련 단위 테스트가 있다
  - Closed: 2026-08-03 (이미 구현된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-15 - 설정 하위 화면(계정 관리, 문의하기)
  - Result: 계정 관리는 `AccountManagementRoute`/`ViewModel`(로그아웃·회원탈퇴)로 구현됨. 문의하기는 전용 stub 화면 대신 `RecapLegalUrls.CUSTOMER_SUPPORT` 외부 URL을 연다
  - Closed: 2026-08-03 (이미 구현된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-08 - 정리 화면에서 이미지 권한 없음과 실제 빈 목록 상태 구분
  - Result: Main에서 `ImageAccessLevel.Denied` 시 정리 진입 전 권한 팝업을 띄우고, Organize는 Partial 접근 카드·권한 요청/설정 이동 경로를 제공한다. 빈 목록은 권한이 있을 때의 empty UI로 구분된다
  - Closed: 2026-08-03 (이미 구현된 상태로 Open에서 Done으로 이동)

- [x] 2026-07-25 - Remote 스크린샷 상세 content 편집 API 연결
  - Context: Remote 상세 로드(`ScreenshotDetailRepository` + `CaptureRepository.getDetail`)와 삭제/즐겨찾기는 연결됐지만 `CaptureApi`에 title/summary/body/type PATCH가 없어 편집 저장은 Room `updateCardContent`만 호출한다. Remote에서는 저장이 실패(save error)한다.
  - Next: 서버 content update API 확정 후 `CaptureMutationRepository`에 updateContent를 추가하고 `ScreenshotViewModel.saveEdit`을 ~~Switching mutation으로 위임~~ `CaptureMutationRepository`(BuildConfig로 선택된 Mock/Remote)로 위임
  - Handoff: not started

- [x] 2026-07-30 - 빠른 전역 navigation push/pop 역전의 화면 레이어 안정화
  - Result: 실제 initial/target scene pair에 방향과 z-index를 고정하는 transition planner를 도입하고, 취소/재타기팅/idle 정규화 및 post-splash 흰색 window/root fallback 배경을 적용
  - Handoff: `docs/handoff/archive/2026-07-30-navigation-interrupted-transition-stability.md`
  - Validation: `:core:design:testDebugUnitTest :app:testDebugUnitTest` GREEN, `assembleDebug` GREEN, `git diff --check` GREEN, SM-S948N 빠른 Home↔Settings·Settings↔NotificationSettings 교차 입력 및 predictive commit 확인

- [x] 2026-07-10 - 민감한 스크린샷·분석 데이터의 Android 백업 정책 강화
  - Result: 서버 SoT 기준으로 `backup_rules.xml` / `data_extraction_rules.xml`(cloud + device-transfer)에서 `recap/` 이미지, `recap.db`, `user_preferences` DataStore를 exclude. 복원 후 재로그인·서버 재동기화가 정상 경로. `LOCAL_DATA.md`·`PROJECT.md`에 정책 문서화
  - Validation: `assembleDebug` GREEN

- [x] 2026-07-29 - 스크린샷 선행 압축 및 서버 분석 진행률 분리
  - Result: Confirmation 진입 시 PNG/JPEG/HEIC/HEIF를 JPEG 품질 75로 백그라운드 선행 압축하고, 시작 시 미완료 작업을 Progress 단계로 넘겨 최대 2회 안에서 완료하도록 구성함. 압축·업로드 진행률은 숨기고 서버 status polling만 progress bar에 반영하며, 공유 경로의 메모리 전용 one-shot 전달과 부분 준비 실패 처리를 유지함
  - Handoff: `docs/handoff/archive/2026-07-29-screenshot-upload-compression.md`
  - Validation: `:core:data:testDebugUnitTest :feature:organize:testDebugUnitTest :app:testDebugUnitTest` GREEN, `assembleDebug` GREEN, `git diff --check` GREEN

- [x] 2026-07-29 - AI 데이터 전송 동의 User API 연동
  - Result: `GET/POST/DELETE /api/v1/users/me/consent`를 UserApi/UserRepository에 추가하고 데이터 관리 화면에서 조회·동의·철회를 서버 상태로 반영
  - Validation: `:core:data:testDebugUnitTest --tests UserRepositoryTest`, `:feature:settings:testDebugUnitTest --tests DataManagementViewModelTest`, `:feature:settings:assembleDebug`

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
  - Result: DataStore 기반 분석 mode 저장, 요청별 Mock/Remote repository 위임, Remote stub 안전 오류 처리, 분석 실행 상태 공유, 기존 로컬 데이터 정리 후 전환하는 개발자 옵션 UX와 관련 테스트를 구현
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
