package su.tagir.apps.radiot.schedulers

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

object AppExecutors {

    val mainThreadExecutor by lazy {
        object: Executor {
            val mainThreadHandler = Handler(Looper.getMainLooper())
            override fun execute(p0: Runnable) {
                mainThreadHandler.post(p0)
            }
        }
    }

}