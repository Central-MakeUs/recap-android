package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.SocialAuthCredential

interface SocialLoginClient {
    val provider: AuthProvider

    suspend fun login(context: Context): Result<SocialAuthCredential>
}
