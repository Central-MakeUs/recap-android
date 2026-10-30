package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import com.chalkak.recap.core.model.user.Usage
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getAccountInfo(): Result<AccountInfo>

    fun observeDataSummary(): Flow<Result<DataSummary>>

    suspend fun prefetchDataSummary(): Result<DataSummary>

    fun refreshDataSummary()

    suspend fun getDataSummary(): Result<DataSummary>

    suspend fun getUsage(): Result<Usage>

    fun observeConsentStatus(): Flow<Result<ConsentStatus>>

    suspend fun prefetchConsentStatus(): Result<ConsentStatus>

    fun refreshConsentStatus()

    suspend fun getConsentStatus(): Result<ConsentStatus>

    suspend fun giveConsent(): Result<Unit>

    suspend fun withdrawConsent(): Result<Unit>

    suspend fun withdraw(): Result<Unit>

    suspend fun deleteAccountData(): Result<Unit>
}
