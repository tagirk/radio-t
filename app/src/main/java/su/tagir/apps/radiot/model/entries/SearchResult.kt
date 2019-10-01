package su.tagir.apps.radiot.model.entries

import java.util.*
import kotlin.collections.ArrayList

data class SearchResult(
        val query: String,
        val ids: List<String> = ArrayList(),
        val timeStamp: Date = Date())

val searchResultMapper:(query: String, ids: List<String>, timeStamp: Date) -> SearchResult
    get() = {query, ids, timeStamp -> SearchResult(query, ids, timeStamp) }