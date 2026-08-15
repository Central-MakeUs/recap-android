package com.chalkak.recap

import android.app.Application
import com.chalkak.recap.app.notification.OrganizeNotificationCoordinator
import com.chalkak.recap.app.observability.FirebaseCollectionGate
import com.chalkak.recap.app.observability.ObservabilityBootstrap
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RecapApplication : Application() {
    @Inject
    lateinit var organizeNotificationCoordinator: OrganizeNotificationCoordinator

    @Inject
    lateinit var observabilityBootstrap: ObservabilityBootstrap

    override fun onCreate() {
        super.onCreate()

        FirebaseCollectionGate.apply()

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        observabilityBootstrap.start()
        organizeNotificationCoordinator.start()
    }
}
