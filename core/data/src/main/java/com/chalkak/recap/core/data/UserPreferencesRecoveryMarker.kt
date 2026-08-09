package com.chalkak.recap.core.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf

/**
 * Owns the Preferences corruption recovery marker key and replacement payload.
 * Handler and startup recovery coordinator must use this instead of declaring the key twice.
 */
internal object UserPreferencesRecoveryMarker {
    val KEY = booleanPreferencesKey("user_preferences_recovery_required")

    fun createReplacementPreferences(): Preferences = preferencesOf(KEY to true)

    fun isRequired(preferences: Preferences): Boolean = preferences[KEY] == true
}
