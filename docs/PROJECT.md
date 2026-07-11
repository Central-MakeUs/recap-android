# PROJECT.md - RECAP

이 문서는 RECAP 프로젝트의 사실과 컨벤션을 기록하는 단일 진실원천이다. Codex와 Cursor는 작업 전 이 문서를 먼저 확인한다.

## 프로젝트 정체성

- 앱 이름: RECAP
- 현재 목표: 사용자가 선택하거나 공유한 스크린샷을 OCR/AI 분석으로 정리하고, 컬렉션으로 관리한다.
- 현재 지원 방향: 화이트모드 우선

## 패키지 / 앱 정보

- root project name: `RECAP`
- 패키지명: `com.chalkak.recap`
- minSdk: 30
- targetSdk: 37
- compileSdk: 37
- versionCode: 1
- versionName: `1.0`
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
- Background work: WorkManager + HiltWorker
- OCR: ML Kit Text Recognition
- AI: Firebase AI
- Image loading: Coil
- Logging: Timber

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
:feature:demo
:feature:developer
:feature:home
:feature:mypage
:feature:onboarding
:feature:organize
:feature:screenshot
```

현재 주요 package / feature map:

```text
com.chalkak.recap
├── app                    # 앱 루트, root navigation, main tab shell
├── core
│   ├── data               # DataStore, Room, WorkManager, OCR, AI repository
│   ├── design             # theme, common components
│   └── model              # 앱 공통 모델
└── feature
    ├── collection         # 컬렉션 화면
    ├── demo               # OCR/AI 분석 데모 및 개발 검증 화면
    ├── developer          # 개발자 옵션 / component garden
    ├── home               # 홈
    ├── mypage             # 마이페이지 및 하위 설정 화면
    ├── organize           # 정리/분류 관련 화면
    ├── onboarding         # 온보딩 플로우
    └── screenshot         # 스크린샷 상세/수정/전체화면
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
- `RecapStartupViewModel`이 온보딩 완료 여부를 판단한다.
- root route:
  - `Onboarding`
  - `Main`
  - `Developer`
- main route:
  - Home
  - Collection
  - My Page 및 하위 화면
- Organize(스크린샷 피커 → 확인)는 AppRoute가 아니라 MainTabs 위 오버레이다. 구조·의도·back 동작은 `docs/ORGANIZE_OVERLAY_NAVIGATION.md`를 본다.

## 데이터 / 외부 연동

- `UserPreferencesRepository`: DataStore 기반 사용자 설정 및 온보딩 상태 관리
- `RecapDatabase`: Room database
- OCR:
  - `OcrRepository`
  - `OcrWorker`
  - `OcrDao`
  - ML Kit text recognition
- AI 분석:
  - `RecapAnalysisRepository`
  - `RecapAnalysisPrompt`
  - `RecapAnalysisSchema`
  - Firebase AI
- WorkManager:
  - HiltWorkerFactory를 `RecapApplication`에서 연결한다.

외부 API, Firebase, local.properties, google-services 파일, API key 등 시크릿은 커밋하지 않는다.

## 컨벤션 (필수)

- 테마 토큰만 사용: 색/타이포는 `MaterialTheme.colorScheme` / `MaterialTheme.typography` 또는 `core/design/theme` 토큰을 사용한다. 임의 하드코딩 금지.
- Material 아이콘 금지: 새 작업에서 `Icons.*`를 추가하지 않는다. 필요한 아이콘은 drawable/vector asset으로 추가하거나 handoff에 요청한다. 아이콘을 Canvas나 텍스트로 대체하지 않는다.
- 문자열 리소스 필수: UI 텍스트는 `app/src/main/res/values/strings.xml`에 정의해서 사용한다.
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
- `docs/LOCAL_DATA.md`: 로컬 데이터 구현체와 저장 정책
- `docs/handoff/HANDOFF.md`: Codex가 작성하고 Cursor가 구현할 작업 스펙
