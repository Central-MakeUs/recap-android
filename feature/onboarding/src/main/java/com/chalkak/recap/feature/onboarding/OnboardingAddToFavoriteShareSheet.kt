package com.chalkak.recap.feature.onboarding

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import com.chalkak.recap.core.design.R as DesignR

private const val ShareFileName = "onboarding_add_to_favorite_share.png"
private const val ShareCacheDirectoryName = "share"

fun Context.launchOnboardingAddToFavoriteShareSheet(
    @DrawableRes imageResId: Int = R.drawable.onboarding_add_to_favorite_share,
) {
    val shareFile = prepareShareImageFile(imageResId) ?: return

    val contentUri = FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.fileprovider",
        shareFile,
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(
        Intent.createChooser(
            sendIntent,
            getString(DesignR.string.onboarding_first_organize_select_button),
        ),
    )
}

private fun Context.prepareShareImageFile(@DrawableRes imageResId: Int): File? {
    val shareDirectory = File(cacheDir, ShareCacheDirectoryName).apply { mkdirs() }
    val shareFile = File(shareDirectory, ShareFileName)
    if (shareFile.exists() && shareFile.length() > 0L) {
        return shareFile
    }

    val resourceUri = ("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
            "${resources.getResourcePackageName(imageResId)}/" +
            "${resources.getResourceTypeName(imageResId)}/" +
            resources.getResourceEntryName(imageResId)).toUri()

    return runCatching {
        contentResolver.openInputStream(resourceUri)?.use { input ->
            shareFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        shareFile.takeIf { it.exists() && it.length() > 0L }
    }.getOrNull()
}
