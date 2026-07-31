package com.chalkak.recap.core.model.observability

interface CrashReporter {
    fun setCustomKey(key: String, value: String)

    fun setCustomKey(key: String, value: Boolean)

    fun setCustomKey(key: String, value: Int)

    fun log(message: String)

    fun recordException(throwable: Throwable)

    companion object {
        val NoOp: CrashReporter = object : CrashReporter {
            override fun setCustomKey(key: String, value: String) = Unit

            override fun setCustomKey(key: String, value: Boolean) = Unit

            override fun setCustomKey(key: String, value: Int) = Unit

            override fun log(message: String) = Unit

            override fun recordException(throwable: Throwable) = Unit
        }
    }
}
