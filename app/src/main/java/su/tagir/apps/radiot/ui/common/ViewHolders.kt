package su.tagir.apps.radiot.ui.common

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.ItemPodcastBinding
import su.tagir.apps.radiot.databinding.ItemPrepBinding
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.image.Transformation
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.utils.longDateFormat
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible


class PrepViewHolder(view: View) : BindingViewHolder<Entry>(view) {

    private val binding = ItemPrepBinding.bind(view)

    private lateinit var entry: Entry

    private val iconSize = itemView.resources.getDimensionPixelSize(R.dimen.commentator_image_size)
    private val margin = iconSize/4
    private val screenMargin = itemView.resources.getDimensionPixelSize(R.dimen.screen_margin)
    private val layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
    private val config = ImageConfig(fit = true, error = R.drawable.ic_account_circle_24dp, placeholder = R.drawable.ic_account_circle_24dp, transformations = listOf(Transformation.Circle))

    private val maxCount = (itemView.resources.displayMetrics.widthPixels - 2 * screenMargin) / (iconSize - margin) - 1

    init {
        layoutParams.marginEnd = -margin
    }

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        binding.title.text = t.title
        binding.date.text = t.date?.longDateFormat()
        binding.comments.text = "${t.commentsCount}"

        binding.avatars.removeAllViews()
        entry = t

        t.commentators?.let { list ->
            for (i in list.indices) {
                if (i > maxCount) {
                    break
                }
                val icon = ImageView(itemView.context)
                icon.id = View.generateViewId()
                icon.alpha = 0.7f
                binding.avatars.addView(icon, layoutParams)

                ImageLoader.display(list[i], icon, config)
            }
        }
    }
}

class PodcastViewHolder(view: View, private val isPirates: Boolean = false) : BindingViewHolder<Entry>(view) {

    val binding = ItemPodcastBinding.bind(view)

    private lateinit var podcast: Entry

    init {
        if(isPirates){
            val margin = itemView.resources.getDimensionPixelSize(R.dimen.screen_margin)
            val btnSize = itemView.resources.getDimensionPixelSize(R.dimen.download_btn_size)
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root)
            constraintSet.clear(binding.btnDownload.id)
            constraintSet.clear(binding.comments.id)
            constraintSet.connect(binding.btnDownload.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(binding.btnDownload.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(binding.btnDownload.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin)
            constraintSet.constrainHeight(binding.btnDownload.id, btnSize)
            constraintSet.constrainWidth(binding.btnDownload.id, btnSize)
            constraintSet.applyTo(binding.root)
            binding.comments.visibleGone(false)
            binding.showNotes.minLines = 1
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        binding.title.text = t.title
        val date = t.date?.longDateFormat()
        val notes = if (t.showNotes.isNullOrBlank()) "" else t.showNotes.replace("\n", " ")
        val sb = SpannableStringBuilder()
                .append(date)

        if (!notes.isBlank()) {
            sb.append(" -").append(notes)
        }
        val resources = binding.root.resources
        val firstLineMargin = resources.getDimensionPixelSize(R.dimen.item_image_size) + (binding.title.layoutParams as ConstraintLayout.LayoutParams).marginStart
        sb.setSpan(LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimaryText)), 0, date?.length
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.showNotes.text = sb

        val progress = t.downloadProgress
        binding.progress.progress = progress
        binding.progress.visibleInvisible(progress >= 0 && t.file == null)
        binding.cancel.visibleInvisible(progress >= 0 && t.file == null)

        binding.btnDownload.visibleInvisible(progress < 0 && t.file == null)
        binding.btnRemove.visibleInvisible(t.file != null)

        t.image?.let { url ->
            val config = ImageConfig(placeholder = R.drawable.ic_notification_large, error = R.drawable.ic_notification_large)
            ImageLoader.display(url, binding.image, config)
        }

        binding.blur.visibleGone(t.state == EntryState.PLAYING || t.state == EntryState.PAUSED)
        binding.blur.setImageDrawable(getImageByState(t.state))
        this.podcast = t

        binding.comments.text = "${t.commentsCount}"


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

class NewsViewHolder(view: View) : BindingViewHolder<Entry>(view) {

    val binding = ItemPrepBinding.bind(view)
    private lateinit var entry: Entry

    init {
        binding.avatars.visibleGone(false)
        binding.date.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorSecondaryText))
    }

    override fun bind(t: Entry?) {
        if (t == null) {
            return
        }
        binding.title.text = t.title
        val date = t.date?.longDateFormat()
        val sb = SpannableStringBuilder()
                .append(date)
                .append(" - ")
                .append(t.showNotes?.replace("\n", ""))
        sb.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimaryText)), 0, date?.length
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.date.text = sb
        binding.comments.text = "${t.commentsCount}"

        this.entry = t

    }
}