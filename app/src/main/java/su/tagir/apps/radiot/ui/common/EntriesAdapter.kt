package su.tagir.apps.radiot.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val type: Int, private val actionHandler: Callback) :
       DataBoundListAdapter<Entry>() {

    override val differ: AsyncListDiffer<Entry> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Entry>(){
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    })

    override fun bind(viewHolder: DataBoundViewHolder<Entry>, position: Int) {
        when (viewHolder) {
            is PodcastViewHolder -> viewHolder.bind(items[position])
            is PrepViewHolder -> viewHolder.bind(items[position])
            is NewsViewHolder -> viewHolder.bind(items[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):DataBoundViewHolder<Entry> {
        val inflater = LayoutInflater.from(parent.context)
         when (viewType) {
            TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                val holder =  NewsViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.select(entry)
                }
                return holder
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                val holder =  PrepViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.select(entry)
                }
                return holder
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                val holder = PodcastViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.select(entry)
                }
                holder.remove.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.remove(entry)
                }
                holder.cancel.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.remove(entry)
                }
                holder.download.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.download(entry)
                }
                holder.comments.setOnClickListener {
                    val entry = items[holder.adapterPosition]
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

    }

    interface Callback{
        fun select(entry: Entry)
        fun download(entry: Entry)
        fun remove(entry: Entry)
        fun openComments(entry: Entry)
    }
}

