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
class RecapDatabaseMigration2To3Test {
    private lateinit var context: Context
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        dbFile = context.getDatabasePath("migration-2-3-test.db")
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
    fun migration2To3_preservesExistingCardAndAddsEmptyBody() {
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.absolutePath)
                .callback(
                    object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            MIGRATION_1_2.migrate(db)
                            db.execSQL(
                                """
                                INSERT INTO screenshot_cards (
                                    imageId, sourceImageUri, storedImagePath, thumbnailPath,
                                    title, summary, primaryContentType, confidence,
                                    isFavorite, createdAtMillis, updatedAtMillis
                                ) VALUES (
                                    'legacy-card', NULL, '/images/legacy-card', NULL,
                                    '레거시 제목', '레거시 요약', 'SHOPPING_PRODUCT', 'HIGH',
                                    1, 1000, 2000
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO screenshot_key_fields (
                                    imageId, label, value, displayPriority, isSensitive
                                ) VALUES ('legacy-card', 'label', 'value', 1, 0)
                                """.trimIndent(),
                            )
                            MIGRATION_2_3.migrate(db)
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
                "SELECT title, summary, body, storedImagePath, isFavorite FROM screenshot_cards WHERE imageId = ?",
                arrayOf<Any?>("legacy-card"),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("레거시 제목", cursor.getString(0))
                assertEquals("레거시 요약", cursor.getString(1))
                assertEquals("", cursor.getString(2))
                assertEquals("/images/legacy-card", cursor.getString(3))
                assertEquals(1, cursor.getInt(4))
            }

            db.query(
                "SELECT COUNT(*) FROM screenshot_key_fields WHERE imageId = ?",
                arrayOf<Any?>("legacy-card"),
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
