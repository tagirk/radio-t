package su.tagir.apps.radiot.model.repository

import androidx.paging.RxPagedListBuilder
import com.google.gson.Gson
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import okio.BufferedSource
import su.tagir.apps.radiot.model.Prefs
import su.tagir.apps.radiot.model.api.GitterAuthClient
import su.tagir.apps.radiot.model.api.GitterClient
import su.tagir.apps.radiot.model.api.GitterStreamClient
import su.tagir.apps.radiot.model.db.GitterDao
import su.tagir.apps.radiot.model.entries.Event
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.Token
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val authClient: GitterAuthClient,
                                         private val streamClient: GitterStreamClient,
                                         private val gitterClient: GitterClient,
                                         private val gitterDao: GitterDao,
                                         private val authHolder: Prefs,
                                         private val gson: Gson,
                                         private val scheduler: BaseSchedulerProvider) {

    private val roomId = "5738c079c43b8c6019730ee3"
//    private val roomId = "5a832dffd73408ce4f8d0021"

    val isSignedIn: Boolean
        get() = authHolder.token != null

    fun getOAuthToken(appId: String?, appKey: String?, code: String, redirectUri: String?): Single<Token> =
            authClient.auth(appId, appKey, code, redirectUri)
                    .doOnSuccess {
                        authHolder.token = it.token
                        authHolder.tokenType = it.type
                    }

    fun removeAuthData() {
        authHolder.token = null
    }

    fun sendMessage(message: String): Completable {
        return gitterClient.sendMessage(roomId, message)
                .toCompletable()
    }

    fun getMessageStream(): Flowable<GitterMessage?> {
        return streamClient.getRoomMessagesStream(roomId)
                .flatMap { responseBody -> events(responseBody.source()) }
                .filter { checkIfValidMessageJson(it) }
                .map {gson.fromJson(it, GitterMessage::class.java) }
                .observeOn(scheduler.io())
                .doOnNext { gitterDao.saveMessage(it) }
    }

    fun getEventsStream(): Flowable<Event?> {
        return streamClient.getRoomEventsStream(roomId)
                .flatMap { responseBody -> events(responseBody.source()) }
                .filter { checkIfValidMessageJson(it) }
                .map { gson.fromJson(it, Event::class.java) }
    }

    fun loadMessages(lastId: String?=null): Completable {
        return gitterClient.getRoomMessages(roomId, 50, lastId)
                .observeOn(scheduler.io())
                .doOnSuccess { gitterDao.saveMessages(it) }
                .toCompletable()
    }

    fun getMessages() = RxPagedListBuilder(gitterDao.getMessages(), 50)
            .setFetchScheduler(scheduler.io())
            .setNotifyScheduler(scheduler.ui())
            .buildFlowable(BackpressureStrategy.BUFFER)

    private fun events(source: BufferedSource): Flowable<String?> {
        return Flowable.create({ emitter ->
            emitter.setCancellable {
                try {
                    source.close()
                } catch (e: Throwable) {
                    Timber.e(e)
                }
            }
            try {
                while (!source.exhausted()) {
                    val string = source.readUtf8Line()
                    if (string != null && !emitter.isCancelled) {
                        emitter.onNext(string)
                    }
                }
            } catch (e: Exception) {
                if (!emitter.isCancelled) {
                    emitter.onError(e)
                }
            }
            if (!emitter.isCancelled) {
                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)

    }

    private fun checkIfValidMessageJson(json: String?): Boolean {
        return json != null && json.contains("{") && json.contains("}")
    }
}