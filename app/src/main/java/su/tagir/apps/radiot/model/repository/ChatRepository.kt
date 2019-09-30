package su.tagir.apps.radiot.model.repository

import kotlinx.coroutines.flow.Flow
import su.tagir.apps.radiot.model.entries.Event
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.ui.chat.AuthListener

interface ChatRepository {

    fun subcribeAuthNeeded(authListener: AuthListener)

    suspend fun getOAuthToken(appId: String?, appKey: String?, code: String, redirectUri: String?)

    fun removeAuthData()

    suspend fun sendMessage(message: String)

    fun getMessageStream(): Flow<GitterMessage>

    fun getEventsStream(): Flow<Event>

    suspend fun loadMessages(lastId: String? = null)

    fun getMessages(): Flow<List<MessageFull>>
}