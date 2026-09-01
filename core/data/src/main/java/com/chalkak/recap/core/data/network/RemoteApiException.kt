package com.chalkak.recap.core.data.network

import retrofit2.HttpException

class RemoteApiException(
    val code: String,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class RemoteNetworkException(
    cause: Throwable? = null,
) : Exception("Network request failed", cause)

fun Throwable.isUsageLimitExceeded(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        when (current) {
            is HttpException -> if (current.code() == HTTP_TOO_MANY_REQUESTS) return true
            is RemoteApiException -> {
                if (current.code.equals(HTTP_429_CODE, ignoreCase = true)) return true
                if (current.message.contains(USAGE_LIMIT_EXCEEDED_MESSAGE_HINT)) return true
            }
        }
        current = current.cause
    }
    return false
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_429_CODE = "HTTP_429"
private const val USAGE_LIMIT_EXCEEDED_MESSAGE_HINT = "사용량을 초과"
