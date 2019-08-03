package su.tagir.apps.radiot.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.jakewharton.rxrelay2.PublishRelay
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val type: Int, private val glide: RequestManager?) :
        PagedListAdapter<Entry, RecyclerView.ViewHolder>(diffCallback) {

    private val selection = PublishRelay.create<Entry>()
    private val download = PublishRelay.create<Entry>()
    private val remove = PublishRelay.create<Entry>()
    private val comments = PublishRelay.create<Entry>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder.itemView.setOnClickListener { selection.accept(getItem(position)) }


        when (holder) {
            is PodcastViewHolder -> {
                holder.bind(getItem(position))
                holder.remove.setOnClickListener { remove.accept(getItem(holder.adapterPosition)) }
                holder.cancel.setOnClickListener { remove.accept(getItem(holder.adapterPosition)) }
                holder.download.setOnClickListener { download.accept(getItem(holder.adapterPosition)) }
                holder.comments.setOnClickListener { comments.accept(getItem(holder.adapterPosition)) }
            }
            is PrepViewHolder -> holder.bind(getItem(position))
            is NewsViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                NewsViewHolder(view)
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                PrepViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, glide!!)
            }
        }
    }

    override fun getItemViewType(position: Int) = type

    fun entryClicks() = selection
    fun downloadClicks() = download
    fun removeClicks() = remove
    fun commentClicks() = comments


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
}

