package com.chalkak.recap.core.data.entity

import com.google.android.gms.tasks.Task
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class EntityTextExtractor @Inject constructor() {
    suspend fun extract(
        text: String,
        modelIdentifier: String = EntityExtractorOptions.KOREAN,
        entityTypesFilter: Set<Int>? = null,
        preferredLocale: Locale? = Locale.KOREAN,
        referenceTimeMillis: Long? = null,
        referenceTimeZone: TimeZone? = null,
    ): List<EntityAnnotation> {
        if (text.isBlank()) return emptyList()

        val extractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(modelIdentifier).build(),
        )
        return try {
            extractor.downloadModelIfNeeded().await()
            extractor.annotate(
                EntityExtractionParams.Builder(text)
                    .apply {
                        entityTypesFilter?.let(::setEntityTypesFilter)
                        preferredLocale?.let(::setPreferredLocale)
                        referenceTimeMillis?.let(::setReferenceTime)
                        referenceTimeZone?.let(::setReferenceTimeZone)
                    }
                    .build(),
            ).await()
        } finally {
            extractor.close()
        }
    }
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
