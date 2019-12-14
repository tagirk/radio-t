package su.tagir.apps.radiot.model.api.auth

import android.content.SharedPreferences
import su.tagir.apps.radiot.extensions.modify
import su.tagir.apps.radiot.model.api.SessionListener
import su.tagir.apps.radiot.schedulers.AppExecutors


class GitterAuthHolder(private val prefs: SharedPreferences) : AuthHolder {

    override var authHeader: String = "Authorization"

    private var sessionListener: SessionListener? = null

    override var accessToken: String?
        get() = prefs.getString(KEY_GITTER_ACCESS_TOKEN, null)
        set(value) = prefs.modify(true) { putString(KEY_GITTER_ACCESS_TOKEN, value) }

    override var tokenType: String?
        get() = prefs.getString(KEY_GITTER_TOKEN_TYPE, null)
        set(value) = prefs.modify(true) { putString(KEY_GITTER_TOKEN_TYPE, value) }

    override var refreshToken: String?
        get() = prefs.getString(KEY_GITTER_REFRESH_TOKEN, null)
        set(value) = prefs.modify(true) { putString(KEY_GITTER_REFRESH_TOKEN, value) }

    override var expiresIn: Long
        get() = prefs.getLong(KEY_GITTER_TOKEN_EXPIRES_IN, 0)
        set(value) {
            prefs.modify(true) { putLong(KEY_GITTER_TOKEN_EXPIRES_IN, value) }
        }

    override fun clear() {
        prefs.modify {
            remove(KEY_GITTER_ACCESS_TOKEN)
            remove(KEY_GITTER_TOKEN_TYPE)
            remove(KEY_GITTER_REFRESH_TOKEN)
            remove(KEY_GITTER_TOKEN_EXPIRES_IN)
        }
    }

    override fun refresh() {
        clear()
        AppExecutors.mainThreadExecutor.execute {
            sessionListener?.sessionExpired()
        }
    }

    override fun subscribeToSessionExpired(sessionListener: SessionListener) {
        this.sessionListener = sessionListener
    }

    companion object {
        private const val KEY_GITTER_ACCESS_TOKEN = "gitter_token"
        private const val KEY_GITTER_TOKEN_TYPE = "gitter_token_type"
        private const val KEY_GITTER_REFRESH_TOKEN = "gitter_refresh_token"
        private const val KEY_GITTER_TOKEN_EXPIRES_IN = "gitter_token_expires_in"
    }
}