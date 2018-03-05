package su.tagir.apps.radiot.ui.common

import android.arch.paging.PagedListAdapter
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val type: Int, private val glide: GlideRequests?, private val callback: Callback) : PagedListAdapter<Entry, RecyclerView.ViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is PodcastViewHolder -> holder.bind(getItem(position))
            is PrepViewHolder -> holder.bind(getItem(position))
            is NewsViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PODCAST -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, glide, callback)
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                PrepViewHolder(view, callback)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                NewsViewHolder(view, callback)
            }
        }
    }

    override fun getItemViewType(position: Int) = type

    interface Callback {
        fun onClick(entry: Entry)

        fun download(entry: Entry)

        fun remove(entry: Entry)
    }

    companion object {

        const val TYPE_PREP = 1
        const val TYPE_PODCAST = 2
        const val TYPE_NEWS = 3

        private val diffCallback
            get() = object : DiffCallback<Entry>() {
                override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.url == newItem.url

                override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem

            }
    }
}

