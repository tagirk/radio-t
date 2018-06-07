package su.tagir.apps.radiot.ui.common

import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.utils.longDateFormat
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible


class PrepViewHolder(view: View, private val callback: EntriesAdapter.Callback) :  DataBoundViewHolder<Entry>(view) {

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.date)
    lateinit var date: TextView

    private lateinit var podcast: Entry

    init {
        ButterKnife.bind(this, view)
    }

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        title.text = t.title
        date.text = t.date?.longDateFormat()
        podcast = t
    }

    @OnClick(R.id.root)
    fun onPlayClick() {
        callback.onClick(podcast)
    }

}

class PodcastViewHolder(view: View,
                        private val type:Int,
                        private val glide: GlideRequests?,
                        private val callback: EntriesAdapter.Callback)

    : DataBoundViewHolder<Entry>(view), MenuItem.OnMenuItemClickListener {

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.image)
    lateinit var image: ImageView

    @BindView(R.id.blur)
    lateinit var blur: ImageView

    @BindView(R.id.progress)
    lateinit var progress: ProgressBar

    @BindView(R.id.cancel)
    lateinit var cancel: ImageView

    @BindView(R.id.btn_download)
    lateinit var download: ImageButton

    @BindView(R.id.btn_remove)
    lateinit var remove: ImageButton

    @BindView(R.id.show_notes)
    lateinit var showNotes: TextView

    @JvmField
    @BindDimen(R.dimen.item_image_corner_radius)
    var cornerRadius: Int = 0

    @JvmField
    @BindColor(R.color.colorPrimaryText)
    var primaryTextColor: Int = 0

    @JvmField
    @BindColor(R.color.colorAccent)
    var accentColor: Int = 0

    private lateinit var podcast: Entry

    init {
        ButterKnife.bind(this, view)
    }

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        title.text = t.title
        val date = t.date?.longDateFormat()
        val notes = if (t.showNotes.isNullOrBlank()) "" else t.showNotes?.replace("\n", "")
        val sb = SpannableStringBuilder()
                .append(date)

        if (!notes.isNullOrBlank()) {
            sb.append(" - ").append(notes)
        }

        sb.setSpan(ForegroundColorSpan(primaryTextColor), 0, date?.length
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        showNotes.text = sb

        val progress = t.downloadProgress
        this.progress.progress = progress
        this.progress.visibleInvisible(progress >= 0 && t.file == null)
        cancel.visibleInvisible(progress >= 0 && t.file == null)

        download.visibleInvisible(progress < 0 && t.file == null)
        remove.visibleInvisible(t.file != null)

        glide
                ?.load(t.image)
                ?.diskCacheStrategy(DiskCacheStrategy.ALL)
                ?.transforms(CenterCrop(), RoundedCorners(cornerRadius))
                ?.placeholder(R.drawable.ic_notification_large)
                ?.error(R.drawable.ic_notification_large)
                ?.into(image)

        blur.visibleGone(t.state == EntryState.PLAYING || t.state == EntryState.PAUSED)
        blur.setImageDrawable(getImageByState(t.state))
        this.podcast = t

        itemView.setOnCreateContextMenuListener { menu, _, _ ->
            menu?.add(Menu.NONE, ITEM_LISTEN, 0, R.string.listen)?.setOnMenuItemClickListener(this)
            if (progress < 0 && t.file == null) {
                menu?.add(Menu.NONE, ITEM_DOWNLOAD, 1, R.string.download)?.setOnMenuItemClickListener(this)
            } else {
                menu?.add(Menu.NONE, ITEM_DELETE, 1, R.string.delete)?.setOnMenuItemClickListener(this)
            }
            if(type== EntriesAdapter.TYPE_PODCAST) {
                menu?.add(Menu.NONE, ITEM_WEB, 2, R.string.web)?.setOnMenuItemClickListener(this)
                menu?.add(Menu.NONE, ITEM_CHAT, 3, R.string.chat_log)?.setOnMenuItemClickListener(this)
            }
        }
    }

    @OnClick(R.id.root)
    fun onPlayClick() {
        callback.onClick(podcast)
    }

    @OnClick(R.id.btn_download)
    fun onDownloadClick() {
        callback.download(podcast)
    }

    @OnClick(R.id.btn_remove, R.id.cancel)
    fun onRemoveClick() {
        AlertDialog.Builder(itemView.context)
                .setMessage("Удалить файл?")
                .setPositiveButton("Да", { _, _ -> callback.remove(podcast) })
                .setNegativeButton("Нет", null)
                .create()
                .show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            ITEM_LISTEN -> onPlayClick()
            ITEM_DOWNLOAD -> onDownloadClick()
            ITEM_DELETE -> onRemoveClick()
            ITEM_WEB -> callback.openWebSite(podcast)
            ITEM_CHAT -> callback.openChatLog(podcast)
        }
        return false
    }

    private fun getImageByState(state: Int): Drawable? {
        return when (state) {
            EntryState.PLAYING -> {
                val animation = ContextCompat.getDrawable(itemView.context, R.drawable.ic_equalizer_white_36dp) as AnimationDrawable?
                DrawableCompat.setTintList(animation!!, ColorStateList.valueOf(accentColor))
                animation.start()
                animation
            }
            EntryState.PAUSED -> ContextCompat.getDrawable(itemView.context, R.drawable.ic_play_accent_24dp)

            else -> null
        }
    }

    companion object {

        const val ITEM_LISTEN = 0
        const val ITEM_DOWNLOAD = 1
        const val ITEM_DELETE = 2
        const val ITEM_WEB = 3
        const val ITEM_CHAT = 4
    }
}

class NewsViewHolder(view: View, private val callback: EntriesAdapter.Callback) :  DataBoundViewHolder<Entry>(view) {

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.image)
    lateinit var image: ImageView

    @BindView(R.id.blur)
    lateinit var blur: View

    @BindView(R.id.progress)
    lateinit var progress: ProgressBar

    @BindView(R.id.cancel)
    lateinit var cancel: ImageView

    @BindView(R.id.btn_download)
    lateinit var download: ImageButton

    @BindView(R.id.btn_remove)
    lateinit var remove: ImageButton

    @BindView(R.id.show_notes)
    lateinit var showNotes: TextView

    @JvmField
    @BindColor(R.color.colorPrimaryText)
    var primaryTextColor: Int = 0

    private lateinit var entry: Entry

    init {
        ButterKnife.bind(this, view)
    }

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
        sb.setSpan(ForegroundColorSpan(primaryTextColor), 0, date?.length?.plus(3)
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

    @OnClick(R.id.root)
    fun onPlayClick() {
        callback.onClick(entry)
    }

}