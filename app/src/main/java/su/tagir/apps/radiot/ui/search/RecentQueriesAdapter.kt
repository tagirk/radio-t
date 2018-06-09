package su.tagir.apps.radiot.ui.search

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import su.tagir.apps.radiot.R


class RecentQueriesAdapter(private val callback: Callback) : PagedListAdapter<String, QueryViewHolder>(diffCallback) {


    override fun onBindViewHolder(holder: QueryViewHolder, position: Int) {
       val item = getItem(position)
        holder.bind(item)
        holder.itemView?.setOnClickListener { callback.onQueryClick(item) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_resent_query, parent, false)
        return QueryViewHolder(view)
    }

    interface Callback{
        fun onQueryClick(query: String?)
    }

    companion object {


        private val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        }
    }
}

class QueryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @BindView(R.id.query)
    lateinit var query: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(query: String?) {
        this.query.text = query
    }
}