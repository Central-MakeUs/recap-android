package com.chalkak.recap.core.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `screenshot_cards` (
                `imageId` TEXT NOT NULL,
                `sourceImageUri` TEXT,
                `storedImagePath` TEXT,
                `thumbnailPath` TEXT,
                `title` TEXT NOT NULL,
                `summary` TEXT NOT NULL,
                `primaryContentType` TEXT NOT NULL,
                `confidence` TEXT NOT NULL,
                `isFavorite` INTEGER NOT NULL,
                `createdAtMillis` INTEGER NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`imageId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `screenshot_key_fields` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `imageId` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `value` TEXT NOT NULL,
                `displayPriority` INTEGER NOT NULL,
                `isSensitive` INTEGER NOT NULL,
                FOREIGN KEY(`imageId`) REFERENCES `screenshot_cards`(`imageId`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_screenshot_key_fields_imageId`
            ON `screenshot_key_fields` (`imageId`)
            """.trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `screenshot_cards`
            ADD COLUMN `body` TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}
