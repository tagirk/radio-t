package su.tagir.apps.radiot.model

import android.app.IntentService
import android.content.Context
import android.content.Intent
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.service.AudioService

class PodcastStateService : IntentService("EntryStateService") {

    companion object {
        const val ACTION_SAVE_CURRENT = "action_save_current"
        const val ACTION_UPDATE_CURRENT_ENTRY_STATE = "action_update_state"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_STOP = "action_stop"

        const val KEY_URL = "key_url"
        const val KEY_STATE = "key_state"
        const val KEY_PROGRESS = "key_progress"


        fun saveCurrent(audioUrl: String, lastProgress: Long, context: Context) {
            val i = newIntent(context)
            i.action = ACTION_SAVE_CURRENT
            i.putExtra(KEY_URL, audioUrl)
            i.putExtra(KEY_PROGRESS, lastProgress)
            context.startService(i)
        }

        fun updateCurrentPodcastStateAndProgress(state: Int, progress: Long, context: Context) {
            val i = newIntent(context)
            i.action = ACTION_UPDATE_CURRENT_ENTRY_STATE
            i.putExtra(KEY_STATE, state)
            i.putExtra(KEY_PROGRESS, progress)
            context.startService(i)
        }
        private fun newIntent(context: Context) = Intent(context, PodcastStateService::class.java)
    }


    private lateinit var entryRepository: EntryRepository

    override fun onCreate() {
        super.onCreate()
        entryRepository = (application as App).appComponent.entryRepository
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        when (intent.action) {

            ACTION_SAVE_CURRENT -> {
                val url = intent.getStringExtra(KEY_URL)
                val lastProgress = intent.getLongExtra(KEY_PROGRESS, 0L)
                entryRepository.setCurrentEntry(url, lastProgress)
            }

            ACTION_UPDATE_CURRENT_ENTRY_STATE -> {
                val state = intent.getIntExtra(KEY_STATE, EntryState.IDLE)
                val progress = intent.getLongExtra(KEY_PROGRESS, 0L)
                if (state != EntryState.IDLE) {
                    entryRepository.updateCurrentEntryStateAndProgress(state, progress)
                }
            }
            ACTION_PAUSE -> AudioService.pause(this)

            ACTION_RESUME -> AudioService.resume(this)

            ACTION_STOP -> AudioService.stop(this)
        }
    }
}