package su.tagir.apps.radiot.schedulers

import io.reactivex.Scheduler


interface BaseSchedulerProvider {

    fun computation(): Scheduler

    fun io(): Scheduler

    fun diskIO(): Scheduler

    fun networkIO(): Scheduler

    fun ui(): Scheduler
}
