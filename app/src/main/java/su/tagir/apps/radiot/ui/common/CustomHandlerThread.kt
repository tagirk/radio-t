package su.tagir.apps.radiot.ui.common

import android.os.Handler
import android.os.HandlerThread


open class CustomHandlerThread(name:String): HandlerThread(name,  android.os.Process.THREAD_PRIORITY_BACKGROUND) {

    private var handler: Handler? = null

    fun prepareHandler(){
        start()
        handler = Handler(looper)
    }

    fun post(task: Runnable){
        handler?.post(task)
    }

    fun postDelayed(task: Runnable, delayMillis: Long){
        handler?.postDelayed(task, delayMillis)
    }

    fun dispose(){
        handler?.removeCallbacksAndMessages(null)
        quit()
    }

    fun removeTask(task: Runnable){
        handler?.removeCallbacks(task)
    }

}