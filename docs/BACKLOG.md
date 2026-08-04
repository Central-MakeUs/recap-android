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

- [ ] 2026-07-31 - 작은 기기·고배율·3버튼 내비에서 깨지는 고정 레이아웃/패딩 전역 대응
  - Context: 화면·컴포넌트 간격이 ~6.7인치 기준 고정 `dp`로 맞춰져 있어, 작은 물리 화면 + 높은 디스플레이/폰트 배율 + 3버튼 내비게이션(inset) 조합에서 온보딩 등 풀뷰포트 화면의 CTA·일러스트·텍스트가 잘리거나 겹친다. `core/design`에 Spacing/compact 레이아웃 시스템이 없고, 로컬 `*Tokens` 고정값·스크롤 없는 Column·큰 고정 높이 일러스트(예: Landing `120/90/58.dp`, AddToFavorite `238.dp`)가 원인. Permission/Upload만 scroll+pinned CTA. insets도 화면마다 `safeDrawing`/`navigationBars` 제각각.
  - Next: (1) `core/design`에 scrollable body + pinned bottom actions + compact 시 일러스트 축소/숨김용 화면 템플릿 (2) 풀스크린 플로우 insets 계약 통일 (3) `RecapSpacing` 시맨틱 토큰 + compact/fontScale에서 여유 간격만 축소. 온보딩을 첫 적용·레퍼런스로 두고 이후 화면은 점진 적용. 고정 padding 일괄 축소만으로는 부족.
  - Handoff: not started

- [ ] 2026-07-25 - 정리 알림 클릭 딥링크
  - Context: 정리 진행/완료 알림 클릭은 현재 MainActivity 실행만 한다. 확인 화면의 NotificationPermissionRequestBottomSheet → POST_NOTIFICATIONS 요청 및 `organizeCompleteNotificationEnabled` 동기화는 완료.
  - Next: 완료/부분실패 결과 화면으로 PendingIntent 딥링크
  - Handoff: not started

- [ ] 2026-07-22 (updated 2026-07-31) - 세션 유효성·온보딩·오프라인을 통합한 앱 진입 라우팅과 계정 전환 시 로컬 데이터 격리
  - Problem: 현재 루트 라우팅은 `onboardingCompleted`만 보고 세션 상태를 관찰하지 않는다. 토큰이 없거나 refresh token이 서버에서 만료·폐기되어도 Main에 남을 수 있고, 반대로 네트워크 단절·timeout처럼 유효성을 일시적으로 확인할 수 없는 상태를 세션 무효로 오판하면 불필요한 로그아웃과 데이터 손실이 발생한다. 세션 만료 후 재로그인 성공 시 온보딩 완료 여부와 무관하게 가이드로 이동하는 흐름도 재방문 사용자 요구와 맞지 않는다.
  - Current implementation:
    - `RecapStartupViewModel`은 로컬 `onboardingCompleted`만으로 splash 종료와 `Onboarding`/`Main`을 결정하고, 세션 유효성 확인 때문에 splash를 유지하지는 않는다.
    - `TokenRefreshCoordinator`는 액세스 토큰 만료 임박 또는 401에서 갱신하며, 서버가 `INVALID_REFRESH_TOKEN`/`EXPIRED_REFRESH_TOKEN`을 명시한 경우에만 토큰을 지우고 네트워크 실패에서는 보존한다. 그러나 이 결과가 앱 전역 인증/라우팅 상태로 노출되지 않는다.
    - `NetworkConnectivityMonitor`는 active network 존재만 확인하므로 Wi-Fi 연결 후 실제 인터넷 접근 불가 상태를 구분하지 못한다.
    - `LocalAppDataResetter`는 Room·저장 이미지·최근 검색·세션·온보딩 상태 전체 초기화를 지원하지만, 동일 계정 재인증과 다른 계정 전환을 구분하는 소유자 ID 및 선택적 wipe 정책은 없다.
  - Required auth state: 토큰 존재 여부만 사용하지 말고 최소 `SignedOut`, `Usable`, `RefreshNeeded`, `TemporarilyUnverified`(오프라인/timeout/5xx), `ReauthRequired`(refresh token 만료·폐기 확정)를 앱 범위의 observable state로 모델링한다. `TemporarilyUnverified`에서는 토큰과 온보딩 완료 상태를 보존하고, 서버의 명시적인 인증 거부에서만 `ReauthRequired`로 전환한다.
  - Desired routing matrix (`hasSession` × `onboardingCompleted`, 세션 유효성 판정은 별도 상태):
    - 세션 X + 온보딩 미완료 → 최초 사용자로 보고 Landing부터 전체 온보딩
    - 세션 O + 온보딩 미완료 → 유효하면 저장된 온보딩 단계 복원. 오프라인이면 유효성 판정을 보류하고 서버 작업 전까지 진행. 무효 확정이면 Landing
    - 세션 X + 온보딩 완료 → 재방문 사용자용 `Reauth` 로그인 모드. 로그인 후 튜토리얼을 건너뛰고 Main
    - 세션 O + 온보딩 완료 → splash에서 원격 검증을 기다리지 않고 Main 진입. 유효/갱신 성공이면 일반 사용, 오프라인·timeout이면 캐시 기반 제한 모드, 무효 확정이면 `Reauth`로 전환
  - Refresh policy:
    - 고정 간격 timeout 10회 반복은 사용하지 않는다. 앱 foreground 진입, `NET_CAPABILITY_VALIDATED` 네트워크 복구, 인증 요청의 401, 토큰 만료 임박, 홈/컬렉션의 사용자 `다시 시도`를 갱신 트리거로 사용한다.
    - 일시 실패를 자동 재시도한다면 exponential backoff+jitter와 foreground 단위 상한을 두고, 네트워크 복구 또는 사용자 수동 갱신에서 횟수를 초기화한다. refresh token 무효/만료가 확정되면 재시도 없이 `ReauthRequired`로 전환한다.
    - 동시 API의 중복 refresh는 기존 single-flight를 유지하고, foreground/background 전환 및 프로세스 재생성 후에도 라우팅 결과가 일관되어야 한다.
  - Offline UX/data policy:
    - 인터넷 연결 확인 불가를 이유로 splash에 사용자를 잡아두지 않는다.
    - 홈/컬렉션은 공통 인터넷 연결 없음 안내와 수동 새로고침을 제공하고, 캐시 데이터가 있으면 계속 표시한다. 조회 외 업로드·분석·수정·삭제를 차단할지, 재연결 후 실행할 작업 큐를 둘지는 별도 확정한다.
    - 빈 데이터와 오프라인으로 불러오지 못한 상태를 구분하고 raw exception 또는 세션 내부 정보를 노출하지 않는다.
  - Reauthentication policy:
    - 인증 손실은 온보딩 초기화가 아니다. `onboardingCompleted`를 유지한 채 Landing UI를 재사용하는 별도 `Reauth` 모드를 두고, 로그인 성공 후 튜토리얼 없이 Main으로 이동한다.
    - 재인증 중 back 동작, 기존 상세/deep link/공유 intent/진행 중 분석의 보존·폐기, 로그아웃과 강제 재인증의 UX 차이를 확정해야 한다.
  - Account isolation/wipe:
    - 서버가 발급하는 불변 RECAP `userId`를 로컬 데이터 소유자 ID로 저장하는 방식을 우선한다. Kakao ID는 서버 계정 병합·다중 provider 정책과 충돌할 수 있어 서버 ID가 없을 때만 대안으로 검토한다.
    - 재로그인 후 이전 소유자 ID와 동일하면 로컬 DB·이미지·검색 기록을 유지하고, 다르면 새 계정 데이터를 노출하기 전에 Room·저장 이미지·최근 검색·계정 종속 캐시를 원자적으로 wipe한다. 온보딩 완료 플래그와 앱 공통 설정의 유지 범위를 데이터별로 명시한다.
    - wipe 실패 시 Main 진입을 막고 복구 가능한 오류를 제공한다. 소유자 ID가 없는 기존 설치 데이터의 최초 마이그레이션은 안전 우선 wipe 또는 1회 계정 귀속 중 하나로 결정한다.
  - Decisions needed: (1) 다른 계정 재로그인 허용 여부 (2) 서버 불변 `userId` 계약 (3) 오프라인에서 허용할 읽기/쓰기 범위와 작업 큐 여부 (4) 자동 재시도 횟수·총 시간 budget (5) `Reauth`의 back/deep link/공유/분석 복원 정책 (6) 계정별 데이터와 기기 공통 설정의 분류 (7) 로그아웃·회원 탈퇴·세션 만료 각각의 wipe 범위
  - Validation scope: 위 상태별 콜드스타트와 런타임 전환, offline→online 복구, timeout/5xx/401/invalid refresh, 동일·다른 계정 로그인, wipe 실패, 프로세스 재생성을 단위/통합 테스트 행렬로 검증한다. 실제 네트워크 복구와 화면 전환은 에뮬레이터 또는 실기기 런타임 검증을 포함한다.
  - Next: 서버 인증 오류 코드와 user identity 계약을 먼저 확정한 뒤 `AuthSessionManager` 역할, root navigation state, 재인증 화면, 계정 소유권 저장/wipe transaction, 홈·컬렉션 수동 갱신 순으로 handoff를 분리한다.
  - Depends: 서버 refresh/user identity 계약
  - Handoff: not started

- [ ] 2026-07-18 - `docs/LOCAL_DATA.md`를 CaptureDetailResponse 동기화 스키마에 맞게 갱신
  - Context: 스크린샷 mock 계약이 `captureId: Long` / `typeCode` / `organizedAt` / Room v1로 리셋되었지만 `LOCAL_DATA.md`는 여전히 imageId·key_fields·migration 설명을 담고 있음
  - Next: LOCAL_DATA.md의 screenshot_cards / repository / image storage 섹션을 현재 구현과 맞추고 레거시 migration 문서 제거
  - Handoff: not started

- [ ] 2026-07-16 - 한국어 텍스트 overflow 대응
  - Context: 설정·계정 관리 등 UI에서 긴 한국어 라벨/이메일/버튼 문구가 잘리거나 레이아웃을 깨뜨릴 수 있음. `RecapButton`·설정 row·계정 관리 화면 등 maxLines/ellipsis/가변 폭 정책이 통일되어 있지 않음
  - Next: 공통 텍스트 overflow 규칙(줄 수, ellipsis, textWrap, 버튼 compactText)을 정한 뒤 design/feature 컴포넌트에 일괄 적용
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

- [ ] 2026-07-08 - Coroutine dispatcher DI 패턴 도입
  - Context: 현재 `Dispatchers.IO` 직접 사용과 `@VisibleForTesting` 테스트 훅이 섞여 있어 비동기 코드 테스트 제어 방식이 일관되지 않음. `@IoDispatcher` 등 qualifier 기반 Hilt provider를 추가하고 ViewModel/Repository/Storage의 blocking work dispatcher를 생성자 주입으로 점진 이전할 필요가 있음
  - Handoff: not started

- [ ] 2026-07-10 - minSdk 30에서 Haze 미지원 fallback 구현
  - Context: 현재 `minSdk = 30`인데 `dev.chrisbanes.haze` glass/blur 효과가 API 30 기기에서 정상 적용되지 않음. `RecapBottomBar`, `RecapHazeFolderCard`, 홈/보관함 `hazeSource` 연동 등 Haze 사용 UI에 대체 렌더링(반투명 배경·단색 tint 등) fallback이 필요함
  - Handoff: not started

- [ ] 2026-07-10 - Navigation3 entry별 ViewModel 수명 범위 구성
  - Context: 각 `NavDisplay`에 ViewModelStore entry decorator가 없어 화면의 Hilt ViewModel이 Activity 범위에 남고, pop 이후에도 화면 상태·Room Flow·대용량 이미지 목록이 유지되거나 재진입 시 재사용될 수 있음
  - Next: `lifecycle-viewmodel-navigation3`과 `rememberViewModelStoreNavEntryDecorator()`를 내비게이션 계층에 적용하고, 의도적으로 공유할 ViewModel만 상위 범위로 분리
  - Handoff: not started

- [ ] 2026-07-24 - RecapNavDisplay OverlayScene/공유 요소 lifecycle 패리티
  - Context: `feature/033-navigaion-3-animation`에서 `RecapNavDisplay`를 공식 `NavDisplay` thin wrapper로 되돌림. OverlayScene/shared-element 패리티는 회복 가능 방향. 대신 저항 preview(`PredictiveMaxFraction` remapping), custom commit completion, interrupted-transition planner/back commit queue는 포기하고 edge full-range predictive + `EDGE_NONE` None으로 단순화함
  - Next: OverlayScene/공유 요소가 실제로 필요해지면 공식 `NavDisplay` 확장 훅을 사용하고, 빠른 역전 입력 레이어 회귀가 나면 planner 재도입 여부를 재평가
  - Note: 2026-07-23 공식 NavDisplay 복원 방향과 다시 정렬. Main tab Home↔Collection은 계속 공식 `NavDisplay` + predictive None
  - Handoff: not started

- [ ] 2026-07-12 - `:core:design` → `:core:model` 의존성 재검토
  - Context: `ScreenshotContentType` 라벨과 `RecapCategoryType` 매핑을 공통화하면서 `:core:design`이 도메인 모델에 의존함. 현재는 관련 로직이 작아 별도 모듈 대신 실용적인 구조로 허용
  - Next: 카테고리 매핑이 늘거나 다른 도메인 모델 의존성이 추가될 때 presentation/UI mapping 계층 분리 또는 feature별 매핑 재배치를 검토
  - Priority: Low
  - Handoff: not started

## In Progress

- 없음

## Done

- [x] 2026-07-10 - DataStore 읽기·손상 오류에도 앱 시작 복구 경로 추가
  - Result: `ReplaceFileCorruptionHandler`로 손상 파일을 `emptyPreferences()`로 교체하고, `safeData()`가 읽기 `IOException`을 즉시 기본값으로 폴백한다(재시도·startup Error UI 없음). 5개 DataStore 소비처에 적용, corruption/`safeData` 단위 테스트 추가
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
  - Next: 서버 content update API 확정 후 `CaptureMutationRepository`에 updateContent를 추가하고 `ScreenshotViewModel.saveEdit`을 Switching mutation으로 위임
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
