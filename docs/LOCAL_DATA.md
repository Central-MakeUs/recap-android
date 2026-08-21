# LOCAL_DATA.md - RECAP 로컬 데이터 구현

이 문서는 현재 `:core:data` 모듈에 있는 로컬 데이터 관련 구현체의 책임과 연결 방식을 정리한다. 로컬 데이터는 기기 내 MediaStore 조회, Room DB, Preference DataStore, 앱 private file storage를 포함한다.

## 전체 구조

```text
:core:data
├── LocalScreenshotDataSource.kt        # MediaStore 기반 로컬 스크린샷 조회
├── LocalAppDataResetter.kt             # 로그아웃 wipe / 계정 전환 wipe
├── RecapDatabase.kt                    # Room DB 정의
├── DatabaseModule.kt                   # Room DB/DAO Hilt 제공
├── UserPreferencesDataStoreOwner.kt    # user_preferences DataStore owner
├── UserPreferencesModule.kt            # DataStore Hilt 제공
├── UserPreferencesRepository.kt        # 사용자 설정 repository
├── account/
│   ├── AccountOwnerDataStoreOwner.kt   # account_owner DataStore owner
│   ├── AccountOwnerModule.kt           # account_owner Hilt 제공
│   ├── AccountOwnerStore.kt            # 소유자 해시 + 기기 로컬 salt 저장
│   └── AccountOwnerHasher.kt           # SHA-256("{salt}|kakao:{id}")
└── screenshot/
    ├── permission/                     # 이미지 권한 인터페이스/모듈
    ├── analysis/                       # 분석 repository (전 빌드 Demo 카탈로그)
    ├── backend/                        # MockScreenshotDataResetter 등
    ├── image/                          # 앱 private 이미지 경로 관리
    └── persistence/                    # 분석 카드 Room 저장
```

## MediaStore 스크린샷 조회

### `LocalScreenshotDataSource`

역할:
- 기기 MediaStore에서 로컬 스크린샷 목록을 조회한다.
- Android 버전에 맞는 이미지 권한 요청 목록을 제공한다.
- 현재 이미지 접근 수준을 `ImageAccessLevel`로 반환한다.
- `ImagePermissionRepository`를 구현한다.

주요 API:
- `imagePermissionRequest()`
- `currentImageAccessLevel()`
- `queryRecentScreenshots(limit)`
- `queryAllScreenshots()`

현재 조회 범위:

- `MediaStore.Images.Media` 전체 (Screenshots 폴더로 제한하지 않음). 데모 카탈로그 파일이 다른 경로에 있어도 피커에 나타나게 한다.
- 최신순 정렬: `DATE_ADDED DESC`

주의사항:
- 권한이 없으면 빈 목록을 반환한다.
- MediaStore 조회 실패는 예외를 밖으로 던지지 않고 빈 목록으로 처리한다.
- 반환 모델은 `LocalImage`이며, 실제 이미지 bytes를 읽지 않는다.

## Room DB

### `RecapDatabase`

역할:
- 앱 로컬 Room database의 단일 정의다.
- DB 파일명은 `recap.db`다.
- 현재 version은 `1`다.
- `exportSchema = false` 상태다.
- schema 변경 시 version을 올리고 명시적 migration을 추가한다. destructive fallback은 사용하지 않는다.

등록된 entity:
- `ScreenshotCardEntity`

제공 DAO:
- `screenshotCardDao()`

### `DatabaseModule`

역할:
- Hilt `SingletonComponent`에 `RecapDatabase` singleton을 제공한다.
- `ScreenshotCardDao`를 DI로 제공한다.

## 분석 카드 로컬 저장

### `screenshot/persistence/ScreenshotCardEntities`

저장 테이블:
- `screenshot_cards`

`screenshot_cards` 역할:
- 분석된 스크린샷 카드 row다.
- primary key는 `captureId`다.
- 이미지 참조 경로와 분석 요약 정보를 저장한다.

주요 컬럼:
- `captureId`
- `sourceImageUri`
- `storedImagePath`
- `thumbnailPath`
- `title`
- `summary`
- `body`
- `typeCode`
- `originalImageUrl`
- `isFavorite`
- `organizedAtMillis`
- `updatedAtMillis`

### `screenshot/persistence/ScreenshotCardDao`

역할:
- 분석 카드 저장/조회/수정/삭제를 담당한다.

주요 API:
- `observeAllCards()`: `organizedAtMillis DESC` 정렬로 전체 observe
- `observeCard(captureId)`: 단일 카드 observe
- `getCardByCaptureId(captureId)`
- `insertCards(cards)`
- `updateFavorite(captureId, isFavorite, updatedAtMillis)`
- `updateCardContent(captureId, title, summary, body, typeCode, updatedAtMillis)`
- `deleteByCaptureId(captureId)`

### `screenshot/persistence/ScreenshotCardMappers`

역할:
- Room entity와 domain model 사이를 변환한다.

### `screenshot/persistence/ScreenshotCardRepository`

역할:
- DAO 세부사항을 숨기는 repository facade다.
- UI/feature 계층은 가능하면 DAO가 아니라 이 repository를 사용한다.

주요 API:
- `observeStoredCards()`
- `observeCard(captureId)`
- `getCard(captureId)`
- `updateFavorite(captureId, isFavorite)`
- `updateCardContent(...)`
- `deleteCard(captureId)`
- `deleteCards(captureIds)`
- `deleteAllCards()`

### `screenshot/persistence/ScreenshotCardModule`

역할:
- `DefaultScreenshotCardRepository`를 `ScreenshotCardRepository`로 Hilt binding한다.

## 앱 private 이미지 저장

### `screenshot/image/ScreenshotImageStorage`

역할:
- 분석 카드에서 사용할 이미지/썸네일 파일 경로를 앱 private storage 아래에 안정적으로 구성한다.
- Room에는 이미지 bytes를 저장하지 않고, URI 또는 파일 경로 문자열만 저장한다.

저장 위치:
- 원본 복사 대상: `context.filesDir/recap/images/`
- 썸네일 대상: `context.filesDir/recap/thumbnails/`

주요 API:
- `resolveImagesDirectory()`
- `resolveThumbnailsDirectory()`
- `buildImagePath(captureId)` / `buildThumbnailPath(captureId)`
- `copyImageFromUri(captureId, sourceUri)`
- `createThumbnailFromUri(captureId, sourceUri)` / `createThumbnailFromStoredImage(captureId)`
- `cacheThumbnailBytes(captureId, bytes)`
- `clearStoredImages()` / `deleteStoredImages(captureIds)`

주의사항:
- 원본 파일명은 `captureId` 문자열, 썸네일은 `{captureId}.jpg`다.
- Mock 경로는 URI 또는 저장된 원본에서 폭/높이 50% JPEG(품질 80) 썸네일을 만든다. Remote 목록/상세는 `RemoteCaptureThumbnailCache`가 서버 바이트를 `cacheThumbnailBytes`로 같은 경로에 둔다.
- `copyImageFromUri`와 썸네일 생성/캐시는 실패 시 예외를 던지지 않고 `null`을 반환한다.
- `clearStoredImages()`는 계정 전환 wipe용이다. 디렉터리를 완전히 비우지 못하면 `false`를 반환한다.

## Preference DataStore

### `UserPreferencesDataStoreOwner`

역할:
- `Context.userPreferencesDataStore` delegate의 단일 owner다.
- DataStore name은 `user_preferences`다.
- `ReplaceFileCorruptionHandler`로 파일 손상 시 `emptyPreferences()`로 교체한다. 온보딩·세션·deviceId·검색 설정이 함께 리셋된다.

### `PreferencesDataStoreExt`

역할:
- `DataStore<Preferences>.safeData(name)`이 읽기 `IOException`에서 최대 3회(재시도 2회, 100ms·300ms 지연) 재수집한다. 3회 모두 실패하면 예외를 그대로 던지므로 호출 측이 실패를 처리해야 한다. 취소와 non-IO 예외는 삼키지 않는다.
- `name`은 로그에만 쓰며, 어느 DataStore 파일에서 실패했는지 구분하기 위해 호출 측이 넘긴다.
- `UserPreferencesRepository`, `SessionTokenStore`, `DeviceIdProvider`, `RecentSearchStore`, `AccountOwnerStore`가 이 경로로 읽는다.

### `UserPreferencesModule`

역할:
- `user_preferences` DataStore를 Hilt singleton으로 제공한다.
- `@UserPreferencesDataStore` qualifier로 같은 타입의 다른 DataStore와 구분한다.

### `AccountOwnerStore` / `account_owner` DataStore

역할:
- 카카오 `user.id`의 SHA-256 해시(`{salt}|kakao:{id}` → hex)와 기기 로컬 salt만 별도 Preferences DataStore `account_owner`에 저장한다. 원문 ID는 디스크에 두지 않는다.
- 세션 만료/`Reauth`에서는 유지하고, 로그인 시 저장된 해시와 비교한다.
- 해시 없음(기존 설치) 또는 불일치면 `LocalAppDataResetter.wipeAndRebindOwner(hash)`로 Room·썸네일 캐시·최근 검색·계정 종속 preference를 wipe한 뒤 새 해시를 저장한다. `onboardingCompleted`·`deviceId`·알림 설정은 유지한다.
- 명시적 로그아웃/탈퇴의 `resetAccountLocalData()`에서 owner hash와 salt를 `clear()`한다. 온보딩 완료 플래그와 세션은 유지하고, 세션은 `logout()`/`withdraw()`가 서버 호출 후 비운다. 루트는 `Reauth`로 간다.

주요 API:
- `getHash()` / `setHash(hash)` / `getOrCreateSalt()` / `clear()`
- `AccountOwnerHasher.hashKakaoUserId(userId)`
- `LocalAppDataResetter.wipeAndRebindOwner(hash)` / `resetAccountLocalData()` / `resetDatabaseAndOnboarding()`

주의사항:
- 일반 사용자 설정은 `user_preferences`에 두고, 계정 소유자 마커만 `account_owner`로 분리한다(세션·온보딩 wipe와 수명 분리).
- salt 없는 SHA-256은 카카오 `user.id` 자릿수가 짧아 전수 조사로 역산된다. 기기 로컬 salt를 반드시 함께 쓴다.
- salt가 사라지면 이전 해시와 절대 일치하지 않아 다음 로그인에서 wipe가 일어난다(안전 우선).
- 두 값 모두 `backup_rules.xml`·`data_extraction_rules.xml`에서 백업/기기 전송 제외 대상이다(`datastore/account_owner.preferences_pb`).
- 로그인 chokepoint는 `AuthRepository.signInWithKakao`다. `me()` 실패 시 서버 로그인/Main 진입을 하지 않는다.
- wipe는 fail-closed다. 이미지/썸네일 삭제가 완전히 끝나지 않으면 `wipeAndRebindOwner`가 예외를 던지고 해시를 갱신하지 않는다. 이전 해시가 남으므로 다음 로그인에서 wipe를 다시 시도한다.
- `reconcileAccountOwner` 경로의 DataStore·wipe 실패는 모두 `Result.failure(AuthException(AuthError.Unknown))`으로 매핑된다. `signInWithKakao`는 예외를 던지지 않는다.
- 계정 전환 wipe는 서버 로그인보다 먼저 실행한다. 서버 로그인이 실패해도 로컬은 이미 비어 있고, 서버가 SoT이므로 재로그인 후 다시 동기화한다.

### `UserPreferencesRepository`

역할:
- 사용자 설정 접근 API를 제공한다.
- 온보딩 완료 여부, 알림 설정, MOCK 모드용 AI 데이터 전송 동의 상태를 관리한다.

주요 API:
- `onboardingCompleted: Flow<Boolean>`
- `setOnboardingCompleted(completed)`
- `organizeCompleteNotificationEnabled: Flow<Boolean>`
- `setOrganizeCompleteNotificationEnabled(enabled)`
- `getAiDataTransferConsentStatus()` / `setAiDataTransferConsent(consented, consentedAt)`
- `clearAccountScopedPreferences()`

저장 key:
- `onboarding_completed`
- `organize_complete_notification_enabled`
- `ai_data_transfer_consented` / `ai_data_transfer_consented_at` (MOCK consent SoT)

주의사항:
- 일반 사용자 설정은 같은 `user_preferences` DataStore에 추가한다. 계정 소유자 해시만 예외적으로 `account_owner` DataStore를 사용한다.
- AI 동의 DataStore 값은 MOCK backend에서만 사용한다. REMOTE는 서버 consent API가 SoT다.
- AI 동의는 계정 종속 값이라 `clearAccountScopedPreferences()`가 지운다. 계정 전환 wipe와 로그아웃 reset이 이 API를 호출한다. 계정별로 다시 받아야 하는 설정을 추가할 때는 여기에도 등록한다.
- 스크린샷 backend(Mock/Remote)는 DataStore가 아니라 `:core:data` `BuildConfig.USE_MOCK_BACKEND`로 빌드 시 고정된다. 자세한 선택은 `docs/ANALYSIS_DATA_SOURCE.md`를 본다.

## Mock backend vs Remote backend 저장 SoT

| | Mock backend | Remote backend |
|--|--------------|----------------|
| 정보카드 SoT | Room `ScreenshotCardRepository` | 서버 Capture/Storage API |
| 원본 이미지 | 앱 private `ScreenshotImageStorage` | 서버 URL (기기 원본 캐시 없음) |
| 썸네일 | 앱 private 썸네일 파일 | `RemoteCaptureThumbnailCache` (capture ID 기반 로컬 캐시) |
| 데이터 요약 | Room 카드 수 (`MockUserRepository.getDataSummary`) | `GET /api/v1/users/me/data-summary` |
| AI 동의 | `user_preferences` consent keys | `GET/POST/DELETE /api/v1/users/me/consent` |
| 전체 데이터 삭제 | `MockScreenshotDataResetter` | `DELETE /api/v1/users/me/data` + 로컬 캐시 정리 |
| Mock 구현 | `MockHomeRepository`, `MockStorageRepository`, `MockCaptureMutationRepository`, `MockUserRepository` 등 | — |
| Remote 구현 | — | `RemoteHomeRepository`, `RemoteStorageRepository`, `RemoteCaptureMutationRepository`, `RemoteScreenshotDetailRepository`, `RemoteUserRepository` 등 |

개발자 옵션의 스크린샷 데이터 초기화와 Mock `deleteAccountData`는 `MockScreenshotDataResetter`로 Mock Room 카드와 private 원본/썸네일만 삭제한다. session token·onboarding·MOCK consent·일반 사용자 설정은 유지한다. Remote 빌드에서도 이 액션은 서버 데이터 삭제로 바뀌지 않는다.
BuildConfig backend 선택 자체는 resetter와 무관하며, 런타임 모드 전환(및 전환 시 wipe)은 없다.

상세 로드는 `ScreenshotDetailRepository`, 즐겨찾기·content 편집·삭제는 `CaptureMutationRepository`다. Remote content 편집은 `PATCH /api/v1/captures/{captureId}`다.

Remote 업로드/정리 파이프라인(`issueUploadUrls` → PUT → `organize` → status poll → ack)은
`RemoteScreenshotAnalysisRepository`에서 `CaptureRepository`를 통해 연결된다.

## 테스트

현재 로컬 데이터 관련 테스트:
- `UserPreferencesRepositoryTest`
- `ScreenshotImageStorageTest`
- `ScreenshotCardDaoTest`
- `AccountOwnerStoreTest` / `AccountOwnerHasherTest`
- `LocalAppDataResetterTest`

검증 범위:
- DataStore 기본값/저장
- 앱 private image/thumbnail directory, `captureId` 기반 path, JPEG 썸네일 생성
- screenshot card 저장 순서
- favorite state 독립 갱신
- card 삭제
- repository round-trip
- 소유자 해시/salt 저장·초기화와 salt별 해시 분리
- 계정 전환 wipe의 fail-closed 동작(이미지 삭제 실패 시 해시 미갱신)과 로그아웃 reset의 fail-open 동작

기본 검증 명령:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

