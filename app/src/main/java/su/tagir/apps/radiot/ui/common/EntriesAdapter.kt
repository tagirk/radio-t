package su.tagir.apps.radiot.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry

class EntriesAdapter(private val actionHandler: Callback) :
        BindingListAdapter<Entry>() {

    override val differ: AsyncListDiffer<Entry> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    })

    override fun bind(viewHolder: BindingViewHolder<Entry>, position: Int) {
        when (viewHolder) {
            is PodcastViewHolder -> viewHolder.bind(items[position])
            is PrepViewHolder -> viewHolder.bind(items[position])
            is NewsViewHolder -> viewHolder.bind(items[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<Entry> {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_prep, parent, false)
                val holder = NewsViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.select(entry)
                }
                holder.binding.comments.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.openComments(entry)
                }
                return holder
            }
            TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_prep, parent, false)
                val holder = PrepViewHolder(view)
                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.openComments(entry)
                }
                return holder
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                val holder = PodcastViewHolder(view, isPirates = viewType == TYPE_PIRATES)

                holder.itemView.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.select(entry)
                }
                holder.binding.btnRemove.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.remove(entry)
                }
                holder.binding.cancel.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.remove(entry)
                }
                holder.binding.btnDownload.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.download(entry)
                }
                holder.binding.comments.setOnClickListener {
                    val entry = items[holder.adapterPosition]
                    actionHandler.openComments(entry)
                }
                return holder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.categories?.firstOrNull()) {
            "prep" -> TYPE_PREP
            "news","info" -> TYPE_NEWS
            "pirates" -> TYPE_PIRATES
            else -> TYPE_PODCAST
        }

    }

    companion object {

        const val TYPE_PREP = 1
        const val TYPE_PODCAST = 2
        const val TYPE_NEWS = 3
        const val TYPE_PIRATES = 4

    }

    interface Callback {
        fun select(entry: Entry)
        fun download(entry: Entry)
        fun remove(entry: Entry)
        fun openComments(entry: Entry)
    }
}

