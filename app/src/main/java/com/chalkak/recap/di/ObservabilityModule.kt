package com.chalkak.recap.di

import com.chalkak.recap.app.observability.FirebaseCrashReporter
import com.chalkak.recap.app.observability.FirebasePerformanceTracer
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.PerformanceTracer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ObservabilityModule {
    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: FirebaseCrashReporter): CrashReporter

    @Binds
    @Singleton
    abstract fun bindPerformanceTracer(impl: FirebasePerformanceTracer): PerformanceTracer
}
