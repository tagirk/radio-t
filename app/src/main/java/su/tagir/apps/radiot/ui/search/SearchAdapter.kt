package su.tagir.apps.radiot.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.*


class SearchAdapter(private val glide: RequestManager, private val callback: EntriesAdapter.Callback) : DataBoundListAdapter<Entry>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<Entry> {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: DataBoundViewHolder<Entry>
        when (viewType) {
            EntriesAdapter.TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                viewHolder = NewsViewHolder(view)
            }
            EntriesAdapter.TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                viewHolder = PrepViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                viewHolder = PodcastViewHolder(view, glide)
            }
        }
        getItem(viewHolder.adapterPosition).let { entry ->
            viewHolder.itemView.setOnClickListener { callback.onEntryClick(entry) }
            if(viewHolder is PodcastViewHolder){
                viewHolder.remove.setOnClickListener { callback.onRemoveClick(entry) }
                viewHolder.cancel.setOnClickListener { callback.onRemoveClick(entry) }
                viewHolder.download.setOnClickListener { callback.onDownloadClick(entry) }
                viewHolder.comments.setOnClickListener { callback.onCommentClick(entry) }
            }
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        val categories = getItem(position).categories
        if (categories?.contains("podcast") == true) {
            return EntriesAdapter.TYPE_PODCAST
        } else if (categories?.contains("prep") == true) {
            return EntriesAdapter.TYPE_PREP
        }
        return EntriesAdapter.TYPE_NEWS
    }


    override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem

}

