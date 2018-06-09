package su.tagir.apps.radiot.schedulers

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulerProvider : BaseSchedulerProvider {

    override fun computation() = Schedulers.computation()

    override fun io() = Schedulers.io()

    override fun networkIO() = Schedulers.from(AppExecutors.networkIOExecutors)

    override fun diskIO() = Schedulers.from(AppExecutors.diskIOExecutor)

    override fun ui(): Scheduler = AndroidSchedulers.mainThread()

}
