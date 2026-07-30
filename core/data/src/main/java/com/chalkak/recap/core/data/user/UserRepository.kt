package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary

interface UserRepository {
    suspend fun getAccountInfo(): Result<AccountInfo>

    suspend fun getDataSummary(): Result<DataSummary>

    suspend fun getConsentStatus(): Result<ConsentStatus>

    suspend fun giveConsent(): Result<Unit>

    suspend fun withdrawConsent(): Result<Unit>

    suspend fun withdraw(): Result<Unit>

    suspend fun deleteAccountData(): Result<Unit>
}
