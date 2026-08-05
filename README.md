<p align="center">
  <img width="120" height="120" alt="Recap 앱 아이콘" src="https://github.com/user-attachments/assets/2e695a43-7bb6-4457-bfdd-c794ea3d484a" />
</p>

<h1 align="center">Recap</h1>

<p align="center">
  <strong>앨범에 쌓인 스크린샷, 필요할 때 바로 꺼내 쓰세요.</strong>
</p>

<p align="center">
  Recap은 스크린샷을 AI로 요약하고 분류해<br />
  제목·요약·이미지 속 내용으로 다시 찾을 수 있게 만드는 Android 앱입니다.
</p>

<p align="center">
  <img width="1024" height="500" alt="Recap 피처 그래픽" src="https://github.com/user-attachments/assets/bd50b6a6-16f8-4db5-a147-24e609b89016" />
</p>

## 주요 기능

| 기능 | 설명 |
|:--|:--|
| 스크린샷 정리 | 앨범에서 여러 장을 선택해 한 번에 정리할 수 있어요. |
| AI 요약 | 이미지 속 내용을 분석해 제목과 핵심 요약을 만들어요. |
| 자동 분류 | 장소·맛집, 일정·예약, 쇼핑·상품 등 알맞은 유형으로 나눠요. |
| 내용 검색 | 제목과 요약은 물론, 이미지 속 텍스트까지 검색할 수 있어요. |
| 빠른 공유 | 방금 캡처한 화면을 Android 공유 시트에서 Recap으로 바로 보낼 수 있어요. |

## 미리 보기

<table>
  <tr>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/a6387e6a-eb23-4aae-91b3-fcf22e43f224" alt="Recap 앱 소개 화면" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/f3836e9e-a3ec-44f6-af7b-ece2b7b73d53" alt="Recap 홈 화면" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/06fe02a3-2849-45aa-afc1-83184fea557e" alt="Recap 스크린샷 정리 화면" width="100%" /></td>
  </tr>
  <tr>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/c71ec50b-3462-460c-a98f-4f6bde02c67e" alt="Recap AI 분석 화면" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/80149b21-10fb-4f60-9132-30b79aaf92f7" alt="Recap 컬렉션 화면" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/0ca4cfd3-1515-470c-8baa-c509df4e0ee4" alt="Recap 스크린샷 상세 화면" width="100%" /></td>
  </tr>
</table>

## 기술 스택

| 영역 | 기술 |
|:--|:--|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Multi-module MVVM, immutable `UiState`, sealed `Action` |
| Navigation | AndroidX Navigation3 |
| Dependency Injection | Hilt, KSP |
| Async | Kotlin Coroutines, Flow |
| Persistence | Room, DataStore |
| Image | Coil |
| Observability | Timber, Firebase Crashlytics, Firebase Performance |
| Analysis | 서버 기반 OCR·AI 분석 |

## 프로젝트 구조

Recap은 `:app`을 중심으로 재사용 계층인 `:core:*`와 화면 단위 기능인 `:feature:*`를 분리한 멀티모듈 프로젝트입니다.

```text
:app
:core:model
:core:design
:core:data
:feature:home
:feature:collection
:feature:organize
:feature:screenshot
:feature:onboarding
:feature:settings
:feature:developer
```

- UI 상태는 immutable `UiState`로 표현하고 사용자 입력은 sealed `Action`으로 명시합니다.
- ViewModel은 상태 생성과 action 처리에 집중합니다.
- Repository가 로컬 저장소와 외부 연동의 세부 구현을 감춥니다.

## 앱 정보

| 항목 | 값 |
|:--|:--|
| Package | `com.chalkak.recap` |
| minSdk | 30 |
| targetSdk / compileSdk | 37 |
| Version | `1.0.0` (`versionCode` 1) |

## 문서

| 문서 | 내용 |
|:--|:--|
| [`docs/PROJECT.md`](docs/PROJECT.md) | 프로젝트 구조와 개발 컨벤션 |
| [`docs/TESTING.md`](docs/TESTING.md) | 테스트 및 검증 정책 |
| [`docs/LOCAL_DATA.md`](docs/LOCAL_DATA.md) | 로컬 데이터 구조와 저장 정책 |
| [`docs/ANALYSIS_DATA_SOURCE.md`](docs/ANALYSIS_DATA_SOURCE.md) | Mock/Remote build-time (`USE_MOCK_BACKEND`) 선택 |
| [`docs/MOCK_REMOTE_CHANGE.md`](docs/MOCK_REMOTE_CHANGE.md) | 제거된 런타임 Mock/Remote 전환 계층 역사 스냅샷 |
| [`docs/SCREENSHOT_MOCK_DATA.md`](docs/SCREENSHOT_MOCK_DATA.md) | Mock 스크린샷 분석 결과 계약 |
| [`docs/ORGANIZE_OVERLAY_NAVIGATION.md`](docs/ORGANIZE_OVERLAY_NAVIGATION.md) | 스크린샷 정리 플로우와 내비게이션 구조 |
| [`docs/BACKLOG.md`](docs/BACKLOG.md) | 후속 작업 백로그 |
| [`docs/handoff/HANDOFF.md`](docs/handoff/HANDOFF.md) | Codex ↔ Cursor 활성 handoff 채널 |
