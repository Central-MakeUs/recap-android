# LOCAL_DATA.md - RECAP 로컬 데이터 구현

이 문서는 현재 `:core:data` 모듈에 있는 로컬 데이터 관련 구현체의 책임과 연결 방식을 정리한다. 로컬 데이터는 기기 내 MediaStore 조회, Room DB, Preference DataStore, 앱 private file storage를 포함한다.

## 전체 구조

```text
:core:data
├── LocalScreenshotDataSource.kt        # MediaStore 기반 로컬 스크린샷 조회
├── RecapDatabase.kt                    # Room DB 정의
├── RecapDatabaseMigrations.kt          # Room migration 정의
├── DatabaseModule.kt                   # Room DB/DAO Hilt 제공
├── UserPreferencesDataStoreOwner.kt    # user_preferences DataStore owner
├── UserPreferencesModule.kt            # DataStore Hilt 제공
├── UserPreferencesRepository.kt        # 사용자 설정 repository
├── ocr/                                # OCR 작업/결과 로컬 저장
└── screenshot/
    ├── image/                          # 앱 private 이미지 경로 관리
    └── persistence/                    # 분석 카드 Room 저장
```

## MediaStore 스크린샷 조회

### `LocalScreenshotDataSource`

역할:
- 기기 MediaStore에서 로컬 스크린샷 목록을 조회한다.
- Android 버전에 맞는 이미지 권한 요청 목록을 제공한다.
- 현재 이미지 접근 수준을 `ImageAccessLevel`로 반환한다.
- `OcrOrganizeRange` 기준으로 스크린샷 개수 또는 목록을 조회한다.

주요 API:
- `imagePermissionRequest()`
- `currentImageAccessLevel()`
- `countScreenshots(range)`
- `queryRecentScreenshots(limit)`
- `queryAllScreenshots()`
- `queryScreenshots(range)`

현재 조회 범위:
- `MediaStore.Images.Media`
- `RELATIVE_PATH = "DCIM/Screenshots/"`
- 최신순 정렬: `DATE_ADDED DESC`

주의사항:
- 권한이 없으면 빈 목록 또는 `0`을 반환한다.
- MediaStore 조회 실패는 예외를 밖으로 던지지 않고 빈 목록으로 처리한다.
- 반환 모델은 `LocalImage`이며, 실제 이미지 bytes를 읽지 않는다.

## Room DB

### `RecapDatabase`

역할:
- 앱 로컬 Room database의 단일 정의다.
- DB 파일명은 `recap.db`다.
- 현재 version은 `3`다.
- `exportSchema = false` 상태다.

등록된 entity:
- `OcrJobEntity`
- `OcrResultEntity`
- `ScreenshotCardEntity`
- `ScreenshotKeyFieldEntity`

제공 DAO:
- `ocrDao()`
- `screenshotCardDao()`

### `DatabaseModule`

역할:
- Hilt `SingletonComponent`에 `RecapDatabase` singleton을 제공한다.
- `OcrDao`, `ScreenshotCardDao`를 DI로 제공한다.
- `MIGRATION_1_2`와 `MIGRATION_2_3`를 Room builder에 등록한다.

### `RecapDatabaseMigrations`

현재 migration:
- `MIGRATION_1_2`
- `MIGRATION_2_3`

하는 일:
- `MIGRATION_1_2`: 기존 OCR 테이블을 유지한 채 `screenshot_cards` 테이블을 추가한다.
- `MIGRATION_1_2`: `screenshot_key_fields` 테이블을 추가한다.
- `MIGRATION_1_2`: `screenshot_key_fields.imageId` index를 추가한다.
- `MIGRATION_2_3`: `screenshot_cards`에 editable `body` 컬럼을 추가한다. 기존 row는 `''`로 초기화된다.

주의사항:
- 앱 데이터 삭제 후 새 설치하면 migration은 실행되지 않고 version 3 schema가 바로 생성된다.
- 기존 version 1/2 DB가 남아 있는 기기에서는 등록된 migration이 필요하다.

## OCR 로컬 저장

### `ocr/OcrEntities`

저장 테이블:
- `ocr_jobs`
- `ocr_results`

`ocr_jobs` 역할:
- WorkManager OCR 작업 단위의 상태를 저장한다.
- `jobId`가 primary key다.
- 작업 범위, 상태, 진행 개수, 시작/종료 시간, error message를 가진다.

`ocr_results` 역할:
- OCR 대상 이미지별 결과를 저장한다.
- `resultId`는 auto-generated primary key다.
- `jobId`, `imageUri`, `displayName`, `rawText`, `rawTextBlocksJson`, `sortIndex`를 가진다.

mapper:
- `OcrJobEntity.toDomain(results)`
- `OcrResultEntity.toDomain()`
- `List<OcrTextBlock>.toJson()`

### `ocr/OcrDao`

역할:
- 최신 OCR job과 그 결과를 observe한다.
- job/result insert 및 job progress/status update를 담당한다.

주요 API:
- `observeLatestJob()`
- `observeResults(jobId)`
- `insertJob(job)`
- `insertResult(result)`
- `updateJobProgress(...)`
- `finishJob(...)`
- `finishActiveJobs(...)`

### `ocr/OcrRepository`

역할:
- `LocalScreenshotDataSource`, `OcrDao`, `WorkManager`를 묶는 OCR facade다.
- 이미지 권한 관련 API는 `ImagePermissionRepository`로 노출한다.
- OCR 시작 시 기존 active job을 cancel 상태로 마감하고 새 WorkManager job을 enqueue한다.

주의사항:
- 실제 OCR 처리는 `OcrWorker`가 담당한다.
- repository는 UI가 Room/WorkManager 세부사항을 알지 않도록 숨긴다.

## 분석 카드 로컬 저장

### `screenshot/persistence/ScreenshotCardEntities`

저장 테이블:
- `screenshot_cards`
- `screenshot_key_fields`

`screenshot_cards` 역할:
- 분석된 스크린샷 카드의 parent row다.
- primary key는 `imageId`다.
- 이미지 참조 경로와 분석 요약 정보를 저장한다.

주요 컬럼:
- `imageId`
- `sourceImageUri`
- `storedImagePath`
- `thumbnailPath`
- `title`
- `summary`
- `body`
- `primaryContentType`
- `confidence`
- `isFavorite`
- `createdAtMillis`
- `updatedAtMillis`

`screenshot_key_fields` 역할:
- 분석 카드에 딸린 key-value field를 저장한다.
- `imageId`로 `screenshot_cards.imageId`를 참조한다.
- parent 삭제 시 cascade delete된다.
- `imageId` index가 있다.

주요 컬럼:
- `id`
- `imageId`
- `label`
- `value`
- `displayPriority`
- `isSensitive`

relation holder:
- `ScreenshotCardWithKeyFields`

### `screenshot/persistence/ScreenshotCardDao`

역할:
- 분석 카드와 key fields 저장/조회/수정/삭제를 담당한다.

주요 API:
- `observeAllCards()`: `createdAtMillis DESC` 정렬로 전체 observe
- `observeCard(imageId)`: 단일 카드 observe
- `getCardByImageId(imageId)`
- `saveAnalysisResults(entries)`
- `updateFavorite(imageId, isFavorite, updatedAtMillis)`
- `updateCardContent(imageId, title, summary, body, primaryContentType, updatedAtMillis)`
- `deleteByImageId(imageId)`

저장 정책:
- `saveAnalysisResults`는 transaction으로 실행한다.
- 기존 카드 재저장 시 기존 `createdAtMillis`는 유지한다.
- key fields는 해당 `imageId`의 기존 row를 삭제한 뒤 새 목록으로 교체한다.
- favorite은 `ScreenshotAnalysisResult.isFavorite` 값으로 저장된다.

### `screenshot/persistence/ScreenshotCardMappers`

역할:
- Room entity와 domain model 사이를 변환한다.
- 이미지 참조값은 `ScreenshotAnalysisResult`에 강제로 넣지 않고 별도 `ScreenshotCardImageRefs`로 둔다.

주요 타입:
- `ScreenshotCardImageRefs`
- `StoredScreenshotCard`
- `ScreenshotCardSaveEntry`

주요 mapper:
- `ScreenshotAnalysisResult.toCardEntity(...)`
- `ScreenshotAnalysisResult.toKeyFieldEntities()`
- `ScreenshotCardWithKeyFields.toStoredScreenshotCard()`

### `screenshot/persistence/ScreenshotCardRepository`

역할:
- DAO 세부사항을 숨기는 repository facade다.
- UI/feature 계층은 가능하면 DAO가 아니라 이 repository를 사용한다.

주요 API:
- `observeStoredCards()`
- `observeCard(imageId)`
- `getCard(imageId)`
- `saveAnalysisResults(results, imageRefsByImageId)`
- `updateFavorite(imageId, isFavorite)`
- `updateCardContent(imageId, title, summary, body, primaryContentType, updatedAtMillis)`
- `deleteCard(imageId)`
- `deleteCards(imageIds)`
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
- 현재는 온보딩 완료 여부만 관리한다.

주요 API:
- `onboardingCompleted: Flow<Boolean>`
- `setOnboardingCompleted(completed)`

저장 key:
- `onboarding_completed`

주의사항:
- 외부 API는 기존과 동일하게 유지한다.
- 새 설정을 추가할 때는 같은 `user_preferences` DataStore를 사용하고, 별도 DataStore 파일을 만들지 않는다.

## 현재 연결되지 않은 부분

다음 항목은 구현체는 존재하지만 아직 앱 flow에 완전히 연결되지 않았다.

- Home/Collection UI는 아직 `ScreenshotCardRepository`의 Room 데이터를 표시하지 않는다.
- 실제 OCR/Firebase AI 결과를 `ScreenshotCardRepository`에 저장하는 연결은 아직 없다.
- 썸네일 생성은 아직 없다.
- 이미지 bytes는 Room BLOB으로 저장하지 않는다.

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

