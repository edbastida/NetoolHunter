package com.netoolhunter.app.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreKeys {
    const val NAME = "netoolhunter_prefs"

    val ENABLED_REPOS      = stringSetPreferencesKey("enabled_repos")
    val CUSTOM_REPOS_JSON  = stringPreferencesKey("custom_repos_json")
    val PREREQS_COMPLETED  = booleanPreferencesKey("prereqs_completed")
}
