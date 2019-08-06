package su.tagir.apps.radiot.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val type: Int, private val glide: RequestManager?, private val callback: Callback) :
        PagedListAdapter<Entry, RecyclerView.ViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PodcastViewHolder -> holder.bind(getItem(position))
            is PrepViewHolder -> holder.bind(getItem(position))
            is NewsViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder
        when (viewType) {
            TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                viewHolder = NewsViewHolder(view)
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                viewHolder = PrepViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                viewHolder = PodcastViewHolder(view, glide!!)
            }
        }
        getItem(viewHolder.adapterPosition)?.let { entry ->
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

    override fun getItemViewType(position: Int) = type

    companion object {

        const val TYPE_PREP = 1
        const val TYPE_PODCAST = 2
        const val TYPE_NEWS = 3
        const val TYPE_PIRATES = 4

        private val diffCallback
            get() = object : DiffUtil.ItemCallback<Entry>() {
                override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.url == newItem.url

                override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem

            }
    }

    interface Callback {
        fun onEntryClick(entry: Entry)
        fun onDownloadClick(entry: Entry)
        fun onRemoveClick(entry: Entry)
        fun onCommentClick(entry: Entry)
    }
}

