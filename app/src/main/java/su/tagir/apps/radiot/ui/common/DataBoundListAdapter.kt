package su.tagir.apps.radiot.ui.common

import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView

abstract class DataBoundListAdapter<T> : RecyclerView.Adapter<DataBoundViewHolder<T>>() {

    abstract val differ: AsyncListDiffer<T>

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int) {
        bind(holder, position)
    }

    protected abstract fun bind(viewHolder: DataBoundViewHolder<T>, position: Int)


    @MainThread
    fun replace(update: List<T>?) {
        differ.submitList(update)
    }

    val items: List<T>
        get() = differ.currentList
}