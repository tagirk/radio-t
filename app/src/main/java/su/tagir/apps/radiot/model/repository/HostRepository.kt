package su.tagir.apps.radiot.model.repository

import io.reactivex.Completable
import su.tagir.apps.radiot.model.api.FirebaseRestClient
import su.tagir.apps.radiot.model.db.HostDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostRepository @Inject constructor(private val hostDao: HostDao,
                                         private val firebaseRestClient: FirebaseRestClient) {

    fun refreshHosts(): Completable {
        return firebaseRestClient
                .getHosts()
                .doOnSuccess { hostDao.insertHosts(it) }
                .toCompletable()
    }

    fun getHosts() = hostDao.findHosts()
}