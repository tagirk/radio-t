package su.tagir.apps.radiot.schedulers

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object AppExecutors {

    val diskIOExecutor: Executor by lazy { Executors.newSingleThreadExecutor()}

    val networkIOExecutors: Executor by lazy {  Executors.newFixedThreadPool(3)}
}