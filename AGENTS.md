# AGENTS.md - RECAP

> 작업 전 `docs/PROJECT.md`를 읽고 프로젝트 사실/컨벤션을 확인한다.
> `docs/handoff/HANDOFF.md`는 `codex-plan` -> `cursor-implement` -> `codex-review` 3단 handoff 워크플로우를 사용할 때만 단일 활성 작업 채널로 취급한다.

## 역할

- Codex: 기획, 설계, 스펙 작성, 구현 결과 검토를 맡는다.
- Cursor: 3단 handoff 워크플로우에서는 `docs/handoff/HANDOFF.md`에 정리된 스펙 구현을 맡는다. 이외의 경우에는 단순 구현만 진행한다.
- 작은 수정이나 사용자가 Cursor 단독 구현을 명시한 작업은 3단 handoff 없이 진행할 수 있다.
- 두 에이전트가 공유해야 하는 후속 항목은 `docs/BACKLOG.md`에 기록한다.
- 테스트/검증 정책은 `docs/TESTING.md`를 따른다.

## 3단 handoff 워크플로우 적용 조건

다음 스킬을 명시적으로 사용할 때만 `docs/handoff/HANDOFF.md` 기반 3단 워크플로우를 따른다.

1. `codex-plan`: Codex가 요구사항을 구현 가능한 handoff 스펙으로 정리한다.
2. `cursor-implement`: Cursor가 `READY_FOR_IMPL` 상태의 handoff만 구현한다.
3. `codex-review`: Codex가 Cursor 구현 결과를 handoff 기준으로 검토한다.

3단 handoff 워크플로우를 사용하지 않는 작업에서는 `HANDOFF.md`를 읽거나 수정할 필요가 없다. 단, 작업자가 현재 작업과 직접 관련 있다고 판단하거나 사용자가 요청한 경우에는 확인할 수 있다.

## Codex 원칙

- `codex-plan` 사용 시 직접 구현하지 않고 `HANDOFF.md` 작성까지만 진행한다. 기본값은 Cursor가 구현을 진행한다.
- `codex-plan`으로 작성하는 스펙에는 목적, 변경 범위, 파일 범위, acceptance criteria, 검증 방법을 포함한다.
- `codex-plan` 사용 시 구현자가 추측하지 않도록 모호한 부분을 먼저 정리한다.
- `codex-review` 사용 시 버그, 스펙 불일치, 누락된 검증, 불필요한 범위 확장을 우선 확인한다.
- `codex-review` 완료 후에는 `HANDOFF.md`를 `docs/handoff/archive`에 아카이빙한다.
- 3단 handoff 밖에서 사용자가 Codex 직접 구현을 요청한 경우에도 `PROJECT.md` 컨벤션과 현재 코드 스타일을 따른다.

## Cursor 원칙

- `cursor-implement` 사용 시 `HANDOFF.md`의 Status가 `READY_FOR_IMPL`일 때만 구현을 시작한다.
- `cursor-implement` 사용 시 `HANDOFF.md`의 Spec을 정확히 구현하고 스코프를 넓히지 않는다.
- `cursor-implement` 사용 시 스펙이 모호하면 추측하지 말고 `Cursor Result`의 `Open questions`에 적고 Status를 `BLOCKED`로 둔다.
- 관련 없는 파일이나 사용자 변경을 되돌리지 않는다.
- `cursor-implement` 완료 후 `HANDOFF.md`의 `Cursor Result`를 작성하고 Status를 `REVIEW_NEEDED`로 바꾼다.
- `cursor-implement` 중 발견한 스코프 밖 기술 부채는 `docs/BACKLOG.md`에 저장하고, `HANDOFF.md`의 `Technical Debt`에는 저장 여부만 요약한다.
- Cursor 단독 작업에서는 관련 코드와 `PROJECT.md`/`TESTING.md` 컨벤션을 기준으로 구현하고, 필요한 검증 결과만 간결히 보고한다.

## 필수 절차

1. `docs/PROJECT.md`를 읽고 현재 프로젝트 사실과 컨벤션을 확인한다.
2. 구현 또는 검토에 필요한 최소 범위의 코드만 확인한다.
3. 변경 범위는 사용자 요청과 직접 관련 파일로 제한한다.
4. 변경 후 `docs/TESTING.md` 기준에 맞는 검증을 수행한다.
5. 3단 handoff 워크플로우를 사용하는 경우에만 `docs/handoff/HANDOFF.md`를 읽고 Status, Spec, Files to touch, Acceptance criteria를 확인한다.
6. 3단 handoff 워크플로우를 사용하는 경우에만 결과를 `HANDOFF.md`의 Result 섹션에 기록한다.

## 프로젝트 컨벤션 요약

- 현재 앱은 화이트모드 우선이다.
- UI 텍스트는 `app/src/main/res/values/strings.xml`에 정의해 사용한다.
- UI 컴포넌트와 화면 구현 시 필요한 Preview를 함께 작성한다.
- Compose Preview는 실제 앱 테마와 동일하게 `RECAPTheme`로 감싼다.
- 아이콘을 Canvas나 텍스트로 대체하지 않는다.
- 한 파일에 두 개 이상의 screen을 넣지 않는다.
- 파일이 과도하게 길어지면 역할별로 분리한다.
- `RecapButton`에 elevation을 줄 경우 12.dp를 사용한다.
- Figma MCP 사용 중 문제가 생기면 작업을 중지하고 보고한다.

## 빌드 명령

PowerShell 기본 debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

## Result 작성 예시

```markdown
## Cursor Result
- Changed files: app/.../MainActivity.kt, app/.../ui/AppRoot.kt
- Build/test: .\gradlew.bat assembleDebug GREEN
- Open questions: none
```
