package su.tagir.apps.radiot.ui.common

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class DataBoundListAdapter<T> : RecyclerView.Adapter<DataBoundViewHolder<T>>() {

    private val differ = AsyncListDiffer<T>(this, object : DiffUtil.ItemCallback<T>(){
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean  =
                this@DataBoundListAdapter.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
                this@DataBoundListAdapter.areContentsTheSame(oldItem, newItem)
    })

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int) {
        holder.bind(differ.currentList[position])
    }

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean


    @SuppressLint("StaticFieldLeak")
    @MainThread
    fun replace(update: List<T>?) {
        differ.submitList(update)
    }

    fun getItems(): List<T> = differ.currentList

    fun getItem(position: Int): T = getItems()[position]
}