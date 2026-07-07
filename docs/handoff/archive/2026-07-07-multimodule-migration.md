# HANDOFF.md - RECAP

이 문서는 Codex가 작성하고 Cursor가 구현하는 단일 활성 작업 채널이다. 완료되면 `docs/handoff/archive/<날짜>-<작업>.md`로 옮기고 이 파일의 각 섹션은 비운다.

## Status

DONE

허용 값:
- `DRAFT`: Codex가 스펙 작성 중
- `READY_FOR_IMPL`: Cursor가 구현 가능
- `BLOCKED`: 구현자가 모호점 또는 외부 조건 때문에 중단
- `REVIEW_NEEDED`: 구현 완료 후 Codex 검토 필요
- `DONE`: Codex 검토까지 완료되어 작업 종료

기본 흐름:

```text
DRAFT -> READY_FOR_IMPL -> REVIEW_NEEDED -> DONE
                         -> BLOCKED
```

## Owner

- Planning / Review: Codex
- Implementation: Cursor

## Context 

현재 코드는 패키지 구조상 `app`, `core`, `feature`로 나뉘어 있지만 Gradle 모듈은 `:app` 단일 모듈이다. `docs/PROJECT.md`의 목표 아키텍처에 맞춰 app shell, core 재사용 계층, feature 화면 계층을 실제 모듈 경계로 분리한다.

## Spec

- Gradle 멀티모듈 구성을 추가한다.
  - `:app`: Application, MainActivity, app shell/navigation composition, DI entry point를 유지한다.
  - `:core:model`: 앱 공통 모델을 둔다.
  - `:core:data`: DataStore, Room, WorkManager, OCR, AI repository와 Hilt module을 둔다.
  - `:core:design`: theme, 공통 Compose component, 공통 UI 리소스를 둔다.
  - `:feature:*`: home, collection, demo, developer, mypage, onboarding 화면/Route/ViewModel/Contract를 둔다.
- 기존 MVVM + UDF/MVI 스타일 계약을 유지한다.
  - 화면 상태는 immutable `UiState` data class로 유지한다.
  - 사용자 입력은 sealed `Action`으로 유지한다.
  - ViewModel은 `StateFlow` 기반 uiState 생산과 action 처리에 집중한다.
- 기능 동작과 화면 디자인은 변경하지 않는다.
- Android library 모듈에서 앱 모듈 `R`에 의존하지 않도록 리소스 참조를 모듈 경계에 맞게 정리한다.
- 새 production dependency는 추가하지 않는다. 필요한 것은 기존 dependency를 모듈별로 재배치한다.

## Files to Touch

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `core/model/build.gradle.kts`
- `core/data/build.gradle.kts`
- `core/design/build.gradle.kts`
- `feature/home/build.gradle.kts`
- `feature/collection/build.gradle.kts`
- `feature/demo/build.gradle.kts`
- `feature/developer/build.gradle.kts`
- `feature/mypage/build.gradle.kts`
- `feature/onboarding/build.gradle.kts`
- `app/src/main/java/com/chalkak/recap/app/**`
- `app/src/main/java/com/chalkak/recap/MainActivity.kt`
- `app/src/main/java/com/chalkak/recap/RecapApplication.kt`
- `app/src/main/AndroidManifest.xml`
- `core/**/src/main/java/**`
- `core/design/src/main/res/**`
- `feature/**/src/main/java/**`
- 필요 시 기존 `app/src/test/**` 테스트 위치 또는 imports

## Acceptance Criteria

- [x] `settings.gradle.kts`에 `:core:*`, `:feature:*` 모듈이 포함되어 있다.
- [x] `:app`은 app shell과 navigation composition만 직접 소유하고, core/feature 구현 코드는 각 모듈로 이동되어 있다.
- [x] feature 모듈은 앱 모듈의 `R` 또는 `BuildConfig`를 import하지 않는다.
- [x] 기존 화면의 `UiState`/`Action`/`ViewModel` UDF 계약이 유지된다.
- [x] debug build가 성공한다.

## Validation

- `$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug`

## Out of scope
- 화면 디자인 변경
- 신규 기능 추가
- 신규 production dependency 추가
- feature별 strings.xml 세분화
- 완전한 build convention plugin 도입

## Technical Debt
<이번 작업 중 해결한 기술 부채, `docs/BACKLOG.md`에 저장한 신규 기술 부채>
- 이번 작업 중 해결한 기술 부채: 없음
- `docs/BACKLOG.md`에 저장한 신규 기술 부채: 없음

작성 규칙:
- 구현 중 발견했지만 이번 스펙과 직접 관련 없는 구조 문제, 중복, 테스트 공백, 임시 구현은 `docs/BACKLOG.md`에 저장한다.
- 이 섹션에는 이번 작업에서 해결했거나 `BACKLOG.md`에 저장한 기술 부채의 요약만 적는다.
- 기술 부채를 이유로 `Files to Touch` 범위 밖 변경을 임의로 수행하지 않는다.

## Cursor Result

- Changed files: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `core/*/build.gradle.kts`, `feature/*/build.gradle.kts`, 모듈별 `src/main` 소스/리소스 이동, `app/src/main/java/com/chalkak/recap/app/**`, `RecapNavHost.kt`, `feature/mypage/MyPageScreen.kt`, 테스트 3건 모듈별 이동
- Build/test: `.\gradlew.bat assembleDebug` GREEN
- Open questions: none

## Codex Review

- Blocking: none
- Nits: none
- Validation: `.\gradlew.bat assembleDebug` GREEN, `.\gradlew.bat testDebugUnitTest` GREEN
- Verdict: DONE
