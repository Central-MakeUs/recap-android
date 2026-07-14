package com.chalkak.recap.core.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class RecapDatabaseMigration3To4Test {
    private lateinit var context: Context
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        dbFile = context.getDatabasePath("migration-3-4-test.db")
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }

    @After
    fun tearDown() {
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }

    @Test
    fun migration3To4_rewritesDesignReferenceToOtherAndPreservesOtherRows() {
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.absolutePath)
                .callback(
                    object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            MIGRATION_1_2.migrate(db)
                            MIGRATION_2_3.migrate(db)
                            db.execSQL(
                                """
                                INSERT INTO screenshot_cards (
                                    imageId, sourceImageUri, storedImagePath, thumbnailPath,
                                    title, summary, body, primaryContentType, confidence,
                                    isFavorite, createdAtMillis, updatedAtMillis
                                ) VALUES (
                                    'legacy-design', NULL, '/images/legacy-design', '/thumbs/legacy-design',
                                    '디자인 레퍼런스', '레거시 요약', '본문', 'DESIGN_REFERENCE', 'HIGH',
                                    1, 1000, 2000
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO screenshot_cards (
                                    imageId, sourceImageUri, storedImagePath, thumbnailPath,
                                    title, summary, body, primaryContentType, confidence,
                                    isFavorite, createdAtMillis, updatedAtMillis
                                ) VALUES (
                                    'already-other', 'content://other', '/images/already-other', NULL,
                                    '기타 카드', '기타 요약', '', 'OTHER', 'MEDIUM',
                                    0, 3000, 4000
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO screenshot_key_fields (
                                    imageId, label, value, displayPriority, isSensitive
                                ) VALUES ('legacy-design', 'label', 'value', 1, 0)
                                """.trimIndent(),
                            )
                            MIGRATION_3_4.migrate(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )

        val db = openHelper.writableDatabase
        try {
            db.query(
                """
                SELECT primaryContentType, title, summary, body, storedImagePath, thumbnailPath,
                       isFavorite, createdAtMillis, updatedAtMillis
                FROM screenshot_cards WHERE imageId = ?
                """.trimIndent(),
                arrayOf<Any?>("legacy-design"),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("OTHER", cursor.getString(0))
                assertEquals("디자인 레퍼런스", cursor.getString(1))
                assertEquals("레거시 요약", cursor.getString(2))
                assertEquals("본문", cursor.getString(3))
                assertEquals("/images/legacy-design", cursor.getString(4))
                assertEquals("/thumbs/legacy-design", cursor.getString(5))
                assertEquals(1, cursor.getInt(6))
                assertEquals(1000L, cursor.getLong(7))
                assertEquals(2000L, cursor.getLong(8))
            }

            db.query(
                """
                SELECT primaryContentType, sourceImageUri, isFavorite, createdAtMillis
                FROM screenshot_cards WHERE imageId = ?
                """.trimIndent(),
                arrayOf<Any?>("already-other"),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("OTHER", cursor.getString(0))
                assertEquals("content://other", cursor.getString(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(3000L, cursor.getLong(3))
            }

            db.query(
                "SELECT COUNT(*) FROM screenshot_key_fields WHERE imageId = ?",
                arrayOf<Any?>("legacy-design"),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
        } finally {
            db.close()
            openHelper.close()
        }
    }
}
