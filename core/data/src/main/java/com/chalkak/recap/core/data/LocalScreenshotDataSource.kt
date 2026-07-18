package com.chalkak.recap.core.data

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.chalkak.recap.core.data.screenshot.ImagePermissionRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.LocalImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalScreenshotDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ImagePermissionRepository {
    override fun imagePermissionRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
            )

            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }
    }

    override fun currentImageAccessLevel(): ImageAccessLevel {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> ImageAccessLevel.Full

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    context.hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> ImageAccessLevel.Selected

            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                    context.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> ImageAccessLevel.Full

            else -> ImageAccessLevel.Denied
        }
    }

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
        val pathSelection = screenshotRelativePaths.joinToString(separator = " OR ") {
            "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        }
        val queryArgs = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "($pathSelection)")
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                screenshotRelativePaths.toTypedArray(),
            )
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

    private fun Context.hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L

        val screenshotRelativePaths = listOf(
            "DCIM/Screenshots/",
            "Pictures/Screenshots/"
        )
    }
}
