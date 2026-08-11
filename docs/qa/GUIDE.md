# Design QA Guide (Draft)

> 디자인 QA 오케스트라 지침 초안이다.  
> 작은 화면·고배율·시스템 내비(특히 3버튼 inset)에서 레이아웃이 깨지지 않는지 검증하는 기준을 정의한다.  
> 구현/리뷰 에이전트는 화면 레이아웃 변경 시 이 문서를 따른다.

관련 이슈: [#43 작은 기기·고배율·3버튼 내비 고정 레이아웃/패딩 전역 대응](https://github.com/Central-MakeUs/recap-android/issues/43)

---

## 1. 목적

디자인 QA의 목표는 시각적 회귀와 **레이아웃 붕괴**를 조기에 잡는 것이다.

우선 확인 대상:

- 텍스트·CTA·일러스트 잘림 / 겹침
- 스크롤 없이 뷰포트에 고정된 콘텐츠로 인한 overflow
- system bar / navigation bar inset 미적용 또는 불일치
- fontScale 증가 시 줄바꿈·버튼 높이·간격 붕괴
- compact 폭(320~360dp)에서 고정 `dp` 기반 패딩/일러스트 과다

이 가이드는 기능 버그 QA가 아니라 **레이아웃·간격·가독성** 중심이다.

---

## 2. 오케스트라 역할

오케스트라는 구현을 직접 하지 않고, 아래를 조율한다.

| 역할 | 책임 |
|------|------|
| Orchestra | 검증 매트릭스 확정, 대상 화면 선정, pass/fail 판정 기준 유지, VM/스크린샷 결과 취합 |
| Implementer (Cursor) | 스펙 구현 + Preview/Screenshot 대상 화면 노출 + 로컬 빌드 검증 |
| Reviewer (Codex) | 스펙 일치·누락 매트릭스·회귀 위험 검토 |

### 오케스트라 운영 원칙

1. **매트릭스를 줄이지 않는다.** 아래 Screenshot Spec / VM Spec은 기본 최소 집합이다. 화면 특성상 일부 조합을 생략할 때는 이유를 기록한다.
2. **온보딩 풀뷰포트를 레퍼런스로 둔다.** (#43) 이후 화면은 점진 적용하되, 동일 판정 기준을 쓴다.
3. **실패는 재현 조건과 함께 기록한다.** `(screen, width×height, fontScale, navMode)` 네 튜플을 남긴다.
4. **고정 padding 일괄 축소를 해결로 인정하지 않는다.** scrollable body + pinned CTA + compact 시 일러스트 축소/숨김 + inset 계약 통일 방향과 맞는지 확인한다.
5. **화이트모드만** 검증한다. 다크모드·태블릿·landscape는 현재 범위 밖이다.

---

## 3. Compose Screenshot Test Spec

Compose Screenshot / Preview 캡처의 기본 매트릭스다.  
가능하면 CI 또는 로컬 screenshot test로 자동화하고, 자동화 전이면 Preview + 수동 캡처로 동일 조합을 커버한다.

### 3.1 Screen sizes

| width (dp) | height (dp) | 의도 |
|------------|-------------|------|
| 320 | 640 | 최소 폭 / 짧은 높이 stress |
| 360 | 800 | 소형 일반 (예: 소형 폰 근사) |
| 412 | 915 | 중형 기준선 (회귀 비교용) |

### 3.2 Font scale

| fontScale | 의도 |
|-----------|------|
| 1.0 | 기본 |
| 1.3 | 약간 확대 |
| 1.5 | 접근성 중간 |
| 2.0 | 접근성 최대 stress |

### 3.3 조합 규칙

- 기본 전체 조합: `3 sizes × 4 fontScale = 12` per screen state.
- 공통 Preview 애노테이션: `@QaPhoneMatrix` (`core/design` → `com.chalkak.recap.core.design.qa`). Preview / screenshotTest에서 매트릭스를 한 줄로 적용한다.
- 상태(empty/loading/error/content)가 여러 개면 **대표 state + 가장 붐비는 state** 우선.
- 풀뷰포트 + pinned CTA 화면은 전 조합 필수.
- 리스트/스크롤 화면은 `320×640 @ 2.0`과 `360×800 @ 1.5`를 우선하고, 나머지는 샘플링 가능(생략 시 기록).

### 3.4 Screenshot 판정 (Pass / Fail)

**Fail**

- 주요 CTA가 화면 밖이거나 system nav에 가려짐
- 본문 텍스트가 컨테이너를 뚫고 잘림 (의도된 ellipsis 제외)
- 일러스트와 텍스트/버튼이 겹침
- 가로 overflow로 잘림
- fontScale 2.0에서 스크롤 가능한 body인데도 필수 액션에 도달 불가

**Pass**

- compact에서 일러스트가 축소/숨김되어도 정보 계층이 유지됨
- body는 스크롤 가능하고 bottom actions는 고정·도달 가능
- 여백이 줄어도 터치 영역과 가독성이 유지됨

### 3.5 산출물

- 실패 시: 해당 조합 이미지 + 재현 튜플 + 예상 원인(고정 height / inset / non-scroll Column 등)
- 통과 시: 대상 화면 목록과 “전 조합 확인” 또는 “샘플링 + 생략 사유”

### 3.6 현재 Screenshot Test 커버리지

작성 위치: 각 feature 모듈 `src/screenshotTest/.../*Screenshots.kt`  
공통 규칙: `@PreviewTest` + `@QaPhoneMatrix` + `RECAPTheme(dynamicColor = false)` (또는 onboarding PreviewContainer)

| 모듈 | 커버 화면 / 상태 |
|------|------------------|
| `feature/onboarding` | Landing, PermissionGuide, UploadMethodGuide, AddToFavorite, AddToFavoriteGuide(step 1–4), StartFirstAnalyze |
| `feature/settings` | Settings(Allowed/Denied), AccountManagement, DataManagement, Notification(On/DeviceOff), PrivacyGuide, UsageGuide |
| `feature/screenshot` | Detail(Content/Loading/Error), Edit, Fullscreen |
| `feature/organize` | Picker Empty, Confirmation, AnalysisStatus(Progress/Success/Failed/PartialFailed), UnsupportedShare |
| `feature/home` | Home(Empty/Error), Search(Idle/Empty), RecentOrganized(Empty/Error) |
| `feature/collection` | Overview(Empty/LoadError/Grid), Detail(Populated/Empty) |

기준 이미지 갱신·검증:

```powershell
.\gradlew.bat updateDebugScreenshotTest
.\gradlew.bat validateDebugScreenshotTest
```

상태 선택 원칙은 §3.3과 같다. 대표 + 밀집/레이아웃 차이 상태만 두고, 미커버 상태는 생략 사유를 남긴다.

---

## 4. QA with VMs

에뮬레이터 실기기 검증은 Screenshot Test가 잡지 못하는 **시스템 내비 inset·제스처 영역·실제 window insets**를 확인한다.

### 4.1 Emulator matrix

| ID | 크기 | System navigation | 목적 |
|----|------|-------------------|------|
| Emulator A | 320~360dp급 | Gesture Navigation | 제스처 바 inset, 하단 CTA 여유 |
| Emulator B | 320~360dp급 | 3-button Navigation | 3버튼 바 높이로 인한 하단 잘림·중복 padding |

두 에뮬레이터 모두 **소형 폭(320~360dp)** 을 유지한다. 큰 기기만으로 통과한 결과는 인정하지 않는다.

### 4.2 권장 설정

- Orientation: Portrait only
- Display size / Font size: 기본 + 최대(또는 fontScale 1.5 / 2.0에 해당하는 설정)를 각각 확인
- 가능하면 API 30+ (프로젝트 minSdk 30)

### 4.3 VM에서 반드시 볼 플로우

우선순위 (issue #43 기준):

1. 온보딩 풀뷰포트 화면들 (Landing, Permission/Upload 포함 단계)
2. pinned bottom CTA가 있는 화면
3. 고정 높이 일러스트가 큰 화면

각 플로우를 Emulator A / B 양쪽에서 확인한다.

### 4.4 VM 판정

**Fail (특히 Emulator B)**

- 3버튼 내비 바에 CTA·필수 텍스트가 가려짐
- Gesture ↔ 3-button 전환 시 하단 여백이 과다/과소로 레이아웃이 깨짐
- `safeDrawing` / `navigationBars` 혼용으로 화면마다 inset 체감이 다름 (계약 불일치)

**Pass**

- A/B 모두에서 하단 액션이 시스템 바와 겹치지 않음
- 고배율에서도 스크롤로 본문 접근 가능, CTA는 고정 노출 또는 동등한 도달성 유지

### 4.5 산출물

```text
VM QA:
- Emulator A (gesture, ~360dp): PASS|FAIL — notes
- Emulator B (3-button, ~360dp): PASS|FAIL — notes
- Font/Display max checked: yes|no
- Failed tuples: (screen, navMode, font/display)
```

---

## 5. 작업 게이트 (오케스트라 체크리스트)

레이아웃/간격/`core/design` 템플릿 변경 PR 또는 handoff 완료 전:

- [ ] Screenshot Spec 최소 매트릭스 확인 (또는 생략 사유 기록)
- [ ] Emulator A (Gesture) 확인
- [ ] Emulator B (3-button) 확인
- [ ] 실패 튜플이 있으면 구현 이슈로 되돌리거나 BACKLOG에 범위 밖 항목으로 분리
- [ ] 고정 padding 일괄 축소만으로 “완료” 처리하지 않음

문서만 변경한 경우 Design QA 게이트는 생략한다.

---

## 6. 보고 형식 (에이전트용)

```markdown
## Design QA Result
- Scope: <screens / components>
- Screenshot matrix: 320×640 / 360×800 / 412×915 × fontScale 1.0·1.3·1.5·2.0
  - Result: PASS | FAIL
  - Failures: (screen, size, fontScale) — note
- VM:
  - Emulator A (gesture, 320~360dp): PASS | FAIL — note
  - Emulator B (3-button, 320~360dp): PASS | FAIL — note
- Open questions: ...
```

---

## 7. Out of scope (현재 초안)

- 다크모드
- 태블릿 / landscape 전용 레이아웃
- 기능·네트워크·인증 QA
- Screenshot Test에 아직 없는 세부 상태(예: Home populated content, Collection list view, dialog/sheet 단독)의 즉시 전수 커버 — 필요 시 §3.6에 추가

---

## 8. 미결 (초안 TODO)

- Compose Screenshot 도구·`@QaPhoneMatrix`·주요 feature screenshotTest는 도입됨 (§3.6). 남은 작업은 golden 갱신/검증과 미커버 상태 확장
- golden image 저장 경로와 diff threshold
- Emulator AVD 이름·API level·정확한 dpi 프로파일 고정
- CI에서 Screenshot Spec 자동 실행 여부
- Design QA 결과를 `TESTING.md` / handoff Result에 의무 링크로 넣을지 여부

이 섹션은 도구/커버리지가 바뀌면 갱신한다.
