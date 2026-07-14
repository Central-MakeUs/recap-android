package com.chalkak.recap.di

import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.data.auth.KakaoNativeAppKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object KakaoAuthModule {
    @Provides
    @KakaoNativeAppKey
    fun provideKakaoNativeAppKey(): String = BuildConfig.KAKAO_NATIVE_APP_KEY
}
