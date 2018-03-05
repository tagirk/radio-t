package su.tagir.apps.radiot.ui.search

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.NewsViewHolder
import su.tagir.apps.radiot.ui.common.PodcastViewHolder
import su.tagir.apps.radiot.ui.common.PrepViewHolder


class SearchAdapter(private var items: List<Entry>?, private val glide: GlideRequests?, private val callback: EntriesAdapter.Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataVersion = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EntriesAdapter.TYPE_PODCAST -> {
                val view = inflater.inflate(R.layout.item_podcast, parent, false)
                PodcastViewHolder(view, glide, callback)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = items?.get(position) ?: return
        when (holder) {
            is PodcastViewHolder -> holder.bind(item)
            is PrepViewHolder -> holder.bind(item)
            is NewsViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount() = items?.size ?: 0

    override fun getItemViewType(position: Int): Int {
        val categories = items?.get(position)?.categories
        if (categories?.contains("podcast") == true){
            return EntriesAdapter.TYPE_PODCAST
        }else if(categories?.contains("prep") == true){
            return EntriesAdapter.TYPE_PREP
        }
        return EntriesAdapter.TYPE_NEWS
    }

    @MainThread
    @SuppressLint("StaticFieldLeak")
    fun replace(update: List<Entry>?) {
        dataVersion++
        when {
            items == null -> {
                if (update == null) {
                    return
                }
                items = update
                notifyDataSetChanged()
            }
            update == null -> {
                val oldSize = items?.size ?: 0
                notifyItemRangeRemoved(0, oldSize)
            }
            else -> {
                val startVersion = dataVersion
                val oldItems = items

                object : AsyncTask<Void, Void, DiffUtil.DiffResult>() {
                    override fun doInBackground(vararg params: Void?): DiffUtil.DiffResult {
                        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                                    update[newItemPosition].url == oldItems?.get(oldItemPosition)?.url

                            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                                    update[newItemPosition] == oldItems?.get(oldItemPosition)


                            override fun getOldListSize() = oldItems?.size ?: 0
                            override fun getNewListSize() = update.size
                        })
                    }

                    override fun onPostExecute(result: DiffUtil.DiffResult) {
                        if (startVersion != dataVersion) {
                            return
                        }
                        items = update
                        result.dispatchUpdatesTo(this@SearchAdapter)
                    }
                }.execute()
            }
        }
    }
}

