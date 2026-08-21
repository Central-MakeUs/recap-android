# AGENTS.md - RECAP

> 작업 전 `docs/PROJECT.md`를 읽고 프로젝트 사실/컨벤션을 확인한다.
> `docs/handoff/HANDOFF.md`는 `codex-plan` -> `cursor-implement` -> `codex-review` 3단 handoff 워크플로우를 사용할 때만 단일 활성 작업 채널로 취급한다.

## 역할

- Codex: 기획, 설계, 스펙 작성, 구현 결과 검토를 맡는다.
- Cursor: 3단 handoff 워크플로우에서는 `docs/handoff/HANDOFF.md`에 정리된 스펙 구현을 맡는다. 이외의 경우에는 단순 구현만 진행한다.
- 작은 수정이나 사용자가 Cursor 단독 구현을 명시한 작업은 3단 handoff 없이 진행할 수 있다.
- 두 에이전트가 공유해야 하는 후속 항목은 `docs/BACKLOG.md`에 기록한다.
- 테스트/검증 정책은 `docs/TESTING.md`를 따른다.

---

## 3단 handoff 워크플로우 적용 조건

다음 스킬을 명시적으로 사용할 때만 `docs/handoff/HANDOFF.md` 기반 3단 워크플로우를 따른다.

1. `codex-plan`: Codex가 요구사항을 구현 가능한 handoff 스펙으로 정리한다.
2. `cursor-implement`: Cursor가 `READY_FOR_IMPL` 상태의 handoff만 구현한다.
3. `codex-review`: Codex가 Cursor 구현 결과를 handoff 기준으로 검토한다.

3단 handoff 워크플로우를 사용하지 않는 작업에서는 `HANDOFF.md`를 읽거나 수정할 필요가 없다.
단, 작업자가 현재 작업과 직접 관련 있다고 판단하거나 사용자가 요청한 경우에는 확인할 수 있다.

---

## Codex 원칙

- `codex-plan` 사용 시 직접 구현하지 않고 `HANDOFF.md` 작성까지만 진행한다. 기본값은 Cursor가 구현을 진행한다.
- `codex-plan`으로 작성하는 스펙에는 목적, 변경 범위, 파일 범위, acceptance criteria, 검증 방법을 포함한다.
- `codex-plan` 사용 시 구현자가 추측하지 않도록 모호한 부분을 먼저 정리한다.
- `codex-review` 사용 시 버그, 스펙 불일치, 누락된 검증, 불필요한 범위 확장을 우선 확인한다.
- `codex-review` 완료 후에는 `HANDOFF.md`를 `docs/handoff/archive`에 아카이빙한다.
- 3단 handoff 밖에서 사용자가 Codex 직접 구현을 요청한 경우에도 `PROJECT.md` 컨벤션과 현재 코드 스타일을 따른다.

---

## Cursor 원칙

- `cursor-implement` 사용 시 `HANDOFF.md`의 Status가 `READY_FOR_IMPL`일 때만 구현을 시작한다.
- `cursor-implement` 사용 시 `HANDOFF.md`의 Spec을 정확히 구현하고 스코프를 넓히지 않는다.
- `cursor-implement` 사용 시 스펙이 모호하면 추측하지 말고 `Cursor Result`의 `Open questions`에 적고 Status를 `BLOCKED`로 둔다.
- 관련 없는 파일이나 사용자 변경을 되돌리지 않는다.
- `cursor-implement` 완료 후 `HANDOFF.md`의 `Cursor Result`를 작성하고 Status를 `REVIEW_NEEDED`로 바꾼다.
- `cursor-implement` 중 발견한 스코프 밖 기술 부채는 `docs/BACKLOG.md`에 저장하고, `HANDOFF.md`의 `Technical Debt`에는 저장 여부만 요약한다.
- Cursor 단독 작업에서는 관련 코드와 `PROJECT.md`/`TESTING.md` 컨벤션을 기준으로 구현하고, 필요한 검증 결과만 간결히 보고한다.

---

## 필수 절차

1. `docs/PROJECT.md`를 읽고 현재 프로젝트 사실과 컨벤션을 확인한다.
2. 구현 또는 검토에 필요한 최소 범위의 코드만 확인한다.
3. 변경 범위는 사용자 요청과 직접 관련 파일로 제한한다.
4. 기존 naming, package, architecture, code style을 따른다.
5. 단순한 취향 차이 때문에 동작 중인 코드를 다시 작성하거나 요청과 무관한 리팩터링을 하지 않는다.
6. 변경 후 `docs/TESTING.md` 기준에 맞는 테스트·빌드와 필요한 레이아웃 QA를 수행한다.
7. 3단 handoff 워크플로우를 사용하는 경우에만 `docs/handoff/HANDOFF.md`를 읽고 Status, Spec, Files to touch, Acceptance criteria를 확인한다.
8. 3단 handoff 워크플로우를 사용하는 경우에만 결과를 `HANDOFF.md`의 Result 섹션에 기록한다.

---

## 프로젝트 컨벤션 요약

- 현재 앱은 화이트모드 우선이다.
- UI 텍스트는 하드코딩하지 않고 모두 `core/design/src/main/res/values/strings.xml`에 정의해 사용한다.
- UI 컴포넌트와 화면 구현 시 필요한 Preview를 함께 작성한다.
- Compose Preview는 실제 앱 테마와 동일하게 `RECAPTheme`로 감싼다.
- 아이콘을 Canvas나 텍스트로 대체하지 않는다.
- 한 파일에 두 개 이상의 screen을 넣지 않는다.
- 파일이 과도하게 길어지면 역할별로 분리한다.
- 최소 API는 30이다.

---

## 의존성 추가 규칙

새 production dependency는 다음 조건을 만족할 때만 추가한다.

1. 구현 또는 유지보수 비용을 의미 있게 줄인다.
2. 현재 유지보수되고 있는 라이브러리다.
3. 설정 및 도입 비용이 과도하지 않다.
4. 현재 프로젝트 아키텍처와 충돌하지 않는다.

추가 원칙:

- 새 의존성을 추가하기 전에 기존 version catalog와 build script를 먼저 확인한다.
- 의존성을 추가할 경우 왜 필요한지 설명한다.
- 몇 줄로 안전하게 구현 가능한 작은 유틸리티를 위해 새 의존성을 추가하지 않는다.

---

## Android 도구 및 Skill 사용

- 일반적인 코드 확인과 검증은 파일 검색, shell, Gradle 등 가장 가벼운 방법을 우선한다.
- 단순 파일 확인을 위해 IDE/MCP나 런타임 도구를 불필요하게 사용하지 않는다.
- Compose의 복잡한 state, side-effect, recomposition 또는 performance 문제는 `compose-expert`를 사용한다.
- ADB, logcat, dumpsys, crash, ANR 분석이 필요한 경우 `android-adb-debugging`을 사용한다.
- 실제 디바이스/에뮬레이터 화면 또는 사용자 플로우 검증이 필요한 경우 `android-mobile-mcp`를 사용한다.
- Android Studio / JetBrains MCP는 IDE inspection, run configuration, 프로젝트 구조 등 IDE 관점의 정보가 실제로 필요할 때 사용한다.
- 앱 데이터 삭제, uninstall, `pm clear` 등 파괴적인 작업은 사용자의 명시적인 요청 없이 수행하지 않는다.
- 동일한 목적의 검증을 여러 도구로 불필요하게 반복하지 않는다.

---

## 에러 처리

사용자에게 보이는 에러는 다음 원칙을 따른다.

- 가능한 경우 사용자가 이해하고 복구할 수 있는 메시지를 제공한다.
- 다시 시도할 수 있는 작업에는 적절한 retry 경로를 제공한다.
- raw exception message를 사용자에게 그대로 노출하지 않는다.
- 기술적인 세부 정보는 debug-safe한 위치에 기록한다.

MVP 또는 프로토타입 단계에서는 요구사항과 작업 범위에 따라 단순한 error state도 허용한다.

---

## 빌드

PowerShell 기본 debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
````

* 의존성 추가나 Gradle plugin 변경이 있는 경우 처음부터 `--offline`을 강제하지 않는다.
* 필요한 경우 dependency resolution을 위해 online build를 허용한다.
* 구체적인 테스트 및 검증 범위는 `docs/TESTING.md`를 따른다.

---

## 완료 기준

작업은 다음 조건을 만족해야 완료된 것으로 본다.

1. 요청된 동작 또는 변경이 구현되어 있다.
2. 요청과 무관한 파일이나 동작을 변경하지 않았다.
3. `docs/TESTING.md` 기준에 맞는 필요한 검증을 수행했다.
4. 검증이 실패하거나 수행할 수 없는 경우 원인과 영향 범위를 명확히 보고했다.
5. 외부 API, 백엔드 또는 불명확한 동작에 대한 중요한 가정을 명시했다.
6. 위험 요소나 남은 작업이 있다면 숨기지 않고 보고했다.

---

## 작업 완료 응답 형식

작업 완료 후 필요한 항목만 간결히 보고한다.

### 변경 사항

* 변경한 내용과 주요 파일

### 검증

* 실행한 검증과 결과

### 가정 / 남은 작업

* 중요한 가정, 미검증 사항, 남은 TODO가 있는 경우에만 작성

사용하지 않은 도구를 모두 나열하지 않는다.

수행하지 않은 검증은 결과의 신뢰성에 영향을 주는 경우에만 이유를 보고한다.

Android Studio / JetBrains MCP, ADB, mobile-mcp 등 런타임 또는 IDE 기반 도구를 실제로 사용한 경우에는 수행한 조작과 핵심 결과를 간결히 명시한다.

위험하거나 불확실한 부분은 조용히 추측하지 말고 명시적으로 보고한다.

---

## Cursor Result 작성 예시

```markdown
## Cursor Result

- Changed files: app/.../RecapStartupViewModel.kt, app/.../RecapStartupViewModelTest.kt
- Build/test: .\gradlew.bat testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN
- Design QA: not applicable - no layout change
- Open questions: none
```
