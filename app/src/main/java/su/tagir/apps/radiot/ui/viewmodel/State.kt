package su.tagir.apps.radiot.ui.viewmodel

data class State<out T>(val status: Status,
                    val data: T? = null,
                    private val errorMessage: String? = null,
                    val hasNextPage: Boolean = true) {

    private var errorHandled: Boolean = false

    fun getErrorIfNotHandled(): String? {
        if (!errorHandled) {
            errorHandled = true
            return errorMessage
        }
        return null
    }

    val loading
        get() = status == Status.LOADING

    val loadingMore
        get() = status == Status.LOADING_MORE

    val refreshing
        get() = status == Status.REFRESHING

    val error
        get() = status == Status.ERROR

    val completed
        get() = status == Status.SUCCESS


}
