package su.tagir.apps.radiot.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.*


class SearchAdapter(private val glide: RequestManager, private val callback: EntriesAdapter.Callback) : DataBoundListAdapter<Entry>() {

    override val differ: AsyncListDiffer<Entry> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Entry>(){
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    })

    override fun bind(viewHolder: DataBoundViewHolder<Entry>, position: Int) {
        viewHolder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<Entry> {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: DataBoundViewHolder<Entry>
        viewHolder = when (viewType) {
            EntriesAdapter.TYPE_NEWS -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                NewsViewHolder(view)
            }
            EntriesAdapter.TYPE_PREP -> {
                val view = inflater.inflate(R.layout.item_entry, parent, false)
                PrepViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, glide)
            }
        }

            viewHolder.itemView.setOnClickListener {
                val entry = items[viewHolder.adapterPosition]
                callback.select(entry)
            }
            if(viewHolder is PodcastViewHolder){
                viewHolder.remove.setOnClickListener {
                    val entry = items[viewHolder.adapterPosition]
                    callback.remove(entry) }
                viewHolder.cancel.setOnClickListener {
                    val entry = items[viewHolder.adapterPosition]
                    callback.remove(entry)
                }
                viewHolder.download.setOnClickListener {
                    val entry = items[viewHolder.adapterPosition]
                    callback.download(entry)
                }
                viewHolder.comments.setOnClickListener {
                    val entry = items[viewHolder.adapterPosition]
                    callback.openComments(entry)
                }
            }

        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        val categories = items[position].categories
        if (categories?.contains("podcast") == true) {
            return EntriesAdapter.TYPE_PODCAST
        } else if (categories?.contains("prep") == true) {
            return EntriesAdapter.TYPE_PREP
        }
        return EntriesAdapter.TYPE_NEWS
    }
}

