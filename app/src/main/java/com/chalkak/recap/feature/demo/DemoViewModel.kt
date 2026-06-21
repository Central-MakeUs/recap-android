package com.chalkak.recap.feature.demo

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemoViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(
        DemoUiState(
            imagePermissionLevel = appContext.currentImagePermissionLevel(),
        ),
    )
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        loadRecentScreenshots(_uiState.value.imagePermissionLevel)
    }

    fun imagePermissionRequest(): Array<String> {
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

    fun onAction(action: DemoAction) {
        when (action) {
            DemoAction.RequestImagePermission,
            DemoAction.RefreshImagePermission,
            -> refreshImagePermissionLevel()
        }
    }

    fun refreshImagePermissionLevel() {
        val permissionLevel = appContext.currentImagePermissionLevel()
        _uiState.update { current ->
            current.copy(
                imagePermissionLevel = permissionLevel,
            )
        }
        loadRecentScreenshots(permissionLevel)
    }

    private fun loadRecentScreenshots(permissionLevel: ImagePermissionLevel) {
        if (permissionLevel == ImagePermissionLevel.Denied) {
            _uiState.update { current ->
                current.copy(recentScreenshotUris = emptyList())
            }
            return
        }

        viewModelScope.launch {
            val screenshotUris = withContext(Dispatchers.IO) {
                appContext.queryRecentScreenshotUris()
            }
            _uiState.update { current ->
                current.copy(recentScreenshotUris = screenshotUris)
            }
        }
    }
}

private fun Context.currentImagePermissionLevel(): ImagePermissionLevel {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> ImagePermissionLevel.Full

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> ImagePermissionLevel.Selected

        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> ImagePermissionLevel.Full

        else -> ImagePermissionLevel.Denied
    }
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun Context.queryRecentScreenshotUris(): List<Uri> {
    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = screenshotRelativePaths.joinToString(separator = " OR ") {
        "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
    }
    val selectionArgs = screenshotRelativePaths.toTypedArray()
    val queryArgs = Bundle().apply {
        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
        putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        putStringArray(
            ContentResolver.QUERY_ARG_SORT_COLUMNS,
            arrayOf(MediaStore.Images.Media.DATE_ADDED),
        )
        putInt(
            ContentResolver.QUERY_ARG_SORT_DIRECTION,
            ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
        )
        putInt(ContentResolver.QUERY_ARG_LIMIT, RecentScreenshotLimit)
    }

    return runCatching {
        contentResolver.query(collection, projection, queryArgs, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            buildList {
                while (cursor.moveToNext()) {
                    add(ContentUris.withAppendedId(collection, cursor.getLong(idColumn)))
                }
            }
        }.orEmpty()
    }.getOrDefault(emptyList())
}

private val screenshotRelativePaths = listOf(
    "DCIM/Screenshots/",
)

private const val RecentScreenshotLimit = 5
