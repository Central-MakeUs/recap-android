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
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.R
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DemoViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private var ocrJob: Job? = null

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

            is DemoAction.RunOcr -> runOcr(action.engine)
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

    private fun runOcr(engine: OcrEngine) {
        val screenshotUris = _uiState.value.recentScreenshotUris.take(RecentScreenshotLimit)
        if (screenshotUris.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    ocrState = OcrUiState(
                        engine = engine,
                        errorMessage = appContext.getString(R.string.demo_ocr_error_no_screenshots),
                    ),
                )
            }
            return
        }

        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    ocrState = OcrUiState(
                        engine = engine,
                        isRunning = true,
                        totalCount = screenshotUris.size,
                    ),
                )
            }

            val recognizer = engine.createTextRecognizer()
            val results = mutableListOf<OcrImageResult>()
            try {
                screenshotUris.forEachIndexed { index, uri ->
                    if (!isActive) return@launch

                    val result = withContext(Dispatchers.IO) {
                        appContext.recognizeScreenshotText(
                            recognizer = recognizer,
                            uri = uri,
                            imageIndex = index + 1,
                        )
                    }
                    results += result
                    _uiState.update { current ->
                        current.copy(
                            ocrState = current.ocrState.copy(
                                completedCount = results.size,
                                results = results.toList(),
                            ),
                        )
                    }
                }
                _uiState.update { current ->
                    current.copy(
                        ocrState = current.ocrState.copy(
                            isRunning = false,
                            completedCount = screenshotUris.size,
                        ),
                    )
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) {
                    throw throwable
                }
                _uiState.update { current ->
                    current.copy(
                        ocrState = current.ocrState.copy(
                            isRunning = false,
                            errorMessage = appContext.getString(R.string.demo_ocr_error_processing),
                        ),
                    )
                }
            } finally {
                recognizer.close()
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

private fun OcrEngine.createTextRecognizer(): TextRecognizer {
    return when (this) {
        OcrEngine.Latin -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        OcrEngine.Korean -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }
}

@WorkerThread
private suspend fun Context.recognizeScreenshotText(
    recognizer: TextRecognizer,
    uri: Uri,
    imageIndex: Int,
): OcrImageResult {
    val image = InputImage.fromFilePath(this, uri)
    val text = recognizer.process(image).await()
    return text.toOcrImageResult(
        imageIndex = imageIndex,
        imageUri = uri.toString(),
    )
}

private fun Text.toOcrImageResult(
    imageIndex: Int,
    imageUri: String,
): OcrImageResult {
    return OcrImageResult(
        imageIndex = imageIndex,
        imageUri = imageUri,
        text = text,
        blocks = textBlocks.map { block ->
            OcrTextBlock(
                text = block.text,
                lines = block.lines.map { line -> line.text },
            )
        },
    )
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { throwable ->
            continuation.resumeWithException(throwable)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}

private val screenshotRelativePaths = listOf(
    "DCIM/Screenshots/",
)

private const val RecentScreenshotLimit = 5
