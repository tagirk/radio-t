package su.tagir.apps.radiot.model.repository

import android.arch.paging.PagedList
import io.reactivex.Observer
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.RTEntry


class PodcastBoundaryCallback(private val restClient: RestClient,
                              private val handleResponse: (List<RTEntry>) -> Unit,
                              private val observer:Observer<List<Entry>>) : PagedList.BoundaryCallback<List<Entry>>(){


}