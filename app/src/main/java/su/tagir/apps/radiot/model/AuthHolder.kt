package su.tagir.apps.radiot.model

interface AuthHolder {
    var token: String?
    var tokenType: String?

    fun clear()
}

