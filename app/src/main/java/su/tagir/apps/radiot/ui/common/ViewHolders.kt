package su.tagir.apps.radiot.ui.common

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.utils.longDateFormat
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible


class PrepViewHolder(view: View) : DataBoundViewHolder<Entry>(view) {

    private val title: TextView = itemView.findViewById(R.id.title)

    private val date: TextView = itemView.findViewById(R.id.date)

    private lateinit var podcast: Entry

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        title.text = t.title
        date.text = t.date?.longDateFormat()
        podcast = t
    }
}

class PodcastViewHolder(view: View) : DataBoundViewHolder<Entry>(view){

    private val title: TextView = itemView.findViewById(R.id.title)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val blur: ImageView = itemView.findViewById(R.id.blur)
    private val progress: ProgressBar = itemView.findViewById(R.id.progress)
    val cancel: ImageView = itemView.findViewById(R.id.cancel)
    val download: ImageButton = itemView.findViewById(R.id.btn_download)
    val remove: ImageButton = itemView.findViewById(R.id.btn_remove)
    private val showNotes: TextView = itemView.findViewById(R.id.show_notes)
    val comments: TextView = itemView.findViewById(R.id.comments)

    private lateinit var podcast: Entry

    @SuppressLint("SetTextI18n")
    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        title.text = t.title
        val date = t.date?.longDateFormat()
        val notes = if (t.showNotes.isNullOrBlank()) "" else t.showNotes.replace("\n", "")
        val sb = SpannableStringBuilder()
                .append(date)

        if (!notes.isBlank()) {
            sb.append(" - ").append(notes)
        }

        sb.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimaryText)), 0, date?.length
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        showNotes.text = sb

        val progress = t.downloadProgress
        this.progress.progress = progress
        this.progress.visibleInvisible(progress >= 0 && t.file == null)
        cancel.visibleInvisible(progress >= 0 && t.file == null)

        download.visibleInvisible(progress < 0 && t.file == null)
        remove.visibleInvisible(t.file != null)

        t.image?.let{url ->
            val config = ImageConfig(placeholder = R.drawable.ic_notification_large, error = R.drawable.ic_notification_large)
            ImageLoader.display(url, image, config)
        }

        blur.visibleGone(t.state == EntryState.PLAYING || t.state == EntryState.PAUSED)
        blur.setImageDrawable(getImageByState(t.state))
        this.podcast = t

        comments.text = itemView.resources.getQuantityString(R.plurals.comments, t.commentsCount, t.commentsCount)
    }

    private fun getImageByState(state: Int): Drawable? {
        return when (state) {
            EntryState.PLAYING -> {
                val animation = ContextCompat.getDrawable(itemView.context, R.drawable.ic_equalizer_white_36dp) as AnimationDrawable?
                DrawableCompat.setTintList(animation!!, ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.colorAccent)))
                animation.start()
                animation
            }
            EntryState.PAUSED -> ContextCompat.getDrawable(itemView.context, R.drawable.ic_play_accent_24dp)

            else -> null
        }
    }
}

class NewsViewHolder(view: View) : DataBoundViewHolder<Entry>(view) {

    private val title: TextView = itemView.findViewById(R.id.title)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val blur: ImageView = itemView.findViewById(R.id.blur)
    private val progress: ProgressBar = itemView.findViewById(R.id.progress)
    private val cancel: ImageView = itemView.findViewById(R.id.cancel)
    private val download: ImageButton = itemView.findViewById(R.id.btn_download)
    private val remove: ImageButton = itemView.findViewById(R.id.btn_remove)
    private val showNotes: TextView = itemView.findViewById(R.id.show_notes)

    private lateinit var entry: Entry

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        title.text = t.title
        val date = t.date?.longDateFormat()
        val sb = SpannableStringBuilder()
                .append(date)
                .append(" - ")
                .append(t.showNotes?.replace("\n", ""))
        sb.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimaryText)), 0, date?.length?.plus(3)
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        showNotes.text = sb

        progress.visibleGone(false)
        download.visibleGone(false)
        remove.visibleGone(false)
        blur.visibleGone(false)
        image.visibleGone(false)
        cancel.visibleGone(false)
        this.entry = t

    }
}