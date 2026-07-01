package com.chalkak.recap.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chalkak.recap.core.data.ocr.OcrDao
import com.chalkak.recap.core.data.ocr.OcrJobEntity
import com.chalkak.recap.core.data.ocr.OcrResultEntity

@Database(
    entities = [
        OcrJobEntity::class,
        OcrResultEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RecapDatabase : RoomDatabase() {
    abstract fun ocrDao(): OcrDao
}
