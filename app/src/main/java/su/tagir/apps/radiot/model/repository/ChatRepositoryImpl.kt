package su.tagir.apps.radiot.model.repository

import com.google.gson.Gson
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.*
import okhttp3.*
import su.tagir.apps.radiot.model.api.GitterAuthClient
import su.tagir.apps.radiot.model.api.GitterClient
import su.tagir.apps.radiot.model.api.SessionListener
import su.tagir.apps.radiot.model.api.auth.AuthHolder
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.*
import su.tagir.apps.radiot.ui.chat.AuthListener
import timber.log.Timber
import java.io.IOException

class ChatRepositoryImpl(private val authClient: GitterAuthClient,
                         private val streamClient: OkHttpClient,
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


    @ExperimentalCoroutinesApi
    override fun getMessageStream(): Flow<GitterMessage> {
        var responseBody: ResponseBody? = null
        return flow {
            val channel = Channel<String>(CONFLATED)
            val request = Request.Builder()
                    .url("https://stream.gitter.im/v1/rooms/$roomId/chatMessages")
                    .addHeader(authHolder.authHeader, "${authHolder.tokenType} ${authHolder.accessToken}")
                    .build()

            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Timber.e(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    responseBody = response.body()
                    responseBody?.let { body ->
                        try {
                            while (!body.source().exhausted()) {
                                val text = body.source().readUtf8Line()
                                text?.let {
                                    channel.offer(text)
                                }
                            }
                        }catch (e: Exception){

                        }
                    }
                }
            }
            streamClient.newCall(request).enqueue(callback)
            try {
                for (item in channel) {
                    emit(item)
                }
            } finally {
                responseBody?.source()?.close()
            }
        }
                .filter { json -> checkIfValidMessageJson(json) }
                .map { json -> gson.fromJson(json, GitterMessage::class.java) }
                .onEach { gm ->
                    dispatcher {
                        database.transaction {
                            mergeAndSavePageResult("chat", listOf(gm))
                        }
                    }
                }
                .onCompletion { responseBody?.source()?.close() }

    }

    override suspend fun subscribeEventsStream() {

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

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun getMessages(): Flow<List<MessageFull>> {
        return pageResultQueries.findByQuery("chat", pageResultMapper)
                .asFlow()
                .mapToOneOrDefault(PageResult("chat", emptyList(), 0), dispatcher)
                .flatMapLatest { page ->
                    messageQueries
                            .findByIdWithUser(page.ids, messageMapper)
                            .asFlow()
                            .mapToList(dispatcher)
                }
    }

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