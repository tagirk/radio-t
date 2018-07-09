package su.tagir.apps.radiot.ui.common

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class DataBoundViewHolder<in T>(view: View): RecyclerView.ViewHolder(view) {

    abstract fun bind(t:T?)
}