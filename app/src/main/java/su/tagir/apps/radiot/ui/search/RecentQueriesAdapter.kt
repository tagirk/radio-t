package su.tagir.apps.radiot.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.common.BindingListAdapter
import su.tagir.apps.radiot.ui.common.BindingViewHolder

class RecentQueriesAdapter(private val callback: Callback) : BindingListAdapter<String>() {


    override val differ: AsyncListDiffer<String> = AsyncListDiffer(this, object: DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    })


    override fun bind(viewHolder: BindingViewHolder<String>, position: Int) {
        val item = items[position]
        viewHolder.bind(item)
        viewHolder.itemView.setOnClickListener { callback.onQueryClick(item) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<String> {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_resent_query, parent, false)
        return QueryViewHolder(view)
    }

    interface Callback{
        fun onQueryClick(query: String?)
    }

}

class QueryViewHolder(itemView: View) :BindingViewHolder<String>(itemView) {

    private val query: TextView = itemView.findViewById(R.id.query)

    override fun bind(t: String?) {
        this.query.text = t
    }
}