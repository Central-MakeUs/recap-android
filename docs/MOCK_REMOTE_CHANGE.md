# MOCK_REMOTE_CHANGE.md - 런타임 Mock/Remote backend 전환 (historical)

> **문서 목적:** 제거된 **런타임 중 Mock/Remote 전환 계층**을, 제거 직전 코드 기준으로 기록한 역사 스냅샷이다. 현재 동작 설명이 아니다.
>
> **상태:** 역사 기록 (제거 완료). 현재 사실 문서는 build-time `BuildConfig.USE_MOCK_BACKEND` 기준의 `docs/ANALYSIS_DATA_SOURCE.md` / `docs/LOCAL_DATA.md` / `docs/PROJECT.md`를 본다.
>
> **관련 문서:**
> - 요약본(현행): `docs/ANALYSIS_DATA_SOURCE.md`
> - Mock/Remote 저장 SoT: `docs/LOCAL_DATA.md`
> - Mock 분석 결과 계약: `docs/SCREENSHOT_MOCK_DATA.md`
> - 제거 handoff: `docs/handoff/HANDOFF.md`

---

## 1. 한 줄 요약

Debug 빌드에서 개발자 옵션이 DataStore에 `MOCK`/`REMOTE`를 저장하고, 8개의 `Switching*Repository`가 **매 요청 또는 Flow마다** 그 값을 읽어 Mock/Remote concrete 구현으로 위임한다. Release(non-debug)는 저장값과 무관하게 항상 effective `REMOTE`다.

---

## 2. 전체 구조

```text
[Developer Options UI]  (feature/developer)
        │ Request / Confirm actions
        ▼
[DeveloperViewModel]
        │ ScreenshotBackendSwitcher.switchTo(target)
        ▼
[ScreenshotBackendSwitcher]  (@Singleton, core/data/.../screenshot/backend)
        │ 1) busy gate (Mutex + ScreenshotAnalysisRunState.isRunning)
        │ 2) MockScreenshotDataResetter.reset()
        │ 3) ScreenshotBackendModeStore.setMode(target)
        ▼
[ScreenshotBackendModeStore]
  └─ DataStoreScreenshotBackendModeStore
       └─ user_preferences DataStore
            keys: screenshot_backend_mode (우선)
                  analysis_data_source_mode (legacy fallback)
        │
        │  mode: Flow<ScreenshotBackendMode>
        │  currentMode(): suspend → mode.first()
        ▼
[Switching*Repository]  ← Hilt가 domain interface에 @Binds
        ├── MOCK   → Mock*Repository
        └── REMOTE → Remote*Repository

[ObservabilityBootstrap]
        └── screenshotBackendModeStore.mode 관찰 → Crashlytics key backend_mode
```

핵심 원칙(현재 코드):

- Feature/ViewModel은 domain repository **interface**만 본다. Mock/Remote concrete type을 직접 주입하지 않는다(개발자 옵션·observability 제외).
- Switching 계층은 **항상** Mock·Remote 구현체를 생성자에서 둘 다 주입받는다(둘 다 graph에 singleton으로 존재).
- Auth(`getAccountInfo` / `withdraw`)는 mode와 무관하게 **항상 Remote**.
- Splash는 mode hydrate를 기다리지 않는다. suspend API는 호출 시점 `currentMode()`, Flow API는 `mode.flatMapLatest`로 전환에 반응한다.

---

## 3. 모드 모델

### 3.1 `ScreenshotBackendMode`

경로: `core/data/.../screenshot/backend/ScreenshotBackendMode.kt`

| 값 | 의미 |
|----|------|
| `MOCK` | 기기 Room + private 원본/썸네일을 SoT로 쓰는 Mock backend |
| `REMOTE` | 서버를 SoT로 쓰고, 기기는 capture ID 기반 썸네일 캐시만 유지 |

`fromStoredValue(value: String?)`:

- `value`가 enum `name`과 일치하면 해당 값
- `null` / 알 수 없는 문자열 → **`MOCK`**

### 3.2 `ScreenshotBackendModeStore` 계약

경로: `core/data/.../screenshot/backend/ScreenshotBackendModeStore.kt`

```text
val mode: Flow<ScreenshotBackendMode>
suspend fun currentMode(): ScreenshotBackendMode
suspend fun setMode(mode: ScreenshotBackendMode)
```

DI: `ScreenshotBackendModule`이 `DataStoreScreenshotBackendModeStore`를 `@Binds` → `ScreenshotBackendModeStore` (`@Singleton`).

### 3.3 `DataStoreScreenshotBackendModeStore` 구현 세부

경로: `core/data/.../screenshot/backend/DataStoreScreenshotBackendModeStore.kt`

| 항목 | 동작 |
|------|------|
| DataStore | `@UserPreferencesDataStore` — 파일 `user_preferences` (별도 DataStore 아님) |
| 읽기 | `dataStore.safeData()` → IOException 시 `emptyPreferences()` emit (재시도 없음) |
| 신규 key | `stringPreferencesKey("screenshot_backend_mode")` |
| Legacy key | `stringPreferencesKey("analysis_data_source_mode")` |
| 읽기 우선순위 | 신규 key → 없으면 legacy key |
| `mode` Flow | `readStoredValue` → `resolveEffectiveMode` |
| `currentMode()` | `mode.first()` (Flow의 첫 emit) |
| Debug 판정 | `isDebugBuild` 필드, 기본값 `BuildConfig.DEBUG` (`@VisibleForTesting`으로 테스트에서 덮어씀) |

#### Effective mode 정책

| 빌드 | 읽기 | 쓰기(`setMode`) |
|------|------|-----------------|
| **Debug** (`isDebugBuild == true`) | 저장값을 `fromStoredValue`로 해석. 없음/unknown → `MOCK` | DataStore에 신규 key 기록 + **legacy key 제거** |
| **non-debug** | 저장값이 `MOCK`이어도 **항상 `REMOTE`** | **no-op** (DataStore 미변경) |

주의:

- Release에서 DataStore에 `MOCK`이 남아 있어도 UI/Switching은 `REMOTE`만 본다.
- Legacy key는 `setMode`가 **성공한 Debug 쓰기**에서만 제거된다. non-debug에서는 legacy가 남아 있어도 읽기 effective는 `REMOTE`.

---

## 4. 전환 policy — `ScreenshotBackendSwitcher`

경로: `core/data/.../screenshot/backend/ScreenshotBackendSwitcher.kt`

`@Singleton`. 주입:

- `ScreenshotBackendModeStore`
- `MockScreenshotDataResetter`
- `ScreenshotAnalysisRunState`

### 4.1 결과 타입 `ScreenshotBackendSwitchResult`

| 값 | 의미 |
|----|------|
| `Success` | 동일 모드 no-op 포함, 또는 reset+setMode 성공 |
| `RejectedBusy` | 다른 전환이 Mutex를 잡고 있거나, 분석 실행 중 |
| `Failure` | reset 또는 setMode 중 일반 예외 |

`CancellationException`은 catch하지 않고 재throw한다.

### 4.2 `switchTo(targetMode)` 순서 (코드 그대로)

1. `switchMutex.tryLock()` 실패 → `RejectedBusy` (이미 전환 중)
2. `currentMode() == targetMode` → `Success` (reset/`setMode` 호출 없음)
3. `screenshotAnalysisRunState.isRunning.value == true` → `RejectedBusy`
4. `_isSwitching = true`
5. `mockScreenshotDataResetter.reset()`
6. `modeStore.setMode(targetMode)`
7. 성공 → `Success` / 예외 → `Failure` (mode는 setMode 전에 실패하면 미변경; setMode 중 실패하면 DataStore edit 트랜잭션에 따름)
8. `finally`: `_isSwitching = false`, `switchMutex.unlock()`

노출 상태:

- `isSwitching: StateFlow<Boolean>` — UI 버튼 disable / busy feedback용

중요 정책:

- **방향과 무관하게** 전환 시 항상 Mock 로컬 스크린샷 데이터를 먼저 지운다 (`MOCK→REMOTE`, `REMOTE→MOCK` 모두).
- 삭제는 비가역. session token / onboarding / 일반 사용자 설정은 건드리지 않는다.
- non-debug에서 `setMode`가 no-op이어도 switcher는 먼저 reset을 수행한다(개발자 옵션 UI는 Debug 전제).

---

## 5. Mock 데이터 초기화 — `MockScreenshotDataResetter`

경로: `core/data/.../screenshot/backend/MockScreenshotDataResetter.kt`

```text
suspend fun reset() {
    screenshotCardRepository.deleteAllCards()
    screenshotImageStorage.clearStoredImages()
}
```

사용처:

| 호출자 | mode 변경? |
|--------|------------|
| `ScreenshotBackendSwitcher.switchTo` | 예 (`setMode` 직전) |
| `DeveloperViewModel.resetScreenshotData` | 아니오 (별도 “스크린샷 데이터 초기화” 액션) |
| `MockUserRepository.deleteAccountData` | 아니오 (Mock 계정 데이터 삭제 경로) |

건드리지 않는 것: session, onboarding, AI consent DataStore keys, Remote 서버 데이터, `RemoteCaptureThumbnailCache`(resetter 자체에는 없음).

---

## 6. 분석 중 게이트 — `ScreenshotAnalysisRunState`

경로: `core/data/.../screenshot/analysis/ScreenshotAnalysisRunState.kt`

- `@Singleton`
- `activeRunCount: AtomicInteger` + `isRunning: StateFlow<Boolean>`
- `beginRun()` / `endRun()` — 중첩 run 지원 (`endRun`은 count가 0이 될 때만 `isRunning=false`)

소비처(전환 관련):

- `ScreenshotBackendSwitcher` — `isRunning.value`면 전환 거부
- `DeveloperViewModel` — UI `canSwitchScreenshotBackend`, confirm 직전 재검사

분석 진행 UI(`ScreenshotAnalysisProgressViewModel`)도 동일 state를 쓰지만, backend 전환 로직의 일부가 아니라 **게이트 입력**이다. 제거 handoff는 이 클래스 자체를 삭제 대상으로 두지 않는다.

---

## 7. Switching repository (8개)

공통 패턴:

1. Hilt: domain interface → `Switching*` `@Binds`
2. Switching 생성자가 `ScreenshotBackendModeStore` + Mock concrete + Remote concrete를 **직접** 주입 (둘 다 eager singleton)
3. 위임 방식은 API 종류에 따라 둘로 나뉨:
   - **suspend/one-shot:** `currentMode()` 한 번 → delegate
   - **Flow 관찰:** `mode.flatMapLatest { ... }` → mode 변경 시 upstream 교체

### 7.1 위임 매트릭스

| Switching 클래스 | Interface | Mock | Remote | Flow 위임 | suspend 위임 | 특이사항 |
|------------------|-----------|------|--------|-----------|--------------|----------|
| `SwitchingScreenshotAnalysisRepository` | `ScreenshotAnalysisRepository` | `MockScreenshotAnalysisRepository` | `RemoteScreenshotAnalysisRepository` | — | `analyze` 단건/리스트, `organize` | 리스트/organize도 **요청당 mode 1회** resolve 후 단일 delegate |
| `SwitchingHomeRepository` | `HomeRepository` | `MockHomeRepository` | `RemoteHomeRepository` | `observeSummary` | `prefetchSummary` | `refreshSummary()`는 **양쪽 모두** 호출 |
| `SwitchingRecentCapturesRepository` | `RecentCapturesRepository` | `MockRecentCapturesRepository` | `RemoteRecentCapturesRepository` | `observeRecentCaptures` | `getRecentCaptures` | — |
| `SwitchingStorageRepository` | `StorageRepository` | `MockStorageRepository` | `RemoteStorageRepository` | `observeOverview`, `observeCapturesByType`, `observeFavoriteCaptures` | prefetch/get* | `refreshOverview()`는 **양쪽 모두** |
| `SwitchingCaptureMutationRepository` | `CaptureMutationRepository` | `MockCaptureMutationRepository` | `RemoteCaptureMutationRepository` | — | favorite/update/delete/report | — |
| `SwitchingSearchRepository` | `SearchRepository` | `MockSearchRepository` | `RemoteSearchRepository` | — | `search` | — |
| `SwitchingScreenshotDetailRepository` | `ScreenshotDetailRepository` | `MockScreenshotDetailRepository` | `RemoteScreenshotDetailRepository` | `observeCard` | — | — |
| `SwitchingUserRepository` | `UserRepository` | `MockUserRepository` | `RemoteUserRepository` | data-summary / consent observe | prefetch/get/give/withdrawConsent/deleteAccountData | Auth는 항상 Remote; refresh*는 **양쪽 모두** |

### 7.2 Hilt 모듈 바인딩

| 모듈 | 바인딩 |
|------|--------|
| `screenshot/analysis/ScreenshotAnalysisModule.kt` | `SwitchingScreenshotAnalysisRepository` → `ScreenshotAnalysisRepository` |
| `home/HomeModule.kt` | `SwitchingHomeRepository` → `HomeRepository`; `SwitchingRecentCapturesRepository` → `RecentCapturesRepository` |
| `storage/StorageModule.kt` | `SwitchingStorageRepository` → `StorageRepository` |
| `capture/CaptureMutationModule.kt` | `SwitchingCaptureMutationRepository` → `CaptureMutationRepository` |
| `search/SearchModule.kt` | `SwitchingSearchRepository` → `SearchRepository` |
| `screenshot/persistence/ScreenshotDetailModule.kt` | `SwitchingScreenshotDetailRepository` → `ScreenshotDetailRepository` |
| `user/UserModule.kt` | `SwitchingUserRepository` → `UserRepository` |
| `screenshot/backend/ScreenshotBackendModule.kt` | `DataStoreScreenshotBackendModeStore` → `ScreenshotBackendModeStore` |

Mock/Remote concrete 클래스는 `@Inject constructor` + `@Singleton`로 등록되며, Switching이 둘 다 생성자 파라미터로 받기 때문에 **선택되지 않은 backend도 graph 생성 시 인스턴스화**된다.

### 7.3 `SwitchingUserRepository` Auth 예외 (세부분기)

| Method | 위임 |
|--------|------|
| `getAccountInfo()` | **항상** `remoteUserRepository` |
| `withdraw()` | **항상** `remoteUserRepository` |
| `observeDataSummary` / `prefetch` / `getDataSummary` | mode 기준 Mock 또는 Remote |
| `observeConsentStatus` / `prefetch` / `get` / `giveConsent` / `withdrawConsent` | mode 기준 |
| `deleteAccountData` | mode 기준 |
| `refreshDataSummary()` / `refreshConsentStatus()` | **Mock + Remote 둘 다** 호출 |

`MockUserRepository` 자체:

- `getAccountInfo` / `withdraw` → `Result.failure(UnsupportedOperationException("Auth is remote-only"))`
- Switching이 Auth를 Remote로 우회하므로 Mock의 failure 구현은 **직접 호출될 때만** 드러남
- `deleteAccountData` → `MockScreenshotDataResetter.reset()` + `RemoteCaptureChangeNotifier.notifyCaptureChanged()`

### 7.4 dual-refresh quirk

다음 refresh API는 “현재 mode의 delegate만”이 아니라 **양쪽 concrete를 항상** 호출한다.

- `SwitchingHomeRepository.refreshSummary()`
- `SwitchingStorageRepository.refreshOverview()`
- `SwitchingUserRepository.refreshDataSummary()` / `refreshConsentStatus()`

제거 handoff는 이 동작을 없애고, 선택된 구현만 실행하도록 바꿀 예정이다.

### 7.5 Flow 전환 동작

`flatMapLatest`를 쓰는 관찰 API는 DataStore mode가 바뀌면:

1. 이전 backend Flow collection 취소
2. 새 backend의 observe*로 즉시 재구독

따라서 개발자 옵션에서 전환이 성공하면, Home/Storage/Detail/Recent/User summary·consent UI는 **앱 재시작 없이** 새 backend 스트림을 탄다.

---

## 8. 개발자 옵션 UI / ViewModel

### 8.1 진입

- Root route `Developer` (`docs/PROJECT.md` 앱 흐름)
- 화면: `feature/developer/.../DeveloperOptionsScreen.kt`
- VM: `feature/developer/.../DeveloperViewModel.kt`

### 8.2 `DeveloperOptionsUiState`

| 필드 | 소스 |
|------|------|
| `screenshotBackendMode` | `ScreenshotBackendModeStore.mode` |
| `isAnalysisRunning` | `ScreenshotAnalysisRunState.isRunning` |
| `isSwitching` | `ScreenshotBackendSwitcher.isSwitching` |
| `pendingSwitchTargetMode` | VM 로컬 `MutableStateFlow` (확인 popup) |
| `feedbackMessageResId` | VM 로컬 feedback |
| `canSwitchScreenshotBackend` | `!isAnalysisRunning && !isSwitching` |

`combine(...).stateIn(..., Eagerly, initialValue = DeveloperOptionsUiState())` — 초기값은 mode=`MOCK`이므로 DataStore 첫 emit 전까지 잠깐 MOCK으로 보일 수 있다.

### 8.3 Action 흐름

```text
[전환 버튼]
  → RequestScreenshotBackendSwitch(opposite mode)
      · target == current → no-op
      · !canSwitch → busy string feedback, dialog 없음
      · else → pendingSwitchTargetMode = target  (RecapPopup 표시)

[Popup 확인]
  → ConfirmScreenshotBackendSwitch
      · 재검사: analysis running 또는 switcher.isSwitching → busy feedback, pending clear
      · else launch: pending clear → switcher.switchTo(target) → Success/RejectedBusy/Failure string

[Popup 취소/dismiss]
  → DismissScreenshotBackendSwitchDialog → pending = null
```

전환과 무관한 유지 액션:

- Component Garden
- onboarding reset
- 스크린샷 데이터 초기화 (`MockScreenshotDataResetter`만, mode 유지)
- test crash

### 8.4 UI 표시

- 현재 mode 라벨: Mock / Remote string
- 버튼 라벨: 반대 mode로 전환 (“Mock로 전환” / “Remote로 전환”)
- 확인 popup: Mock 데이터 삭제 경고, confirm 버튼색 `RecapError`
- Preview 3종: Mock / Remote+running / confirm dialog — 모두 `RECAPTheme`

### 8.5 관련 string resources

`core/design/src/main/res/values/strings.xml`:

- `developer_options_screenshot_backend_current`
- `developer_options_screenshot_backend_mock` / `_remote`
- `developer_options_switch_to_mock_button` / `_remote_button`
- `developer_options_switch_screenshot_backend_confirm_*`
- `developer_options_switch_screenshot_backend_success` / `_failure` / `_rejected_busy`

---

## 9. Observability

경로: `app/.../observability/ObservabilityBootstrap.kt`

`start()`에서 다음을 `combine` 후 Crashlytics custom key로 설정:

| Key (`ObservabilityKeys`) | 값 |
|---------------------------|-----|
| `onboarding_completed` | UserPreferences |
| `logged_in` | refresh token non-blank |
| `backend_mode` | `ScreenshotBackendMode.name.lowercase()` → `"mock"` / `"remote"` |

mode Flow가 바뀌면 `distinctUntilChanged` 후 key를 갱신한다. Performance trace attribute와는 별개이며, key 상수 정의는 `core/model/.../PerformanceTracer.kt`의 `ObservabilityKeys.BACKEND_MODE`.

---

## 10. Mock vs Remote SoT (전환이 바꾸는 것)

상세 표는 `docs/LOCAL_DATA.md` “Mock backend vs Remote backend 저장 SoT”를 본다. 전환 계층 관점 요약:

| 영역 | MOCK | REMOTE |
|------|------|--------|
| 카드/목록/검색/상세 | Room + private images | 서버 API + `RemoteCaptureThumbnailCache` |
| 분석/organize | Mock 분석 구현 | Remote 업로드/분석 |
| 데이터 요약 / consent / 전체 삭제 | MockUserRepository | RemoteUserRepository |
| 계정 조회 / 탈퇴 | (Switching이 Remote로 고정) | Remote |
| onboarding / 일반 설정 | mode 무관 | mode 무관 |

---

## 11. 단위 테스트 목록 (전환 계층)

| 테스트 | 검증 초점 |
|--------|-----------|
| `ScreenshotBackendModeStoreTest` | default MOCK, set/get, unknown→MOCK, non-debug force REMOTE + setMode no-op, legacy fallback, 신규 key 우선, setMode 시 legacy 제거 |
| `ScreenshotBackendSwitcherTest` | same-mode Success, reset→setMode 순서, analysis busy, reset/setMode Failure, concurrent RejectedBusy |
| `MockScreenshotDataResetterTest` | deleteAllCards + clearStoredImages |
| `SwitchingScreenshotAnalysisRepositoryTest` | MOCK/REMOTE 단건 위임, list/organize mode 1회 |
| `SwitchingHomeRepositoryTest` | observe flatMap, prefetch, refresh 양쪽 |
| `SwitchingRecentCapturesRepositoryTest` | observe / get 위임 |
| `SwitchingCaptureMutationRepositoryTest` | mutation 위임 |
| `SwitchingScreenshotDetailRepositoryTest` | observeCard flatMap |
| `SwitchingUserRepositoryTest` | Auth always remote, data/consent mode 위임 |
| `DeveloperViewModelTest` | request/confirm/dismiss, success/failure/busy, reset data |

Search/Storage Switching 전용 테스트 파일은 현재 grep 기준 없거나 다른 이름일 수 있음 — Home/Analysis/User/Capture/Detail/ModeStore/Switcher/VM이 핵심 회귀 범위다.

---

## 12. 파일 인벤토리 (제거 handoff 대상과 대응)

### 12.1 backend 패키지 (`core/data/.../screenshot/backend/`)

| 파일 | 역할 | 제거 예정? |
|------|------|------------|
| `ScreenshotBackendMode.kt` | enum | 예 |
| `ScreenshotBackendModeStore.kt` | interface | 예 |
| `DataStoreScreenshotBackendModeStore.kt` | DataStore 구현 | 예 |
| `ScreenshotBackendSwitcher.kt` | 전환 policy | 예 |
| `ScreenshotBackendModule.kt` | Store @Binds | 예 |
| `MockScreenshotDataResetter.kt` | Mock 로컬 wipe | **아니오** (개발자 초기화·Mock deleteAccountData 유지) |

### 12.2 Switching 구현 (전부 제거 예정)

```text
screenshot/analysis/SwitchingScreenshotAnalysisRepository.kt
home/SwitchingHomeRepository.kt
home/SwitchingRecentCapturesRepository.kt
storage/SwitchingStorageRepository.kt
capture/SwitchingCaptureMutationRepository.kt
search/SwitchingSearchRepository.kt
screenshot/persistence/SwitchingScreenshotDetailRepository.kt
user/SwitchingUserRepository.kt
```

대응 test의 `Switching*RepositoryTest.kt`, `ScreenshotBackendModeStoreTest.kt`, `ScreenshotBackendSwitcherTest.kt`도 제거/대체 예정.

### 12.3 유지하되 수정되는 소비처

| 파일 | 현재 의존 | 제거 후 방향(handoff) |
|------|-----------|------------------------|
| `feature/developer/DeveloperViewModel.kt` | ModeStore, Switcher, RunState, Resetter | 전환 UI/의존 삭제, Resetter 유지 |
| `feature/developer/DeveloperOptionsScreen.kt` | mode UI/popup/actions | 전환 UI 삭제 |
| `app/.../ObservabilityBootstrap.kt` | ModeStore.mode Flow | `BuildConfig.USE_MOCK_BACKEND` 고정 문자열 |
| 각 `*Module.kt` | Switching @Binds | BuildConfig로 Mock/Remote Provider 선택 |
| `MockUserRepository.kt` | Auth failure stub | Remote auth delegate 주입 후 Auth만 위임 |

### 12.4 DataStore preference keys (제거 예정)

- `screenshot_backend_mode`
- `analysis_data_source_mode` (legacy)

같은 `user_preferences`의 onboarding/consent/notification keys는 유지.

---

## 13. 역사 (참고)

| 시기 | 내용 |
|------|------|
| ~2026-07-19 | `AnalysisDataSourceMode` + 분석 Switching만 런타임 전환 (`archive/...-analysis-data-source-runtime-switch.md`) |
| ~2026-07-22 | 전역 `ScreenshotBackendMode` / Store / Switcher / Resetter로 승격, Home·Storage·Capture 등 확장 (`archive/...-global-screenshot-backend-switch.md`) |
| 이후 | Search / ScreenshotDetail / User data-summary·consent도 Switching에 편입. non-debug effective는 **항상 REMOTE**(초기 handoff의 non-debug MOCK 강제와 다름 — **현재 코드 기준은 REMOTE**) |
| 현재 handoff | 런타임 계층 제거 → `BuildConfig.USE_MOCK_BACKEND` 프로세스 고정 |

---

## 14. 제거 시 놓치기 쉬운 세부 (as-is quirk 체크리스트)

1. **Eager dual inject:** Switching이 Mock+Remote를 둘 다 생성 → 제거 후 `Provider<>` lazy 선택이 handoff 요구사항.
2. **dual refresh:** Home/Storage/User refresh가 양쪽 호출 → 제거 후 선택 구현만.
3. **Auth split:** SwitchingUserRepository만 Auth를 Remote 고정. MockUserRepository Auth는 failure stub.
4. **전환 시 무조건 Mock wipe:** Remote→Mock도 wipe 후 mode 저장.
5. **Flow flatMapLatest:** 런타임 전환의 실시간 UI 반영 수단. build-time 고정 후에는 불필요.
6. **Release setMode no-op + read force REMOTE:** DataStore에 MOCK이 남아 있어도 effective는 REMOTE.
7. **Legacy key migration:** setMode 시에만 legacy 삭제.
8. **VM initial MOCK:** Eager stateIn 초기값과 실제 DataStore 불일치 가능(짧은 구간).
9. **ANALYSIS_DATA_SOURCE.md 목록 누락:** 요약본에 Search/Detail Switching이 빠져 있음 — 이 문서 7.1이 완전 목록.
10. **Observability:** mode를 Flow로 구독. 고정값으로 바꾸면 combine 구조 단순화 가능.

---

## 15. 이 문서의 사용법

- **제거 구현 전:** 이 문서를 as-is 진실원천으로 삼아 영향 범위를 확인한다.
- **제거 구현 중:** handoff Acceptance Criteria와 대조한다. 구현은 시작하지 말라는 요청이 있으면 코드 변경을 하지 않는다.
- **제거 완료 후:** `PROJECT.md` / `ANALYSIS_DATA_SOURCE.md` / `LOCAL_DATA.md`는 handoff대로 build-time 구조로 갱신하고, 본 문서는 아카이브성으로 두거나 “제거됨” 고지를 추가한다.
