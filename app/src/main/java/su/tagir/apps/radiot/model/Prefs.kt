package su.tagir.apps.radiot.model

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


class Prefs(context: Context) : AuthHolder {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override var token: String?
        get() = sharedPreferences.getString(KEY_GITTER_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_GITTER_TOKEN, value).apply()


    override var tokenType: String?
        get() = sharedPreferences.getString(KEY_GITTER_TOKEN_TYPE, null)
        set(value) = sharedPreferences.edit().putString(KEY_GITTER_TOKEN_TYPE, value).apply()


    override fun clear() {
        sharedPreferences.edit()
                .remove(KEY_GITTER_TOKEN)
                .remove(KEY_GITTER_TOKEN_TYPE)
                .apply()
    }

    var showTimer: Boolean
        get() = sharedPreferences.getBoolean("show_timer", true)
        set(value) = sharedPreferences.edit().putBoolean("show_timer", value).apply()

    companion object {
        private const val KEY_GITTER_TOKEN = "gitter_token"
        private const val KEY_GITTER_TOKEN_TYPE = "gitter_token_type"
    }
}