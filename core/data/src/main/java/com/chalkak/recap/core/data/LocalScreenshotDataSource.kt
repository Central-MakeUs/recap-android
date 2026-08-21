package com.chalkak.recap.core.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRepository
import com.chalkak.recap.core.data.screenshot.permission.currentImageAccessLevel
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.LocalImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequest as platformImagePermissionRequest

@Singleton
class LocalScreenshotDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ImagePermissionRepository {
    override fun imagePermissionRequest(): Array<String> = platformImagePermissionRequest(
        accessLevel = context.currentImageAccessLevel(),
    )

    override fun currentImageAccessLevel(): ImageAccessLevel = context.currentImageAccessLevel()

    suspend fun queryRecentScreenshots(limit: Int): List<LocalImage> = withContext(Dispatchers.IO) {
        queryScreenshotImages(limit = limit)
    }

    suspend fun queryAllScreenshots(): List<LocalImage> = withContext(Dispatchers.IO) {
        queryScreenshotImages(limit = null)
    }

    private fun queryScreenshotImages(limit: Int?): List<LocalImage> {
        if (currentImageAccessLevel() == ImageAccessLevel.Denied) {
            return emptyList()
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
        )
        // qa/release demo: load every MediaStore image so catalog files outside Screenshots/ appear.
        val queryArgs = screenshotImageQueryArgs(
            limit = limit,
            restrictToScreenshotFolders = BuildConfig.DEBUG,
        )

        return runCatching {
            context.contentResolver.query(collection, projection, queryArgs, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        add(
                            LocalImage(
                                uri = ContentUris.withAppendedId(collection, id).toString(),
                                displayName = cursor.getString(displayNameColumn).orEmpty()
                                    .ifBlank { "screenshot-$id" },
                                dateAddedMillis = cursor.getLong(dateAddedColumn) * MILLIS_PER_SECOND,
                            ),
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}

internal val screenshotRelativePaths = listOf(
    "DCIM/Screenshots/",
    "Pictures/Screenshots/",
)

internal fun screenshotImageQueryArgs(
    limit: Int?,
    restrictToScreenshotFolders: Boolean,
): Bundle = Bundle().apply {
    if (restrictToScreenshotFolders) {
        val pathSelection = screenshotRelativePaths.joinToString(separator = " OR ") {
            "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        }
        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "($pathSelection)")
        putStringArray(
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
            screenshotRelativePaths.toTypedArray(),
        )
    }
    putStringArray(
        ContentResolver.QUERY_ARG_SORT_COLUMNS,
        arrayOf(MediaStore.Images.Media.DATE_ADDED),
    )
    putInt(
        ContentResolver.QUERY_ARG_SORT_DIRECTION,
        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
    )
    if (limit != null) {
        putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
    }
}
