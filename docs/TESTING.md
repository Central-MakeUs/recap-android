# TESTING.md - RECAP 테스트 컨벤션

이 문서는 RECAP 프로젝트의 테스트 작성/검증 기준을 정의한다.

## 현재 상태

- 현재 프로젝트에는 특별한 테스트 코드 체계가 없다.
- 로컬 단위 테스트는 JUnit5 중심으로 작성한다.
- 기존 JUnit4 테스트는 JUnit Vintage engine으로 당분간 함께 실행한다.
- Compose UI test, AndroidX test 의존성은 설정되어 있다.
- MockK, Turbine, coroutines-test, Room testing 의존성이 설정되어 있다.

현재 설정된 주요 테스트 의존성:

- JUnit5: `org.junit.jupiter:junit-jupiter`
- JUnit Platform Launcher: `org.junit.platform:junit-platform-launcher`
- JUnit Vintage: `org.junit.vintage:junit-vintage-engine`
- MockK: `io.mockk:mockk`
- Coroutines test: `org.jetbrains.kotlinx:kotlinx-coroutines-test`
- Turbine: `app.cash.turbine:turbine`
- Room testing: `androidx.room:room-testing`

Instrumentation/Compose UI test는 AndroidX Compose test가 JUnit4 rule 기반이므로 별도 정책을 유지한다.

## 기본 검증 명령

PowerShell debug build:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat assembleDebug
```

로컬 단위 테스트가 추가된 뒤 기본 test 명령:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

## 파일 위치

- 현재 `:app` 단일 모듈의 main source는 `app/src/main/java/...`에 있다.
- 로컬 단위 테스트는 `app/src/test/java/...`에 둔다.
- Android instrumentation / Compose UI 테스트는 `app/src/androidTest/java/...`에 둔다.
- 패키지 경로는 대상 클래스와 동일하게 미러링한다.

예:

```text
app/src/main/java/com/chalkak/recap/feature/home/HomeViewModel.kt
app/src/test/java/com/chalkak/recap/feature/home/HomeViewModelTest.kt
```

추후 멀티모듈 전환 후에는 각 모듈의 `src/test/java` 또는 `src/test/kotlin` 아래에 대상 클래스 경로를 미러링한다.

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

`docs/handoff/HANDOFF.md`의 Result 섹션에 다음을 남긴다.

```markdown
## Cursor Result
- Changed files: ...
- Build/test: .\gradlew.bat assembleDebug GREEN, .\gradlew.bat testDebugUnitTest GREEN
- Open questions: none
```

문서만 수정한 경우:

```markdown
## Cursor Result
- Changed files: docs/TESTING.md
- Build/test: not run - docs only
- Open questions: none
```
