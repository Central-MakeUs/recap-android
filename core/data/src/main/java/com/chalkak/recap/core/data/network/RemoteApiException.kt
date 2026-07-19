package com.chalkak.recap.core.data.network

class RemoteApiException(
    val code: String,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class RemoteNetworkException(
    cause: Throwable? = null,
) : Exception("Network request failed", cause)
