package su.tagir.apps.radiot.model.entries

import java.util.*
import kotlin.collections.ArrayList

data class PageResult(
        val query: String,
        val ids: List<String> = ArrayList(),
        val totalCount: Int?,
        val timeStamp: Date = Date())

val pageResultMapper:(query: String, ids: List<String>, totalCount: Int?, timeStamp: Date) -> PageResult
    get() = {query, ids, totalCount, timeStamp -> PageResult(query, ids, totalCount, timeStamp) }