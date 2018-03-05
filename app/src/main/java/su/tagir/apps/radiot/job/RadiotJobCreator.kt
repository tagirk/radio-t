package su.tagir.apps.radiot.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class RadiotJobCreator : JobCreator {


    override fun create(tag: String): Job? {
        return when (tag) {
            StreamNotificationJob.TAG -> StreamNotificationJob()
            else -> null
        }
    }
}