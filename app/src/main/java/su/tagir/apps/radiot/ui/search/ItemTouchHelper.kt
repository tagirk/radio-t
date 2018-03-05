package su.tagir.apps.radiot.ui.search

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.LEFT

class ItemTouchHelper(private val callback: Callback): ItemTouchHelper.SimpleCallback(LEFT, LEFT) {

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback.removeQuery(viewHolder.adapterPosition)
    }

    interface Callback{
        fun removeQuery(position: Int)
    }
}