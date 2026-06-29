package com.chalkak.recap.core.data

import android.content.Context
import androidx.room.Room
import com.chalkak.recap.core.data.ocr.OcrDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRecapDatabase(
        @ApplicationContext context: Context,
    ): RecapDatabase {
        return Room.databaseBuilder(
            context,
            RecapDatabase::class.java,
            "recap.db",
        ).addMigrations(RecapDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideOcrDao(database: RecapDatabase): OcrDao = database.ocrDao()
}
