package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.util.UUID
import kotlin.random.Random

class ScreenshotMockRandomizer(
    private val nextImageId: () -> String = { UUID.randomUUID().toString() },
    private val nextUnitDouble: () -> Double = { Random.nextDouble() },
    private val nextContentTypeIndex: () -> Int = { Random.nextInt(ScreenshotContentType.entries.size) },
) {
    fun imageId(): String = nextImageId()

    fun unitDouble(): Double = nextUnitDouble()

    fun contentTypeIndex(): Int = nextContentTypeIndex()
}
