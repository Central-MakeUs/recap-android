# PROJECT.md - RECAP

이 문서는 RECAP 프로젝트의 사실과 컨벤션을 기록하는 단일 진실원천이다. Codex와 Cursor는 작업 전 이 문서를 먼저 확인한다.

## 프로젝트 정체성

- 앱 이름: RECAP
- 현재 목표: 사용자가 선택하거나 공유한 스크린샷을 서버 OCR/AI 분석으로 정리하고, 컬렉션으로 관리한다.
- 현재 지원 방향: 화이트모드 우선

## 패키지 / 앱 정보

- root project name: `RECAP`
- 패키지명: `com.chalkak.recap`
- minSdk: 30
- targetSdk: 37
- compileSdk: 37
- versionCode: 1
- versionName: `1.0.0`
- MainActivity: `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- Application: `app/src/main/java/com/chalkak/recap/RecapApplication.kt`

## 현재 기술 스택

- Language: Kotlin
- UI: Jetpack Compose, Material 3
- Theme: `RECAPTheme`
- Architecture: 현재는 feature별 MVVM + UiState/Action 계약을 사용한다.
- Navigation: AndroidX Navigation3
- DI: Hilt
- Annotation processing: KSP
- Async: Kotlin Coroutines / Flow
- Persistence: DataStore, Room
- Image loading: Coil
- Logging: Timber
- Crash reporting: Firebase Crashlytics
- Performance: Firebase Performance Monitoring (auto + organize/share custom traces)

## 빌드 명령

PowerShell 기본 debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

오프라인 빌드는 사용자가 요청했거나 의존성 변경이 없고 로컬 캐시만으로 충분하다고 판단될 때만 사용한다.

## 모듈 맵

현재는 Android application 모듈과 core/feature library 모듈로 구성된 멀티모듈 구조다.

```text
:app
:core:data
:core:design
:core:model
:feature:collection
:feature:developer
:feature:home
:feature:settings
:feature:onboarding
:feature:organize
:feature:screenshot
```

현재 주요 package / feature map:

```text
com.chalkak.recap
├── app                    # 앱 루트, root navigation, main tab shell
├── core
│   ├── data               # DataStore, Room, screenshot repository, network
│   ├── design             # theme, common components
│   └── model              # 앱 공통 모델
└── feature
    ├── collection         # 컬렉션 화면
    ├── developer          # 개발자 옵션 / component garden
    ├── home               # 홈 (search/, recent/ 하위 패키지)
    ├── settings           # 설정 (account/, data/, notification/, guide/)
    ├── organize           # 정리/분류 관련 화면
    ├── onboarding         # 온보딩 플로우
    └── screenshot         # 스크린샷 상세/수정/전체화면

core.data.screenshot 하위:
    permission/ · analysis/ · backend/ · image/ · persistence/
```

## 목표 아키텍처

현재 멀티모듈 MVVM + UDF/MVI 스타일을 목표 구조로 유지한다.

모듈 방향:

- `:app`은 app shell, navigation composition, DI entry point 중심으로 유지한다.
- `:core:*`는 design, model, data, common util 등 재사용 계층으로 분리한다.
- `:feature:*`는 화면 단위 기능 모듈로 분리한다.
- 화면 상태는 immutable UiState로 표현한다.
- UI 이벤트는 Action/Event로 명시한다.
- ViewModel은 state 생산과 action 처리에 집중한다.
- Repository는 데이터 소스와 외부 연동 세부사항을 감춘다.

## 현재 앱 흐름

- `MainActivity`가 splash 유지 조건과 edge-to-edge 설정을 담당한다.
- `RecapApp`이 `RECAPTheme`로 앱을 감싸고 root route를 결정한다.
- `RecapStartupViewModel`이 `onboardingCompleted`와 `AuthSessionStateProvider.hasSession`(refresh token 보유 여부)을 합쳐 `RecapEntryMode`(`Onboarding` / `Reauth` / `Main`)를 파생한다. 네트워크 상태는 entry 입력이 아니며, refresh token이 서버에서 무효/만료로 확정되어 clear될 때만 세션 없음으로 전환한다. 온보딩·Reauth 로그인 전 오프라인은 `NetworkConnectivityMonitor.isInternetValidated()` + `RecapPopup`으로 안내한다. Main Home/Collection/Search는 오프라인 캐시 읽기 없이 로드 실패 Error UI와 수동 재시도를 쓰고, foreground 복귀 또는 validated 네트워크 복구 시 Error일 때만 자동 refresh를 1회 시도한다.
- root route:
  - `Onboarding`
  - `Reauth` (온보딩 완료 사용자가 세션을 잃었거나 로그아웃/탈퇴했을 때. `OnboardingLandingScreen`을 그대로 재사용해 로그인만 처리하고, 성공 시 튜토리얼 없이 Main으로 복귀한다. 로그인 시 카카오 user.id 해시가 없으면/다르면 로컬 계정 데이터를 wipe한다. 자발적 로그아웃/탈퇴에서는 세션 만료 토스트를 띄우지 않는다)
  - `Main`
  - `Developer`
- main route:
  - Home
  - Collection
  - Settings(설정) 및 하위 화면
- Organize(스크린샷 피커 → 확인)는 AppRoute가 아니라 MainTabs 위 오버레이다. 구조·의도·back 동작은 `docs/ORGANIZE_OVERLAY_NAVIGATION.md`를 본다.
- 온보딩 `StartFirstAnalyze`의 "스크린샷 선택하기"는 root `Onboarding`을 유지한 채 같은 entry 위에 `OrganizeRoute`(피커 → 확인)를 오버레이한다. 분석 진행/결과도 온보딩 위에서 표시하며, terminal 결과의 `완료/닫기` 이후에만 온보딩을 완료하고 Main Home으로 이동한다. "나중에 하기"는 분석 없이 즉시 온보딩을 완료하고 홈만 연다.

## 데이터 / 외부 연동

- `UserPreferencesRepository`: DataStore 기반 사용자 설정 및 온보딩 상태 관리
- `RecapDatabase`: Room database (`screenshot_cards` 등)
- 스크린샷 분석/저장:
  - `ScreenshotCardRepository`
  - `ScreenshotAnalysisRepository` (build-time Mock/Remote 선택, 요약 `docs/ANALYSIS_DATA_SOURCE.md`, 과거 런타임 전환 스냅샷 `docs/MOCK_REMOTE_CHANGE.md`)
  - 동일 기준이 분석뿐 아니라 Home/Storage/Capture command 등 스크린샷 도메인 전역 backend에 적용된다.
  - `LocalScreenshotDataSource` / `ImagePermissionRepository`
- OCR/AI 분석은 서버에서 수행한다. 로컬 ML Kit OCR 및 Firebase AI 클라이언트는 사용하지 않는다.
- 스크린샷 도메인 backend는 `:core:data`의 `BuildConfig.USE_MOCK_BACKEND`로 프로세스 수명 동안 고정된다.
  - 기본값: debug `true`, qa/release `false` (qa/release는 Remote 고정)
  - debug 덮어쓰기: `-PUSE_MOCK_BACKEND=true|false`가 있으면 그걸 쓰고, 없으면 `local.properties`의 `USE_MOCK_BACKEND`(Kakao 키와 동일)를 쓴다. 둘 다 없으면 debug 기본값은 Mock(`true`). release/qa는 `-P`/local.properties로도 Mock으로 바뀌지 않는다.
  - Hilt 모듈이 Mock 또는 Remote concrete repository를 한 번 선택해 제공한다.

외부 API, Firebase, local.properties, google-services 파일, API key 등 시크릿은 커밋하지 않는다.

## 디자인 패턴

UI 색상·타이포는 `MaterialTheme.colorScheme` / `MaterialTheme.typography`보다 `core/design/theme` 디자인 토큰을 우선 사용한다.

- **RecapColor** — `core/design/src/main/java/com/chalkak/recap/core/design/theme/Color.kt`에 정의된 `RecapBlue*`, `RecapGray*`, `RecapCategory*` 등 색상 토큰
- **RecapTypo** — `core/design/src/main/java/com/chalkak/recap/core/design/theme/Type.kt`의 `RecapTypography` (`RecapHeading*`, `RecapBody*`, `RecapCaption*`). 사용 시 `import ...RecapTypography.RecapBody1`처럼 멤버를 직접 import하고 `style = RecapBody1`로 쓴다. `MaterialTheme.typography` / `RecapTypography.RecapBody1` 형태는 쓰지 않는다.

시맨틱 색·Material 역할 매핑이 필요할 때만 `MaterialTheme`을 보조로 쓴다. 색·타이포 임의 하드코딩은 금지한다.

## 컨벤션 (필수)

- 테마 토큰만 사용: 색/타이포는 위 디자인 패턴의 RecapColor·RecapTypo를 우선한다.
- Material 아이콘 금지: 새 작업에서 `Icons.*`를 추가하지 않는다. 필요한 아이콘은 drawable/vector asset으로 추가하거나 handoff에 요청한다. 아이콘을 Canvas나 텍스트로 대체하지 않는다.
- 문자열 리소스 필수: UI 텍스트는 하드코딩하지 않고 모두 `core/design/src/main/res/values/strings.xml`에 정의해 사용한다.
- Preview 필수: UI 컴포넌트와 화면에는 필요한 Preview를 작성하고 `RECAPTheme`로 감싼다. `core/design` 컴포넌트는 variant/state가 여러 개면 그만큼 Preview를 둔다. 기존 예시는 `RecapButton.kt`, `RecapActionBottomSheet.kt` 참고.
- 화면/파일 분리: 하나의 파일에 두 개 이상의 screen을 넣지 않는다. 파일이 길어지면 `Route`, `Screen`, `Contract`, `ViewModel`, `component`, `Previews`로 분리한다.
- 상태/액션 규칙: 화면 상태는 immutable `UiState` data class로 두고, 사용자 액션은 sealed interface로 명시한다.
- 주석: 자명한 코드에 주석을 달지 않는다. 외부 연동, 정책, 권한, 작업 스케줄링처럼 동작/함정/플랫폼 제약이 있는 경우에만 짧은 KDoc 또는 주석을 허용한다.
- 에러 처리: 사용자에게 raw exception message를 그대로 노출하지 않는다. 복구 가능한 에러는 다시 시도하거나 다음 행동을 할 수 있게 안내한다.
- 커밋: 기존 히스토리처럼 `type(scope): 한국어 설명` 형식을 사용한다. 예: `feat(onboarding): 온보딩 단계에서 이미지 권한 요구 추가`, `fix(entry): 진입 화면 상태 복원 오류 수정`, `test(gradle): JUnit5 테스트 의존성 및 플랫폼 설정 추가`. 단순 영문 문장형 커밋 메시지는 쓰지 않는다.
- 시크릿: `local.properties`, `google-services.json`, API key, Firebase/외부 서비스 키는 커밋 금지.

## 공유 문서

- `docs/PROJECT.md`: 프로젝트 사실과 컨벤션
- `docs/BACKLOG.md`: 두 에이전트가 공유해야 하는 후속 항목
- `docs/TESTING.md`: 테스트/검증 정책
- `docs/qa/GUIDE.md`: 디자인 QA(스크린샷·VM) 오케스트라 지침
- `docs/LOCAL_DATA.md`: 로컬 데이터 구현체와 저장 정책
- `docs/ANALYSIS_DATA_SOURCE.md`: 스크린샷 Mock/Remote build-time (`USE_MOCK_BACKEND`) 선택
- `docs/MOCK_REMOTE_CHANGE.md`: 제거된 런타임 Mock/Remote 전환 계층 역사 스냅샷
- `docs/handoff/HANDOFF.md`: Codex가 작성하고 Cursor가 구현할 작업 스펙
