package su.tagir.apps.radiot.model.api

import io.reactivex.Single
import retrofit2.http.GET
import su.tagir.apps.radiot.model.entries.Host


interface FirebaseRestClient {

    @GET("/hosts.json")
    fun getHosts(): Single<List<Host>>

}