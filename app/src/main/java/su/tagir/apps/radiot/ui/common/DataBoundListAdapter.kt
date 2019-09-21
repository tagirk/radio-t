package su.tagir.apps.radiot.ui.common

import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class DataBoundListAdapter<T> : RecyclerView.Adapter<DataBoundViewHolder<T>>() {

    private val differ = AsyncListDiffer<T>(this, object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = this@DataBoundListAdapter.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = this@DataBoundListAdapter.areContentsTheSame(oldItem, newItem)

    })

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int) {
        bind(holder, position)
    }

    protected abstract fun bind(viewHolder: DataBoundViewHolder<T>, position: Int)

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    @MainThread
    fun replace(update: List<T>?) {
        differ.submitList(update)
    }

    val items: List<T>
        get() = differ.currentList
}