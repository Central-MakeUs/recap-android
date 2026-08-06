package com.chalkak.recap.core.data.account

import androidx.annotation.VisibleForTesting
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로컬 계정 소유자 키용 해시.
 *
 * 카카오 user.id는 자릿수가 짧아 salt 없는 SHA-256은 전수 조사로 역산된다.
 * 기기 로컬 salt를 섞어 저장 값만으로는 원문 ID를 복원할 수 없게 한다.
 */
@Singleton
class AccountOwnerHasher @Inject constructor(
    private val accountOwnerStore: AccountOwnerStore,
) {
    suspend fun hashKakaoUserId(userId: Long): String =
        hash(userId = userId, salt = accountOwnerStore.getOrCreateSalt())

    companion object {
        @VisibleForTesting
        internal fun hash(userId: Long, salt: String): String =
            MessageDigest
                .getInstance("SHA-256")
                .digest("$salt|kakao:$userId".toByteArray(Charsets.UTF_8))
                .toOwnerHex()
    }
}

internal fun ByteArray.toOwnerHex(): String =
    joinToString(separator = "") { byte -> "%02x".format(byte) }
