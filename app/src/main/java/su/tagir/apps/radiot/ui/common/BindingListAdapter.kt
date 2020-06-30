package su.tagir.apps.radiot.ui.common

import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView

abstract class BindingListAdapter<T> : RecyclerView.Adapter<BindingViewHolder<T>>() {

    abstract val differ: AsyncListDiffer<T>

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: BindingViewHolder<T>, position: Int) {
        bind(holder, position)
    }

    protected abstract fun bind(viewHolder: BindingViewHolder<T>, position: Int)


    @MainThread
    fun replace(update: List<T>?) {
        differ.submitList(update)
    }

    val items: List<T>
        get() = differ.currentList
}