package su.tagir.apps.radiot.schedulers

import io.reactivex.schedulers.Schedulers

class ImmediateSchedulerProvider : BaseSchedulerProvider {

    override fun computation() = Schedulers.trampoline()

    override fun io() = Schedulers.trampoline()

    override fun networkIO() = Schedulers.trampoline()

    override fun diskIO() = Schedulers.trampoline()

    override fun ui() = Schedulers.trampoline()

}
