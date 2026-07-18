package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

class ScreenshotMockRandomizer(
    private val nextCaptureId: () -> Long = defaultCaptureIdSupplier(),
    private val nextOrganizedAt: () -> Instant = { Instant.now() },
    private val nextContentTypeIndex: () -> Int = { Random.nextInt(ScreenshotContentType.entries.size) },
) {
    fun captureId(): Long = nextCaptureId()

    fun organizedAt(): Instant = nextOrganizedAt()

    fun contentTypeIndex(): Int = nextContentTypeIndex()

    companion object {
        /**
         * 프로세스 재시작마다 1부터 다시 시작하는 시퀀스를 쓰지 않는다.
         * Room PK REPLACE로 기존 카드/이미지 파일을 조용히 덮어쓰는 충돌을 피하기 위해
         * UUID 기반 양의 Long을 생성한다. 테스트는 결정적 supplier를 주입한다.
         */
        private fun defaultCaptureIdSupplier(): () -> Long = ::nextPositiveCaptureId

        internal fun nextPositiveCaptureId(): Long {
            while (true) {
                val candidate = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
                if (candidate > 0L) {
                    return candidate
                }
            }
        }
    }
}
