package su.tagir.apps.radiot.ui.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BindingViewHolder<in T>(view: View): RecyclerView.ViewHolder(view) {

    abstract fun bind(t:T?)
}