package su.tagir.apps.radiot.model.repository

import android.text.TextUtils
import com.google.gson.Gson
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import su.tagir.apps.radiot.model.api.*
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.*
import su.tagir.apps.radiot.ui.chat.AuthListener
import timber.log.Timber

class ChatRepositoryImpl(private val authClient: GitterAuthClient,
                         private val streamClient: GitterStreamClient,
                         private val gitterClient: GitterClient,
                         private val database: RadiotDb,
                         private val authHolder: AuthHolder,
                         private val gson: Gson,
                         private val dispatcher: CoroutineDispatcher = Dispatchers.Default) : ChatRepository {

    private val roomId = "5738c079c43b8c6019730ee3"
//    private val roomId = "5a832dffd73408ce4f8d0021"

    private val messageQueries = database.messageQueries
    private val userQueries = database.userQueries
    private val pageResultQueries = database.pageResultQueries

    private var authListener: AuthListener? = null

    init {
        authHolder.subscribeToSessionExpired(object : SessionListener {
            override fun sessionExpired() {
                authListener?.needAuth()
            }
        })
    }

    override val isSignedIn: Boolean
        get() = authHolder.tokenType != null && authHolder.accessToken != null

    override fun subcribeAuthNeeded(authListener: AuthListener) {
        this.authListener = authListener

    }

    @ExperimentalCoroutinesApi
    override suspend fun getOAuthToken(appId: String?, appKey: String?, code: String, redirectUri: String?) {
        val token = authClient.auth(appId, appKey, code, redirectUri)
        Timber.d("token: $token")
        dispatcher {
            authHolder.accessToken = token.token
            authHolder.tokenType = token.type
            token.expiresIn?.let { time ->
                authHolder.expiresIn = time
            }
            Timber.d("authHolder: ${authHolder.tokenType} ${authHolder.accessToken}")
        }
    }

    override fun removeAuthData() {
        authHolder.clear()
    }

    override suspend fun sendMessage(message: String) = gitterClient.sendMessage(roomId, message)


    override suspend fun subscribeMessageStream() {
//        val body = streamClient.getRoomMessagesStream(roomId)
//
//
//        return streamClient.getRoomMessagesStream(roomId)
//                .flatMap { responseBody -> events(responseBody.source()) }
//                .filter { checkIfValidMessageJson(it) }
//                .map { gson.fromJson(it, GitterMessage::class.java) }
//                .observeOn(scheduler.io())
//                .doOnNext { gitterDao.saveMessage(it) }
    }

    override suspend fun subscribeEventsStream() {
//        streamClient.getRoomEventsStream(roomId)
//                .flatMap { responseBody -> events(responseBody.source()) }
//                .filter { checkIfValidMessageJson(it) }
//                .map { gson.fromJson(it, Event::class.java) }
    }


    @ExperimentalCoroutinesApi
    override suspend fun loadMessages(lastId: String?) {
        val messages = gitterClient.getRoomMessages(roomId, 50, lastId)
        dispatcher {
            database.transaction {
                if (lastId == null) {
                    pageResultQueries.removeQuery("chat")
                }
                mergeAndSavePageResult("chat", messages)
            }
        }
    }

    @FlowPreview
    override fun getMessages(): Flow<List<MessageFull>> {
        return pageResultQueries.findByQuery("chat", pageResultMapper)
                .asFlow()
                .mapToOneOrDefault(PageResult("chat", emptyList(), 0), dispatcher)
                .flatMapConcat { page ->
                    Timber.d("page: $page")
                    val idsStr = "'${TextUtils.join("','", page.ids)}'"
                    messageQueries
                        .findByIdWithUser(idsStr, messageMapper)
                        .asFlow()
                        .mapToList(dispatcher)}
    }

//    private fun events(source: BufferedSource): Flowable<String?> {
//        return Flowable.create({ emitter ->
//            emitter.setCancellable {
//                try {
//                    source.close()
//                } catch (e: Throwable) {
//                    Timber.e(e)
//                }
//            }
//            try {
//                while (!source.exhausted()) {
//                    val string = source.readUtf8Line()
//                    if (string != null && !emitter.isCancelled) {
//                        emitter.onNext(string)
//                    }
//                }
//            } catch (e: Exception) {
//                if (!emitter.isCancelled) {
//                    emitter.onError(e)
//                }
//            }
//            if (!emitter.isCancelled) {
//                emitter.onComplete()
//            }
//        }, BackpressureStrategy.BUFFER)
//
//    }

    private fun checkIfValidMessageJson(json: String?): Boolean {
        return json != null && json.contains("{") && json.contains("}")
    }

    private fun mergeAndSavePageResult(query: String, messages: List<GitterMessage>, totalCount: Int? = null) {
        val current = pageResultQueries
                .findByQuery(query, mapper = pageResultMapper)
                .executeAsOneOrNull()
                ?: PageResult(query, emptyList(), totalCount)

        val merged = current.ids.toMutableList()
        merged.addAll(messages.map { it.id })
        pageResultQueries.insert(query, merged, totalCount)

        messages.forEach { gm ->
            gm.toMessage().insert(messageQueries)
            gm.fromUser?.insert(userQueries)
        }
    }
}