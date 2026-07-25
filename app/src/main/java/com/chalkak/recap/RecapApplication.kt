package com.chalkak.recap

import android.app.Application
import com.chalkak.recap.app.notification.OrganizeNotificationCoordinator
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class RecapApplication : Application() {
    @Inject
    lateinit var organizeNotificationCoordinator: OrganizeNotificationCoordinator

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        organizeNotificationCoordinator.start()
    }
}
