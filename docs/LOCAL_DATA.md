# LOCAL_DATA.md - RECAP 로컬 데이터 구현

이 문서는 현재 `:core:data` 모듈에 있는 로컬 데이터 관련 구현체의 책임과 연결 방식을 정리한다. 로컬 데이터는 기기 내 MediaStore 조회, Room DB, Preference DataStore, 앱 private file storage를 포함한다.

## 전체 구조

```text
:core:data
├── LocalScreenshotDataSource.kt        # MediaStore 기반 로컬 스크린샷 조회
├── RecapDatabase.kt                    # Room DB 정의
├── DatabaseModule.kt                   # Room DB/DAO Hilt 제공
├── UserPreferencesDataStoreOwner.kt    # user_preferences DataStore owner
├── UserPreferencesModule.kt            # DataStore Hilt 제공
├── UserPreferencesRepository.kt        # 사용자 설정 repository
└── screenshot/
    ├── permission/                     # 이미지 권한 인터페이스/모듈
    ├── analysis/                       # 분석 repository (mock/remote/switching)
    ├── backend/                        # Mock/Remote 백엔드 모드 전환
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
- `MediaStore.Images.Media`
- `RELATIVE_PATH = "DCIM/Screenshots/"` 또는 `"Pictures/Screenshots/"`
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
- `buildImagePath(imageId)`
- `buildThumbnailPath(imageId)`
- `copyImageFromUri(imageId, sourceUri)`

주의사항:
- 현재 썸네일 생성 pipeline은 없다.
- `copyImageFromUri`는 실패 시 예외를 던지지 않고 `null`을 반환한다.
- 파일명은 현재 `imageId` 그대로 사용한다. 외부 입력을 직접 imageId로 쓰는 경우 path-safe 값인지 확인해야 한다.

## Preference DataStore

### `UserPreferencesDataStoreOwner`

역할:
- `Context.userPreferencesDataStore` delegate의 단일 owner다.
- DataStore name은 `user_preferences`다.

### `UserPreferencesModule`

역할:
- `user_preferences` DataStore를 Hilt singleton으로 제공한다.
- `@UserPreferencesDataStore` qualifier로 같은 타입의 다른 DataStore와 구분한다.

### `UserPreferencesRepository`

역할:
- 사용자 설정 접근 API를 제공한다.
- 현재는 온보딩 완료 여부/단계만 관리한다.

주요 API:
- `onboardingCompleted: Flow<Boolean>`
- `setOnboardingCompleted(completed)`
- `getOnboardingStep()` / `setOnboardingStep(step)` / `clearOnboardingStep()`

저장 key:
- `onboarding_completed`
- `onboarding_step`

주의사항:
- 스크린샷 backend 모드는 `ScreenshotBackendModeStore`가 같은 `user_preferences` DataStore에서 관리한다.
- 새 설정을 추가할 때는 같은 `user_preferences` DataStore를 사용하고, 별도 DataStore 파일을 만들지 않는다.

### `ScreenshotBackendModeStore`

역할:
- 전역 스크린샷 backend 모드(`MOCK` / `REMOTE`)를 관찰·저장한다.
- Debug에서만 저장값을 사용하고, non-debug 현재 단계는 항상 `MOCK`이다.

저장 key:
- `screenshot_backend_mode` (신규, 우선)
- `analysis_data_source_mode` (legacy fallback, `setMode` 시 제거)

자세한 전환 구조는 `docs/ANALYSIS_DATA_SOURCE.md`를 본다.

## Mock backend vs Remote backend 저장 SoT

| | Mock backend | Remote backend |
|--|--------------|----------------|
| 정보카드 SoT | Room `ScreenshotCardRepository` | 서버 Capture/Storage API |
| 원본 이미지 | 앱 private `ScreenshotImageStorage` | 서버 URL (기기 원본 캐시 없음) |
| 썸네일 | 앱 private 썸네일 파일 | `RemoteCaptureThumbnailCache` (capture ID 기반 로컬 캐시) |
| Mock 구현 | `MockHomeRepository`, `MockStorageRepository`, `MockCaptureMutationRepository` 등 | — |
| Remote 구현 | — | `RemoteHomeRepository`, `RemoteStorageRepository`, `RemoteCaptureMutationRepository` 등 |

모드 전환 시 `MockScreenshotDataResetter`가 Mock Room 카드와 private 원본/썸네일만 삭제한다. session token·onboarding·일반 사용자 설정은 유지한다.

## 현재 연결되지 않은 부분

다음 항목은 구현체는 존재하지만 아직 앱 flow에 완전히 연결되지 않았다.

- Capture 상세의 Mock/Remote repository 전환은 아직 없다. (즐겨찾기 토글은 `CaptureMutationRepository`로 연결됨)
- 썸네일 생성 pipeline(원본→썸네일)은 Mock 경로에 제한적으로만 있다.
- 이미지 bytes는 Room BLOB으로 저장하지 않는다.

Remote 업로드/정리 파이프라인(`issueUploadUrls` → PUT → `organize` → status poll → ack)은
`RemoteScreenshotAnalysisRepository`에서 `CaptureRepository`를 통해 연결된다.

## 테스트

현재 로컬 데이터 관련 테스트:
- `UserPreferencesRepositoryTest`
- `ScreenshotImageStorageTest`
- `ScreenshotCardDaoTest`
- `RecapDatabaseMigration2To3Test`
- `ScreenshotImageStorageTest`

검증 범위:
- DataStore 기본값/저장
- 앱 private image/thumbnail directory 및 stable path
- screenshot card 저장 순서
- key fields 교체 저장
- favorite state 독립 갱신
- card 삭제 시 key fields 제거
- repository round-trip

기본 검증 명령:

```powershell
$env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"; .\gradlew.bat testDebugUnitTest
```

