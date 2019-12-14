package su.tagir.apps.radiot.model.entries

data class PageResult(
        val query: String,
        val ids: List<String> = ArrayList(),
        val totalCount: Int?)

val pageResultMapper:(query: String, ids: List<String>, totalCount: Int?) -> PageResult
    get() = {query, ids, totalCount -> PageResult(query, ids, totalCount) }