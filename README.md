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
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/0bbfcc0d-6842-4b7d-8f5f-462b366c7926" alt="Recap 스크린샷 업로드" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/68cb34dc-b46c-491a-8f87-a51af9ae62a9" alt="Recap AI 자동 요약" width="100%" /></td>
  </tr>
  <tr>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/5c829d1c-03f9-42de-a394-dea7b198286f" alt="Recap 유형별 자동 분류" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/fdc491ef-93f0-4bdb-8bbf-cec49ba04de5" alt="Recap 스크린샷 검색" width="100%" /></td>
    <td width="33.33%"><img src="https://github.com/user-attachments/assets/51d727f1-9aa4-40af-9e6d-4937e7355f41" alt="Recap 캡처하고, 전송하기" width="100%" /></td>
  </tr>
</table>

## 기술 스택

| 영역                   | 기술                                                      |
|:---------------------|:--------------------------------------------------------|
| Language             | Kotlin 2.4                                              |
| UI                   | Jetpack Compose, Material 3, Lottie                     |
| Architecture         | Multi-module MVVM, immutable `UiState`, sealed `Action` |
| Navigation           | AndroidX Navigation3                                    |
| Dependency Injection | Hilt, KSP                                               |
| Async                | Kotlin Coroutines, Flow                                 |
| Network              | Retrofit, OkHttp, Kotlinx Serialization                 |
| Auth                 | Kakao Login                                             |
| Persistence          | Room, DataStore                                         |
| Image                | Coil                                                    |
| Observability        | Timber, Firebase Crashlytics, Firebase Performance      |
| Analysis             | 서버 기반 OCR·AI 분석                                         |
| Test                 | JUnit 5, Compose Preview Screenshot Testing             |

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
- 스크린샷 도메인 backend는 빌드 시점에 Mock 또는 Remote로 고정됩니다. debug 기본값은 Mock이며, qa/release는 Remote입니다.

## 앱 정보

| 항목                     | 값                          |
|:-----------------------|:---------------------------|
| Package                | `com.chalkak.recap`        |
| minSdk                 | 30                         |
| targetSdk / compileSdk | 37                         |
| Kotlin / AGP           | 2.4.10 / 9.3.1             |
| JDK                    | 17                         |
| Version                | `1.1.3` (`versionCode` 11) |
| Build types            | debug, qa, release         |

## 문서

| 문서                                                                           | 내용                                             |
|:-----------------------------------------------------------------------------|:-----------------------------------------------|
| [`docs/PROJECT.md`](docs/PROJECT.md)                                         | 프로젝트 구조와 개발 컨벤션                                |
| [`docs/TESTING.md`](docs/TESTING.md)                                         | 테스트 및 검증 정책                                    |
| [`docs/qa/GUIDE.md`](docs/qa/GUIDE.md)                                       | 디자인 QA(스크린샷·VM) 오케스트라 지침                       |
| [`docs/LOCAL_DATA.md`](docs/LOCAL_DATA.md)                                   | 로컬 데이터 구조와 저장 정책                               |
| [`docs/ANALYSIS_DATA_SOURCE.md`](docs/ANALYSIS_DATA_SOURCE.md)               | Mock/Remote build-time (`USE_MOCK_BACKEND`) 선택 |
| [`docs/MOCK_REMOTE_CHANGE.md`](docs/MOCK_REMOTE_CHANGE.md)                   | 제거된 런타임 Mock/Remote 전환 계층 역사 스냅샷               |
| [`docs/SCREENSHOT_MOCK_DATA.md`](docs/SCREENSHOT_MOCK_DATA.md)               | Mock 스크린샷 분석 결과 계약                             |
| [`docs/ORGANIZE_OVERLAY_NAVIGATION.md`](docs/ORGANIZE_OVERLAY_NAVIGATION.md) | 스크린샷 정리 플로우와 내비게이션 구조                          |
| [`docs/BACKLOG.md`](docs/BACKLOG.md)                                         | 후속 작업 백로그                                      |
| [`docs/handoff/HANDOFF.md`](docs/handoff/HANDOFF.md)                         | 3단 워크플로우 전용 Codex ↔ Cursor handoff 채널          |
