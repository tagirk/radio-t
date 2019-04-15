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
        return when (viewType) {
            EntriesAdapter.TYPE_PODCAST -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, viewType, glide, callback)
            }
            EntriesAdapter.TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                PrepViewHolder(view, callback)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                NewsViewHolder(view, callback)
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
    }

    override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
}

