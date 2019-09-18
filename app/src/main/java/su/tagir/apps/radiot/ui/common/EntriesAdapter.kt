package su.tagir.apps.radiot.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val type: Int, private val glide: RequestManager, private val actionHandler: Callback) :
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
         when (viewType) {
            TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                val holder =  NewsViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.select(entry)
                }
                return holder
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                val holder =  PrepViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.select(entry)
                }
                return holder
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                val holder = PodcastViewHolder(view, glide)
                holder.itemView.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.select(entry)
                }
                holder.remove.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.remove(entry)
                }
                holder.cancel.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.remove(entry)
                }
                holder.download.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.download(entry)
                }
                holder.comments.setOnClickListener {
                    val entry = getItem(holder.adapterPosition) ?: return@setOnClickListener
                    actionHandler.openComments(entry)
                }
                return holder
            }
        }
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

    interface Callback{
        fun select(entry: Entry)
        fun download(entry: Entry)
        fun remove(entry: Entry)
        fun openComments(entry: Entry)
    }
}

