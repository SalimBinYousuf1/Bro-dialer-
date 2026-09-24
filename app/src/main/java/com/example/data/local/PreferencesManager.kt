package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ContactSortOrder
import com.example.data.model.DialerSettings
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val DIALPAD_TONES = booleanPreferencesKey("dialpad_tones")
        val VIBRATE_ON_CONNECT = booleanPreferencesKey("vibrate_on_connect")
        val VIBRATE_ON_DISCONNECT = booleanPreferencesKey("vibrate_on_disconnect")
        val CALL_CONFIRMATION = booleanPreferencesKey("call_confirmation")
        val AUTO_SPEAKER = booleanPreferencesKey("auto_speaker")
        val CONTACT_SORT_ORDER = stringPreferencesKey("contact_sort_order")
        val SHOW_CONTACT_PHOTOS = booleanPreferencesKey("show_contact_photos")
        val T9_SEARCH_ENABLED = booleanPreferencesKey("t9_search_enabled")
        val GROUP_CALLS_BY_DATE = booleanPreferencesKey("group_calls_by_date")
        val CONFIRM_DELETE_CALL_LOG = booleanPreferencesKey("confirm_delete_call_log")
        val VOICEMAIL_NUMBER = stringPreferencesKey("voicemail_number")
        val BLOCK_UNKNOWN_NUMBERS = booleanPreferencesKey("block_unknown_numbers")
        val DEFAULT_START_TAB = stringPreferencesKey("default_start_tab")
        val CALL_BACKGROUND_URI = stringPreferencesKey("call_background_uri")
    }

    val settingsFlow: Flow<DialerSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.LIGHT.name
        val sortOrderStr = prefs[Keys.CONTACT_SORT_ORDER] ?: ContactSortOrder.FIRST_NAME.name
        DialerSettings(
            themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.LIGHT },
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            dialpadTones = prefs[Keys.DIALPAD_TONES] ?: true,
            vibrateOnConnect = prefs[Keys.VIBRATE_ON_CONNECT] ?: true,
            vibrateOnDisconnect = prefs[Keys.VIBRATE_ON_DISCONNECT] ?: true,
            callConfirmation = prefs[Keys.CALL_CONFIRMATION] ?: false,
            autoSpeaker = prefs[Keys.AUTO_SPEAKER] ?: false,
            contactSortOrder = try { ContactSortOrder.valueOf(sortOrderStr) } catch (_: Exception) { ContactSortOrder.FIRST_NAME },
            showContactPhotos = prefs[Keys.SHOW_CONTACT_PHOTOS] ?: true,
            t9SearchEnabled = prefs[Keys.T9_SEARCH_ENABLED] ?: true,
            groupCallsByDate = prefs[Keys.GROUP_CALLS_BY_DATE] ?: true,
            confirmDeleteCallLog = prefs[Keys.CONFIRM_DELETE_CALL_LOG] ?: true,
            voicemailNumber = prefs[Keys.VOICEMAIL_NUMBER] ?: "",
            blockUnknownNumbers = prefs[Keys.BLOCK_UNKNOWN_NUMBERS] ?: false,
            defaultStartTab = prefs[Keys.DEFAULT_START_TAB] ?: "home",
            callBackgroundUri = prefs[Keys.CALL_BACKGROUND_URI]
        )
    }

    suspend fun updateSettings(transform: (DialerSettings) -> DialerSettings) {
        val current = settingsFlow.first()
        val updated = transform(current)
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedback
            prefs[Keys.DIALPAD_TONES] = updated.dialpadTones
            prefs[Keys.VIBRATE_ON_CONNECT] = updated.vibrateOnConnect
            prefs[Keys.VIBRATE_ON_DISCONNECT] = updated.vibrateOnDisconnect
            prefs[Keys.CALL_CONFIRMATION] = updated.callConfirmation
            prefs[Keys.AUTO_SPEAKER] = updated.autoSpeaker
            prefs[Keys.CONTACT_SORT_ORDER] = updated.contactSortOrder.name
            prefs[Keys.SHOW_CONTACT_PHOTOS] = updated.showContactPhotos
            prefs[Keys.T9_SEARCH_ENABLED] = updated.t9SearchEnabled
            prefs[Keys.GROUP_CALLS_BY_DATE] = updated.groupCallsByDate
            prefs[Keys.CONFIRM_DELETE_CALL_LOG] = updated.confirmDeleteCallLog
            prefs[Keys.VOICEMAIL_NUMBER] = updated.voicemailNumber
            prefs[Keys.BLOCK_UNKNOWN_NUMBERS] = updated.blockUnknownNumbers
            prefs[Keys.DEFAULT_START_TAB] = updated.defaultStartTab
            if (updated.callBackgroundUri != null) {
                prefs[Keys.CALL_BACKGROUND_URI] = updated.callBackgroundUri
            } else {
                prefs.remove(Keys.CALL_BACKGROUND_URI)
            }
        }
    }

    suspend fun updateDefaultStartTab(tab: String) {
        context.dataStore.edit { it[Keys.DEFAULT_START_TAB] = tab }
    }

    suspend fun updateCallBackgroundUri(uri: String?) {
        context.dataStore.edit {
            if (uri != null) {
                it[Keys.CALL_BACKGROUND_URI] = uri
            } else {
                it.remove(Keys.CALL_BACKGROUND_URI)
            }
        }
    }
}
