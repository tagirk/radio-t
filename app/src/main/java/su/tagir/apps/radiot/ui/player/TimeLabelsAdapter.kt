package su.tagir.apps.radiot.ui.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.utils.convertMillis

class TimeLabelsAdapter(private var items: List<TimeLabel>) : RecyclerView.Adapter<TimeLabelViewHolder>() {

    private val clicks = PublishRelay.create<TimeLabel>()

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TimeLabelViewHolder, position: Int) {
        val timeLabel = items[position]
        holder.bind(timeLabel)
        holder.itemView.setOnClickListener { clicks.accept(items[holder.adapterPosition]) }
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

    fun labels(): Observable<TimeLabel> = clicks
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
