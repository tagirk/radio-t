package su.tagir.apps.radiot.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.jakewharton.rxrelay2.PublishRelay
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.*


class SearchAdapter(private val glide: RequestManager) : DataBoundListAdapter<Entry>() {

    private val selection = PublishRelay.create<Entry>()
    private val download = PublishRelay.create<Entry>()
    private val remove = PublishRelay.create<Entry>()
    private val comments = PublishRelay.create<Entry>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<Entry> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EntriesAdapter.TYPE_PODCAST -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, glide)
            }
            EntriesAdapter.TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                PrepViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                NewsViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val categories = items?.get(position)?.categories
        if (categories?.contains("podcast") == true) {
            return EntriesAdapter.TYPE_PODCAST
        } else if (categories?.contains("prep") == true) {
            return EntriesAdapter.TYPE_PREP
        }
        return EntriesAdapter.TYPE_NEWS
    }

    override fun bind(viewHolder: DataBoundViewHolder<Entry>, position: Int) {
        viewHolder.bind(items?.get(position))
        viewHolder.itemView.setOnClickListener { selection.accept(items?.get(position)) }
        if (viewHolder is PodcastViewHolder) {
            viewHolder.remove.setOnClickListener { remove.accept(items?.get(viewHolder.adapterPosition)) }
            viewHolder.cancel.setOnClickListener { remove.accept(items?.get(viewHolder.adapterPosition)) }
            viewHolder.download.setOnClickListener { download.accept(items?.get(viewHolder.adapterPosition)) }
            viewHolder.comments.setOnClickListener { comments.accept(items?.get(viewHolder.adapterPosition)) }
        }

    }

    override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem

    fun entryClicks() = selection
    fun downloadClicks() = download
    fun removeClicks() = remove
    fun commentClicks() = comments
}

