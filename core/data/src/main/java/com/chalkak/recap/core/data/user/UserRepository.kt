package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.data.user.remote.UserApi
import com.chalkak.recap.core.data.user.remote.toDomain
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.DataSummary
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val sessionTokenStore: SessionTokenStore,
) {
    suspend fun getAccountInfo(): Result<AccountInfo> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getAccountInfo()) { it.toDomain() }.getOrThrow()
        }

    suspend fun getDataSummary(): Result<DataSummary> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getDataSummary()) { it.toDomain() }.getOrThrow()
        }

    suspend fun withdraw(): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            userApi.withdraw()
        }
        // 서버 실패여도 로컬 세션은 비워 재로그인 가능하게 한다.
        sessionTokenStore.clear()
        return result
    }

    suspend fun deleteAccountData(): Result<Unit> =
        runRemoteCatchingSuspend {
            userApi.deleteAccountData()
        }
}
