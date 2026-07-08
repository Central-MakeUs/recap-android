# SCREENSHOT_MOCK_DATA.md

이 문서는 스크린샷 분석 mock repository의 JSON-equivalent 계약과 생성 규칙을 설명한다.

## 목적

실제 OCR/Firebase AI 분석 경로가 준비되기 전까지, 호출자가 스크린샷 파일명만 전달해도 분석 결과 형태의 mock 데이터를 받을 수 있게 한다.

mock repository는 이미지 바이트, 픽셀, URI 내용을 읽지 않는다.

## JSON-equivalent shape

```json
{
  "image_id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "스크린샷capture_01.png",
  "summary": "요약capture_01.png",
  "content_types": {
    "primary_content_type": "SHOPPING_PRODUCT"
  },
  "key_fields": [
    {
      "label": "라벨1",
      "value": "값1",
      "display_priority": 1,
      "is_sensitive": false
    },
    {
      "label": "라벨2",
      "value": "값2",
      "display_priority": 2,
      "is_sensitive": false
    },
    {
      "label": "라벨3",
      "value": "값3",
      "display_priority": 3,
      "is_sensitive": true
    }
  ],
  "confidence": "HIGH"
}
```

### Kotlin 필드 매핑

| JSON | Kotlin |
|------|--------|
| `image_id` | `ScreenshotAnalysisResult.imageId` |
| `title` | `ScreenshotAnalysisResult.title` |
| `summary` | `ScreenshotAnalysisResult.summary` |
| `content_types.primary_content_type` | `ScreenshotAnalysisResult.contentTypes.primaryContentType` |
| `key_fields` | `ScreenshotAnalysisResult.keyFields` |
| `key_fields[].display_priority` | `ScreenshotKeyField.displayPriority` |
| `key_fields[].is_sensitive` | `ScreenshotKeyField.isSensitive` |
| `confidence` | `ScreenshotAnalysisResult.confidence` |

`secondary_content_types`는 현재 mock 계약에 포함하지 않는다.

## Mock 생성 규칙

입력:

- `ScreenshotAnalysisInput(fileName: String)`

출력:

- `ScreenshotAnalysisResult`

규칙:

1. `image_id`
   - 기본값: `UUID.randomUUID().toString()`
2. `title`
   - `"스크린샷" + fileName`
3. `summary`
   - `"요약" + fileName`
4. `content_types.primary_content_type`
   - 10개 enum 중 하나를 무작위 선택
5. `key_fields`
   - 항상 3개 생성
   - 1번: `label = "라벨1"`, `value = "값1"`, `display_priority = 1`
   - 2번: `label = "라벨2"`, `value = "값2"`, `display_priority = 2`
   - 3번: `label = "라벨3"`, `value = "값3"`, `display_priority = 3`
   - 각 항목의 `is_sensitive`는 독립적으로 `random < 0.05`이면 `true`
6. `confidence`
   - 단일 `random` 값으로 아래 구간에 따라 결정
   - `[0.0, 0.6)` → `HIGH` (60%)
   - `[0.6, 0.85)` → `MEDIUM` (25%)
   - `[0.85, 1.0]` → `LOW` (15%)

배치 분석 시 입력 순서를 그대로 유지한다.

## Enum 값

### `primary_content_type`

- `JOB_CAREER`
- `SHOPPING_PRODUCT`
- `PLACE_RESTAURANT`
- `SCHEDULE_RESERVATION`
- `INFO_KNOWLEDGE`
- `DESIGN_REFERENCE`
- `BOOK_CONTENT`
- `BENEFIT_EVENT`
- `RECORD_CAPTURE`
- `OTHER`

### `confidence`

- `HIGH`
- `MEDIUM`
- `LOW`

## 구현 위치

### 모델 (`:core:model`)

```text
core/model/src/main/java/com/chalkak/recap/core/model/screenshot/
├── ScreenshotAnalysisConfidence.kt
├── ScreenshotAnalysisResult.kt
├── ScreenshotContentType.kt
├── ScreenshotContentTypes.kt
└── ScreenshotKeyField.kt
```

### 데이터 (`:core:data`)

```text
core/data/src/main/java/com/chalkak/recap/core/data/screenshot/
├── MockScreenshotAnalysisRepository.kt
├── ScreenshotAnalysisInput.kt
├── ScreenshotAnalysisModule.kt
├── ScreenshotAnalysisRepository.kt
└── ScreenshotMockRandomizer.kt
```

### 테스트

```text
core/data/src/test/java/com/chalkak/recap/core/data/screenshot/
└── MockScreenshotAnalysisRepositoryTest.kt
```

## 테스트 가능한 랜덤성

`ScreenshotMockRandomizer`가 아래 supplier를 주입받도록 분리되어 있다.

- `nextImageId`
- `nextUnitDouble`
- `nextContentTypeIndex`

단위 테스트는 경계값만 검증하며, 반복 샘플링으로 확률 분포를 검증하지 않는다.

## 범위 밖

- 실제 OCR
- Firebase AI 호출
- 이미지 디코딩
- Room/DataStore 저장
- UI 표시
