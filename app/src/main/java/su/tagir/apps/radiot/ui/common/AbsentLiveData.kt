package su.tagir.apps.radiot.ui.common

import androidx.lifecycle.LiveData

class AbsentLiveData<T>: LiveData<T>() {

    init {
        postValue(null)
    }

    companion object {
        fun <T>create() = AbsentLiveData<T>()
    }
}