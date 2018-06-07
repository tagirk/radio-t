package su.tagir.apps.radiot.ui.player

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.utils.convertMillis

class TimeLabelsAdapter(private var items: List<TimeLabel>,
                        private val callback:Callback) : RecyclerView.Adapter<TimeLabelViewHolder>() {

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TimeLabelViewHolder, position: Int) {
        val timeLabel = items[position]
        holder.bind(timeLabel)
        holder.itemView?.setOnClickListener { callback.onItemClick(timeLabel) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLabelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_time_label, parent, false)
        return TimeLabelViewHolder(view)
    }

    fun update(items: List<TimeLabel>?) {
        this.items = items ?: emptyList()
        notifyDataSetChanged()
    }

    interface Callback {
        fun onItemClick(item: TimeLabel)
    }
}

class TimeLabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    @BindView(R.id.title)
    lateinit var title: TextView

    init {
        ButterKnife.bind(this, view)
    }

    @SuppressLint("SetTextI18n")
    fun bind(timeLabel: TimeLabel) {
        title.text = "${timeLabel.topic} - ${timeLabel.time?.convertMillis()}"
    }
}
