package com.chalkak.recap

import android.app.Application
import com.chalkak.recap.app.notification.OrganizeNotificationCoordinator
import com.chalkak.recap.app.observability.CrashlyticsTimberTree
import com.chalkak.recap.app.observability.FirebaseCollectionGate
import com.chalkak.recap.app.observability.ObservabilityBootstrap
import com.chalkak.recap.core.model.observability.CrashReporter
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class RecapApplication : Application() {
    @Inject
    lateinit var organizeNotificationCoordinator: OrganizeNotificationCoordinator

    @Inject
    lateinit var crashReporter: CrashReporter

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
        } else {
            Timber.plant(CrashlyticsTimberTree(crashReporter))
        }

        observabilityBootstrap.start()
        organizeNotificationCoordinator.start()
    }
}
