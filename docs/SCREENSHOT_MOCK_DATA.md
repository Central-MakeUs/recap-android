# SCREENSHOT_MOCK_DATA.md

이 문서는 스크린샷 분석 mock repository의 JSON-equivalent 계약과 생성 규칙을 설명한다.
배포된 RE-CAP Swagger `CaptureDetailResponse`와 동일한 완료된 캡처 detail 형태를 따른다.

## 목적

MOCK 모드에서 호출자가 스크린샷 파일명만 전달해도
`CaptureDetailResponse`와 동일한 분석 결과 형태의 mock 데이터를 받을 수 있게 한다.
REMOTE 모드의 실제 upload/organize/poll은 `RemoteScreenshotAnalysisRepository` + `CaptureRepository`가 담당한다.

mock repository는 이미지 바이트, 픽셀, URI 내용을 읽지 않는다.

## 관련 계약과의 구분

| 계약 | 용도 | mock 범위 |
|------|------|-----------|
| `CaptureDetailResponse` | 저장된 캡처 1건의 detail | **이번 mock 결과** |
| `CaptureSummaryResponse` | 목록용 summary (`thumbnailUrl`, body/originalImageUrl 없음) | mock 대상 아님 |
| Upload workflow (`UploadItem.imageKey`/`uploadUrl`, organize `imageKeys`) | 업로드·정리 파이프라인 | mock 대상 아님 |

## JSON-equivalent shape (`CaptureDetailResponse`)

```json
{
  "captureId": 1001,
  "typeCode": "SHOPPING",
  "title": "스크린샷capture_01.png",
  "summary": "요약capture_01.png",
  "body": "본문capture_01.png",
  "originalImageUrl": "mock://captures/1001",
  "isFavorite": false,
  "organizedAt": "2026-07-18T00:00:00Z"
}
```

예시 값은 생성 규칙을 반영한 mock 출력이다. 특정 식당/장소 fixture를 하드코딩하지 않는다.
`typeCode`는 실행 시 무작위 선택되며, `title`/`summary`/`body`는 파일명 기반 규칙을 유지한다.

### Kotlin 필드 매핑

| JSON (camelCase) | Kotlin |
|------------------|--------|
| `captureId` | `ScreenshotAnalysisResult.captureId: Long` |
| `typeCode` | `ScreenshotAnalysisResult.typeCode: ScreenshotContentType` |
| `title` | `ScreenshotAnalysisResult.title` |
| `summary` | `ScreenshotAnalysisResult.summary` |
| `body` | `ScreenshotAnalysisResult.body` |
| `originalImageUrl` | `ScreenshotAnalysisResult.originalImageUrl` |
| `isFavorite` | `ScreenshotAnalysisResult.isFavorite` |
| `organizedAt` | `ScreenshotAnalysisResult.organizedAt: java.time.Instant` |

로컬 전용 이미지 참조(`sourceImageUri`, `storedImagePath`, `thumbnailPath`)는
`ScreenshotCardImageRefs`에만 두고 Swagger-aligned result 모델에 섞지 않는다.

## Mock 생성 규칙

입력:

- `ScreenshotAnalysisInput(fileName: String)`

출력:

- `ScreenshotAnalysisResult`

규칙:

1. `captureId`
   - 기본값: injectable 양의 `Long` supplier
   - 기본 공급자는 UUID 기반 양의 `Long`을 생성한다. 프로세스 재시작마다 `1, 2, ...`로
     다시 시작하지 않아 Room `REPLACE`로 기존 카드/이미지 파일을 덮어쓰지 않는다
   - 배치 내 충돌이 없어야 하며, 테스트에서 결정적으로 주입 가능해야 한다
2. `typeCode`
   - 9개 enum 중 하나를 기존과 동일하게 random index로 선택
3. `title`
   - `"스크린샷" + fileName`
4. `summary`
   - `"요약" + fileName`
5. `body`
   - `"본문" + fileName`
6. `originalImageUrl`
   - `"mock://captures/" + captureId`
   - nonblank이며 실제 백엔드 객체로 오인되지 않는 mock URL
7. `isFavorite`
   - 기본값: `false`
8. `organizedAt`
   - injectable clock/time supplier로 생성 (테스트에서 wall clock 직접 호출 금지)

배치 분석 시 입력 순서를 그대로 유지한다.

## Enum 값 (`typeCode`)

Swagger wire 값과 동일하다.

- `JOB`
- `SHOPPING`
- `PLACE`
- `SCHEDULE`
- `KNOWLEDGE`
- `CONTENT`
- `BENEFIT`
- `RECORD`
- `ETC`

## 구현 위치

### 모델 (`:core:model`)

```text
core/model/src/main/java/com/chalkak/recap/core/model/screenshot/
├── ScreenshotAnalysisResult.kt
└── ScreenshotContentType.kt
```

### 데이터 (`:core:data`)

```text
core/data/src/main/java/com/chalkak/recap/core/data/screenshot/
├── MockScreenshotAnalysisRepository.kt
├── ScreenshotAnalysisInput.kt
├── ScreenshotAnalysisModule.kt
├── ScreenshotAnalysisRepository.kt
├── ScreenshotMockRandomizer.kt
├── image/
│   └── ScreenshotImageStorage.kt
└── persistence/
    ├── ScreenshotCardDao.kt
    ├── ScreenshotCardEntities.kt
    ├── ScreenshotCardMappers.kt
    ├── ScreenshotCardModule.kt
    └── ScreenshotCardRepository.kt
```

### 테스트

```text
core/data/src/test/java/com/chalkak/recap/core/data/screenshot/
├── MockScreenshotAnalysisRepositoryTest.kt
├── image/
│   └── ScreenshotImageStorageTest.kt
└── persistence/
    └── ScreenshotCardDaoTest.kt
```

## 테스트 가능한 랜덤성

`ScreenshotMockRandomizer`가 아래 supplier를 주입받도록 분리되어 있다.

- `nextCaptureId: () -> Long` (미주입 시 UUID 기반 양의 Long; 1부터 재시작하지 않음)
- `nextOrganizedAt: () -> Instant`
- `nextContentTypeIndex: () -> Int`

단위 테스트는 경계값만 검증하며, 반복 샘플링으로 확률 분포를 검증하지 않는다.
테스트에서는 고정 `Instant` 값과 결정적 `captureId` supplier를 사용한다.
서로 다른 기본 randomizer 인스턴스가 동일 순차 시퀀스(`1..n`)를 재사용하지 않는지도 회귀 검증한다.

## 저장 계약

- Room `screenshot_cards` 테이블에 분석 카드를 저장한다. 스키마는 fresh-install `version = 1`이다.
- 컬럼: `captureId`(PK), `typeCode`, `title`, `summary`, `body`, `originalImageUrl`,
  `isFavorite`, `organizedAtMillis`, `updatedAtMillis`, 로컬 이미지 ref 문자열
- `organizedAt`는 domain에서 `Instant`이고 Room 경계에서 epoch millis로 변환한다.
- 레거시 `confidence`, `screenshot_key_fields`는 없다.
- `isFavorite`는 카드 테이블 컬럼으로 저장되며, 분석 결과와 독립적으로 갱신할 수 있다.
- 이미지 바이트는 Room BLOB으로 저장하지 않는다. `ScreenshotImageStorage`가
  `files/recap/images/`, `files/recap/thumbnails/` 경로를 관리하고,
  파일명은 `captureId`에서 파생한 안정적인 문자열을 사용한다.
- 기존 version-4 개발 DB는 마이그레이션하지 않는다. 앱 데이터 삭제 또는 재설치가 필요하다.

## 범위 밖

- `CaptureSummaryResponse` 목록 엔드포인트 소비
- 실제 OCR / Firebase AI 스키마·프롬프트 계약
- UI 리디자인 또는 카피 변경
- Organize cancel / pending-result 앱 재시작 복구 / 완료 notification
