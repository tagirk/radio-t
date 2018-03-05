package su.tagir.apps.radiot.ui.viewmodel

class ViewModelState private constructor(val loading: Boolean = false,
                                         val refreshing: Boolean = false,
                                         val loadingMore: Boolean = false,
                                         val error: Boolean = false,
                                         private val errorMessage: String?) {

    private var errorHandled: Boolean = false

    fun getErrorIfNotHandled(): String? {
        if (!errorHandled) {
            errorHandled = true
            return errorMessage
        }
        return null
    }

    fun isCompleted() = !loading && !refreshing && !loadingMore && !error

    companion object {
        val COMPLETE = ViewModelState(loading = false, refreshing = false, loadingMore = false, error = false, errorMessage = null)
        val LOADING = ViewModelState(loading = true, refreshing = false, loadingMore = false, error = false, errorMessage = null)
        fun error(errorMessage: String?) = ViewModelState(loading = false, refreshing = false, loadingMore = false, error = true, errorMessage = errorMessage)
        val REFRESHING = ViewModelState(loading = false, refreshing = true, loadingMore = false, error = false, errorMessage = null)
        val LOADING_MORE = ViewModelState(loading = false, refreshing = false, loadingMore = true, error = false, errorMessage = null)
    }

}