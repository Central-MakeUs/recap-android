package com.chalkak.recap.core.data.account

import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class AccountOwnerHasherTest {
    @Test
    fun `same kakao user id and salt produce same hash`() {
        val first = AccountOwnerHasher.hash(userId = 4991360438L, salt = SALT)
        val second = AccountOwnerHasher.hash(userId = 4991360438L, salt = SALT)

        assertEquals(first, second)
        assertEquals(64, first.length)
    }

    @Test
    fun `different kakao user ids produce different hashes`() {
        val first = AccountOwnerHasher.hash(userId = 1L, salt = SALT)
        val second = AccountOwnerHasher.hash(userId = 2L, salt = SALT)

        assertNotEquals(first, second)
    }

    @Test
    fun `different salts produce different hashes for the same user id`() {
        val first = AccountOwnerHasher.hash(userId = 4991360438L, salt = SALT)
        val second = AccountOwnerHasher.hash(userId = 4991360438L, salt = "other-salt")

        assertNotEquals(first, second)
    }

    @Test
    fun `hash does not contain raw user id`() {
        val userId = 4991360438L

        val hash = AccountOwnerHasher.hash(userId = userId, salt = SALT)

        assertFalse(hash.contains(userId.toString()))
        assertFalse(hash.contains("kakao:"))
    }

    @Test
    fun `hashKakaoUserId reuses the stored salt across calls`() = runTest {
        val hasher = AccountOwnerHasher(AccountOwnerStore(InMemoryPreferencesDataStore()))

        val first = hasher.hashKakaoUserId(4991360438L)
        val second = hasher.hashKakaoUserId(4991360438L)

        assertEquals(first, second)
    }

    @Test
    fun `hashKakaoUserId differs per device salt`() = runTest {
        val first = AccountOwnerHasher(AccountOwnerStore(InMemoryPreferencesDataStore()))
        val second = AccountOwnerHasher(AccountOwnerStore(InMemoryPreferencesDataStore()))

        assertNotEquals(
            first.hashKakaoUserId(4991360438L),
            second.hashKakaoUserId(4991360438L),
        )
    }

    private companion object {
        const val SALT = "0123456789abcdef"
    }
}
