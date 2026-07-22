package com.chalkak.recap.core.data.network

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException

private val remoteErrorJson =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

inline fun <T, R> mapApiResponse(
    response: ApiResponseDto<T>,
    transform: (T) -> R,
): Result<R> {
    val data = response.data
    return when {
        response.success && data != null -> Result.success(transform(data))
        response.error != null ->
            Result.failure(
                RemoteApiException(
                    code = response.error.code,
                    message = response.error.message,
                ),
            )
        else -> Result.failure(RemoteApiException(code = "UNKNOWN", message = "Unknown API error"))
    }
}

fun mapHttpException(error: HttpException): RemoteApiException {
    val rawBody =
        try {
            error.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }

    if (!rawBody.isNullOrBlank()) {
        try {
            val envelope = remoteErrorJson.decodeFromString<ApiResponseDto<JsonElement?>>(rawBody)
            val apiError = envelope.error
            if (apiError != null) {
                return RemoteApiException(
                    code = apiError.code,
                    message = apiError.message,
                    cause = error,
                )
            }
        } catch (_: Exception) {
            // Fall through to HTTP status-based error.
        }
    }

    return RemoteApiException(
        code = "HTTP_${error.code()}",
        message = error.message().orEmpty().ifBlank { "HTTP ${error.code()}" },
        cause = error,
    )
}

inline fun <R> runRemoteCatching(block: () -> R): Result<R> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: RemoteApiException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(mapHttpException(error))
    } catch (_: IOException) {
        Result.failure(RemoteNetworkException())
    } catch (error: Throwable) {
        Result.failure(RemoteApiException(code = "UNKNOWN", message = "Unknown API error", cause = error))
    }

suspend inline fun <R> runRemoteCatchingSuspend(block: suspend () -> R): Result<R> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: RemoteApiException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(mapHttpException(error))
    } catch (_: IOException) {
        Result.failure(RemoteNetworkException())
    } catch (error: Throwable) {
        Result.failure(RemoteApiException(code = "UNKNOWN", message = "Unknown API error", cause = error))
    }
