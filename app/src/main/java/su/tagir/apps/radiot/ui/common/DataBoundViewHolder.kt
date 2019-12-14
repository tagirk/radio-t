package su.tagir.apps.radiot.ui.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class DataBoundViewHolder<in T>(view: View): RecyclerView.ViewHolder(view) {

    abstract fun bind(t:T?)
}