package com.chalkak.recap.core.data

import android.content.ContentResolver
import android.provider.MediaStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ScreenshotImageQueryArgsTest {
    @Test
    fun `debug query restricts to screenshot folders`() {
        val args = screenshotImageQueryArgs(limit = 20, restrictToScreenshotFolders = true)

        assertEquals(
            "(${MediaStore.Images.Media.RELATIVE_PATH} = ? OR ${MediaStore.Images.Media.RELATIVE_PATH} = ?)",
            args.getString(ContentResolver.QUERY_ARG_SQL_SELECTION),
        )
        assertArrayEquals(
            screenshotRelativePaths.toTypedArray(),
            args.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS),
        )
        assertEquals(20, args.getInt(ContentResolver.QUERY_ARG_LIMIT))
    }

    @Test
    fun `demo query loads all images without folder filter`() {
        val args = screenshotImageQueryArgs(limit = null, restrictToScreenshotFolders = false)

        assertNull(args.getString(ContentResolver.QUERY_ARG_SQL_SELECTION))
        assertNull(args.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS))
        assertEquals(0, args.getInt(ContentResolver.QUERY_ARG_LIMIT, 0))
        assertArrayEquals(
            arrayOf(MediaStore.Images.Media.DATE_ADDED),
            args.getStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS),
        )
    }
}
