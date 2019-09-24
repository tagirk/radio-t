package su.tagir.apps.radiot.model.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import su.tagir.apps.radiot.model.entries.Event
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.entries.Token
import su.tagir.apps.radiot.ui.chat.AuthListener

interface ChatRepository {

    fun subcribeAuthNeeded(authListener: AuthListener)

    fun getOAuthToken(appId: String?, appKey: String?, code: String, redirectUri: String?): Single<Token>

    fun removeAuthData()

    fun sendMessage(message: String): Completable

    fun getMessageStream(): Flowable<GitterMessage>

    fun getEventsStream(): Flowable<Event>

    fun loadMessages(lastId: String? = null): Completable

    fun getMessages(): Flowable<out List<MessageFull>>
}