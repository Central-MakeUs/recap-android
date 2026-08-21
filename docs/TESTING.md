# TESTING.md - RECAP 테스트 컨벤션

이 문서는 RECAP 프로젝트의 테스트 작성/검증 기준을 정의한다.

## 현재 상태

- 현재 프로젝트는 `:app`, `:core:*`, `:feature:*`의 대상 모듈에 로컬 단위 테스트를 두는 멀티모듈 테스트 체계를 운영한다.
- 로컬 단위 테스트는 JUnit5 중심으로 작성한다.
- 기존 JUnit4 테스트는 JUnit Vintage engine으로 당분간 함께 실행한다.
- Compose UI test, AndroidX test 의존성은 설정되어 있다.
- MockK, Turbine, coroutines-test, Room testing 의존성이 설정되어 있다.
- Compose Preview Screenshot Testing은 `:app`, `:core:design` 및 UI가 있는 `:feature:*` 모듈에 적용되어 있다.

현재 설정된 주요 테스트 의존성:

- JUnit5: `org.junit.jupiter:junit-jupiter`
- JUnit Platform Launcher: `org.junit.platform:junit-platform-launcher`
- JUnit Vintage: `org.junit.vintage:junit-vintage-engine`
- MockK: `io.mockk:mockk`
- Coroutines test: `org.jetbrains.kotlinx:kotlinx-coroutines-test`
- Turbine: `app.cash.turbine:turbine`
- Room testing: `androidx.room:room-testing`

Instrumentation/Compose UI test는 AndroidX Compose test가 JUnit4 rule 기반이므로 별도 정책을 유지한다.

## Compose Preview Screenshot Testing

- Screenshot Test의 추가·기준 이미지 갱신·실행은 사용자가 명시적으로 요청한 경우에만 수행한다. 레이아웃 변경이나 기존 screenshot test 존재만으로 자동
  실행하지 않는다.
- 플러그인: `com.android.compose.screenshot` (`libs.plugins.compose.screenshot`, `0.0.1-alpha16`)
- `gradle.properties`와 모듈 `android.experimental.enableScreenshotTest`로 `screenshotTest` source set을 켠다
- Preview는 `src/screenshotTest/...`에 두고 `@Preview` + `@PreviewTest`로 지정한다
- 화면 크기·fontScale 매트릭스는 `@QaPhoneMatrix` (`com.chalkak.recap.core.design.qa`)로 적용한다 — `docs/qa/GUIDE.md` §3
- 의존성: `screenshotTestImplementation(libs.screenshot.validation.api)`, `screenshotTestImplementation(libs.androidx.compose.ui.tooling)`
- 기준 이미지 갱신: `.\gradlew.bat updateDebugScreenshotTest`
- 검증: `.\gradlew.bat validateDebugScreenshotTest`

## 기본 검증 명령

PowerShell debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

qa 빌드(Performance 플러그인 instrumentation 포함):

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleQa
```

Firebase Observability 수동 확인(qa/release 기기):

- DEBUG: Crashlytics/Performance collection OFF
- qa/release: Crashlytics·Performance ON
- 강제 non-fatal / organize custom trace가 Firebase 콘솔에 보이는지 확인

전체 debug 로컬 단위 테스트:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

## 파일 위치

- production 코드는 대상 모듈의 `src/main/java/...`에 둔다.
- 로컬 단위 테스트는 대상 모듈의 `src/test/java/...` 또는 `src/test/kotlin/...`에 둔다.
- Android instrumentation / Compose UI 테스트는 대상 모듈의 `src/androidTest/java/...` 또는
  `src/androidTest/kotlin/...`에 둔다.
- Preview Screenshot 테스트는 screenshot plugin을 적용한 대상 모듈의 `src/screenshotTest/java/...` 또는
  `src/screenshotTest/kotlin/...`에 둔다.
- 패키지 경로는 대상 클래스와 동일하게 미러링한다.

예:

```text
feature/home/src/main/java/com/chalkak/recap/feature/home/HomeViewModel.kt
feature/home/src/test/java/com/chalkak/recap/feature/home/HomeViewModelTest.kt
```

## 변경 유형별 필수 검증

- production 코드 변경: 영향받는 모듈의 `testDebugUnitTest`와 루트 `assembleDebug`를 실행한다. 영향 범위가 여러 모듈이거나 불명확하면 루트
  `testDebugUnitTest`를 실행한다.
- 테스트 코드만 변경: 해당 모듈의 관련 테스트를 실행한다. production compile 영향이 있으면 `assembleDebug`도 실행한다.
- Gradle plugin/dependency/build type 변경: 관련 테스트와 영향받는 build를 실행하며, qa/release 전용 설정을 바꿨다면 해당
  variant도 검증한다.
- 문서만 변경: Gradle 빌드·테스트는 생략할 수 있으며 문서 링크, 명령, 현재 코드 사실을 정적으로 확인한다.
- 실행할 수 없는 필수 검증이 있으면 생략하지 않은 것처럼 보고하지 말고 원인과 영향 범위를 완료 응답에 남긴다.

### 레이아웃 QA

- 레이아웃, 간격, typography, `core/design` 템플릿 또는 화면 구조 변경은 관련 Compose Preview를 확인한다.
- `validateDebugScreenshotTest`, 기준 이미지 갱신, screenshot test 추가·수정은 사용자가 명시적으로 요청한 경우에만 수행한다. 요청되지
  않았다면 미실행 사유를 따로 보고할 필요가 없다.
- 작은 화면·고배율·시스템 navigation inset에 영향을 줄 수 있는 변경(풀뷰포트, pinned bottom CTA, 고정 높이/패딩, `WindowInsets`
  /system bar 처리)은 `docs/qa/GUIDE.md`의 Emulator A(gesture)와 B(3-button)를 모두 확인한다.
- 위 위험과 무관한 시각 변경은 VM A/B 검증을 생략할 수 있지만, 완료 응답 또는 3단 handoff Result에 생략 사유를 기록한다.
- 문서만 변경한 경우 레이아웃 QA는 생략한다.

## 네이밍

- 파일명은 `<대상클래스>Test.kt`로 쓴다.
- 테스트 함수명은 백틱으로 감싼 영어 서술형을 쓴다.
- Given/When/Then 주석은 쓰지 않고 빈 줄로 구획한다.
- 테스트 하나는 하나의 동작만 검증한다.

```kotlin
@Test
fun `onAction updates selected tab`() {
    val viewModel = HomeViewModel()

    viewModel.onAction(HomeAction.SelectTab(HomeTab.Recent))

    assertEquals(HomeTab.Recent, viewModel.uiState.value.selectedTab)
}
```

## JUnit5

- `@Test`는 `org.junit.jupiter.api.Test`를 사용한다.
- `@BeforeEach`, `@AfterEach`도 JUnit5 패키지를 사용한다.
- 예외 검증은 `org.junit.jupiter.api.assertThrows`를 사용한다.
- Android instrumentation / Compose UI test는 JUnit4 기반 rule을 사용할 수 있다.

```kotlin
@Test
fun `throws when capture id is invalid`() {
    assertThrows<IllegalArgumentException> {
        require(captureId > 0)
    }
}
```

## MockK

- 기본은 엄격 모크인 `mockk<T>()`를 사용한다.
- 동기 함수는 `every { } returns`와 `verify { }`를 사용한다.
- suspend 함수는 `coEvery { } returns`와 `coVerify { }`를 사용한다.
- 반환값이 테스트와 무관한 부수 의존성에만 `relaxed = true`를 예외적으로 쓴다.

```kotlin
val repository = mockk<ScreenshotCardRepository>()
coEvery { repository.deleteAllCards() } returns Unit

viewModel.resetScreenshotData()

coVerify(exactly = 1) { repository.deleteAllCards() }
```

## 코루틴 테스트

- suspend 코드는 `kotlinx.coroutines.test.runTest` 안에서 실행한다.
- `viewModelScope`처럼 `Dispatchers.Main`을 참조하는 대상은 테스트마다 Main dispatcher를 지정하고 해제한다.
- 비동기 작업 완료가 필요하면 `advanceUntilIdle()`을 사용한다.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class SomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads initial state`() = runTest(testDispatcher) {
        viewModel.load()
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.value)
    }
}
```

## Flow 검증

- `StateFlow`와 `Flow`의 방출 순서는 Turbine으로 확인한다.
- Compose `mutableStateOf` 프로퍼티는 Flow가 아니므로 호출 후 값을 직접 읽어 검증한다.
- 무한 Flow는 테스트 마지막에 `cancelAndIgnoreRemainingEvents()`를 호출한다.

```kotlin
viewModel.uiState.test {
    assertEquals(initial, awaitItem())

    viewModel.onAction(action)

    assertEquals(expected, awaitItem())
    cancelAndIgnoreRemainingEvents()
}
```

## TDD 우선순위

1. ViewModel 테스트
   - UiState 초기값
   - Action 처리 결과
   - loading/error/empty state 전이
2. 순수 domain/model 테스트
   - validation
   - mapper
   - business rule
3. Repository 테스트
   - fake data source 기반
   - 외부 API 실패와 retry 가능 상태
4. Room DAO 테스트
   - insert/query/update/delete
   - migration 추가 시 migration test
5. Compose UI 테스트
   - 핵심 화면의 state별 렌더링
   - 주요 버튼 action

## Result 작성 규칙

`codex-plan` → `cursor-implement` → `codex-review` 3단 워크플로우를 사용한 경우에만 `docs/handoff/HANDOFF.md`의
Result 섹션에 결과를 남긴다. 그 밖의 작업은 `HANDOFF.md`를 읽거나 수정하지 않고 작업 완료 응답에 같은 수준의 변경·검증·미검증 사항을 보고한다.

```markdown
## Cursor Result
- Changed files: ...
- Build/test: .\gradlew.bat testDebugUnitTest GREEN, .\gradlew.bat assembleDebug GREEN
- Design QA: not applicable - no layout change
- Open questions: none
```

문서만 수정한 경우:

```markdown
## Cursor Result
- Changed files: docs/TESTING.md
- Build/test: not run - docs only; links/commands/current facts checked
- Design QA: not applicable - docs only
- Open questions: none
```
