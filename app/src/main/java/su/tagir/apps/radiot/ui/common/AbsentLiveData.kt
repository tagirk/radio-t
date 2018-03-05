package su.tagir.apps.radiot.ui.common

import android.arch.lifecycle.LiveData

class AbsentLiveData<T>: LiveData<T>() {

    init {
        postValue(null)
    }

    companion object {
        fun <T>create() = AbsentLiveData<T>()
    }
}