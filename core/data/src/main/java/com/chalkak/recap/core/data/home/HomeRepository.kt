package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.model.home.HomeSummary
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeSummary(): Flow<HomeSummary>
}
