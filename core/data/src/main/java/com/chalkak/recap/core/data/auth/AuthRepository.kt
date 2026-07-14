package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.model.auth.AuthSignInResult
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val kakaoLoginClient: KakaoLoginClient,
) {
    suspend fun signInWithKakao(context: Context): Result<AuthSignInResult> =
        kakaoLoginClient.login(context).map {
            AuthSignInResult.PendingServerIntegration
        }
}
