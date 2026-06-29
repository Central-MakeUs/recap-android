package com.chalkak.recap.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chalkak.recap.core.data.ocr.OcrDao
import com.chalkak.recap.core.data.ocr.OcrJobEntity
import com.chalkak.recap.core.data.ocr.OcrResultEntity

@Database(
    entities = [
        OcrJobEntity::class,
        OcrResultEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class RecapDatabase : RoomDatabase() {
    abstract fun ocrDao(): OcrDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE ocr_results ADD COLUMN rawTextBlocksJson TEXT NOT NULL DEFAULT '[]'",
                )
            }
        }
    }
}
