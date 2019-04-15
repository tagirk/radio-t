package su.tagir.apps.radiot.ui.search

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelper(private val callback: Callback): ItemTouchHelper.SimpleCallback(LEFT, LEFT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback.removeQuery(viewHolder.adapterPosition)
    }

    interface Callback{
        fun removeQuery(position: Int)
    }
}