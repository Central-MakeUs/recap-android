package com.chalkak.recap.core.data.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.safeData
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class AccountOwnerStore @Inject constructor(
    @param:AccountOwnerDataStore private val dataStore: DataStore<Preferences>,
) {
    suspend fun getHash(): String? = read(OWNER_HASH)

    suspend fun setHash(hash: String) {
        require(hash.isNotBlank()) { "owner hash must not be blank" }
        dataStore.edit { preferences ->
            preferences[OWNER_HASH] = hash
        }
    }

    /**
     * 소유자 해시용 기기 로컬 salt. 없으면 한 번만 생성해 저장한다.
     * salt가 사라지면 이전 해시와 절대 일치하지 않아 다음 로그인에서 wipe가 일어난다(안전 우선).
     */
    suspend fun getOrCreateSalt(): String {
        read(OWNER_SALT)?.let { return it }

        var salt = ""
        dataStore.edit { preferences ->
            salt = preferences[OWNER_SALT]?.takeIf { it.isNotBlank() }
                ?: generateSalt().also { generated -> preferences[OWNER_SALT] = generated }
        }
        return salt
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(OWNER_HASH)
            preferences.remove(OWNER_SALT)
        }
    }

    private suspend fun read(key: Preferences.Key<String>): String? =
        dataStore
            .safeData(ACCOUNT_OWNER_DATASTORE_NAME)
            .first()[key]
            ?.takeIf { it.isNotBlank() }

    private companion object {
        val OWNER_HASH = stringPreferencesKey("owner_hash")
        val OWNER_SALT = stringPreferencesKey("owner_salt")
        const val SALT_BYTES = 32

        fun generateSalt(): String =
            ByteArray(SALT_BYTES)
                .also { bytes -> SecureRandom().nextBytes(bytes) }
                .toOwnerHex()
    }
}
