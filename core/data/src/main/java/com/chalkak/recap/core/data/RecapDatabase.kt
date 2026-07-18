package com.chalkak.recap.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardDao
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardEntity

@Database(
    entities = [
        ScreenshotCardEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RecapDatabase : RoomDatabase() {
    abstract fun screenshotCardDao(): ScreenshotCardDao
}
