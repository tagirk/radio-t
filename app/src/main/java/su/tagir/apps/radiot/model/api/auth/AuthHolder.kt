package su.tagir.apps.radiot.model.api.auth

import su.tagir.apps.radiot.model.api.SessionListener

interface AuthHolder {
    var authHeader: String
    var accessToken: String?
    var tokenType: String?
    var refreshToken: String?
    var expiresIn: Long

    fun clear()
    fun refresh()
    fun subscribeToSessionExpired(sessionListener: SessionListener)
}

