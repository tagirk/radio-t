package su.tagir.apps.radiot.model.repository

import kotlinx.coroutines.flow.Flow
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.ui.chat.AuthListener

interface ChatRepository {

    val isSignedIn: Boolean

    fun subcribeAuthNeeded(authListener: AuthListener)

    suspend fun getOAuthToken(appId: String?, appKey: String?, code: String, redirectUri: String?)

    fun removeAuthData()

    suspend fun sendMessage(message: String)

    suspend fun subscribeMessageStream()

    suspend fun subscribeEventsStream()

    suspend fun loadMessages(lastId: String? = null)

    fun getMessages(): Flow<List<MessageFull>>
}