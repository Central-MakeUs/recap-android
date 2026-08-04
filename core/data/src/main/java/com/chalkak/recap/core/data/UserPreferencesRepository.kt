package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.model.user.ConsentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    val onboardingCompleted: Flow<Boolean> =
        dataStore.safeData().map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val organizeCompleteNotificationEnabled: Flow<Boolean> =
        dataStore.safeData().map { preferences ->
            preferences[ORGANIZE_COMPLETE_NOTIFICATION_ENABLED]
                ?: preferences[LEGACY_ORGANIZE_COMPLETE_ENABLED]
                ?: false
        }

    suspend fun setOrganizeCompleteNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ORGANIZE_COMPLETE_NOTIFICATION_ENABLED] = enabled
            preferences.remove(LEGACY_ORGANIZE_COMPLETE_ENABLED)
        }
    }

    suspend fun getAiDataTransferConsentStatus(): ConsentStatus {
        val preferences = dataStore.safeData().first()
        return ConsentStatus(
            consented = preferences[AI_DATA_TRANSFER_CONSENTED] ?: false,
            consentedAt = preferences[AI_DATA_TRANSFER_CONSENTED_AT],
        )
    }

    suspend fun setAiDataTransferConsent(consented: Boolean, consentedAt: String?) {
        dataStore.edit { preferences ->
            preferences[AI_DATA_TRANSFER_CONSENTED] = consented
            if (consentedAt == null) {
                preferences.remove(AI_DATA_TRANSFER_CONSENTED_AT)
            } else {
                preferences[AI_DATA_TRANSFER_CONSENTED_AT] = consentedAt
            }
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ORGANIZE_COMPLETE_NOTIFICATION_ENABLED =
            booleanPreferencesKey("organize_complete_notification_enabled")
        val LEGACY_ORGANIZE_COMPLETE_ENABLED =
            booleanPreferencesKey("organize_complete_enabled")
        val AI_DATA_TRANSFER_CONSENTED =
            booleanPreferencesKey("ai_data_transfer_consented")
        val AI_DATA_TRANSFER_CONSENTED_AT =
            stringPreferencesKey("ai_data_transfer_consented_at")
    }
}
