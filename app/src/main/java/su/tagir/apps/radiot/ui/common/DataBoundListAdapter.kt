package su.tagir.apps.radiot.ui.common

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView

abstract class DataBoundListAdapter<T> : RecyclerView.Adapter<DataBoundViewHolder<T>>() {

    var items: List<T>? = null
        private set

    private var dataVersion = 0

    override fun getItemCount() = items?.size ?: 0

    override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int) {
        bind(holder, position)
    }

    protected abstract fun bind(viewHolder: DataBoundViewHolder<T>, position: Int)

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean


    @SuppressLint("StaticFieldLeak")
    @MainThread
    fun replace(update: List<T>?) {
        dataVersion++
        if (items == null || items!!.isEmpty()) {
            if (update == null) {
                return
            }
            items = update
            notifyDataSetChanged()
        } else if (update == null) {
            val oldSize = items!!.size
            items = null
            notifyItemRangeRemoved(0, oldSize)
        } else {
            val startVersion = dataVersion
            val oldItems = items!!
            object : AsyncTask<Void, Void, DiffUtil.DiffResult>() {
                override fun doInBackground(vararg voids: Void): DiffUtil.DiffResult {
                    return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun getOldListSize(): Int {
                            return oldItems.size
                        }

                        override fun getNewListSize(): Int {
                            return update.size
                        }

                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            val oldItem = oldItems.get(oldItemPosition)
                            val newItem = update[newItemPosition]
                            return this@DataBoundListAdapter.areItemsTheSame(oldItem, newItem)
                        }

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            val oldItem = oldItems.get(oldItemPosition)
                            val newItem = update[newItemPosition]
                            return this@DataBoundListAdapter.areContentsTheSame(oldItem, newItem)
                        }
                    })
                }

                override fun onPostExecute(diffResult: DiffUtil.DiffResult) {
                    if (startVersion != dataVersion) {
                        return
                    }
                    items = update
                    diffResult.dispatchUpdatesTo(this@DataBoundListAdapter)

                }
            }.execute()
        }
    }
}