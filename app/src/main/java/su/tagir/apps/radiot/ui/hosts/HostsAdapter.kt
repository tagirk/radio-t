package su.tagir.apps.radiot.ui.hosts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindDimen
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Host
import su.tagir.apps.radiot.utils.visibleGone

class HostsAdapter(private val glide: GlideRequests?, private val callback: Callback) : PagedListAdapter<Host, HostViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_host, parent, false)
        return HostViewHolder(view, glide, callback)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Host>() {
            override fun areItemsTheSame(oldItem: Host, newItem: Host) = oldItem.nickname == newItem.nickname

            override fun areContentsTheSame(oldItem: Host, newItem: Host) = oldItem == newItem

        }
    }

    interface Callback {

        fun onSocialNetClick(url: String)

    }
}

class HostViewHolder(itemView: View,
                     private val glide: GlideRequests?,
                     private val callback: HostsAdapter.Callback) : RecyclerView.ViewHolder(itemView) {

    @BindView(R.id.avatar)
    lateinit var avatar: ImageView

    @BindView(R.id.name)
    lateinit var name: TextView

    @BindView(R.id.lurk)
    lateinit var lurk: View

    @BindView(R.id.twitter)
    lateinit var twitter: View

    @BindView(R.id.instagram)
    lateinit var instagram: View

    @JvmField
    @BindDimen(R.dimen.item_image_corner_radius)
    var imageRadius: Int = 0

    private var host: Host? = null

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(host: Host?) {
        glide
                ?.load(host?.avatar)
                ?.diskCacheStrategy(DiskCacheStrategy.ALL)
                ?.transforms(CenterCrop(),RoundedCorners(imageRadius))
                ?.placeholder(R.drawable.ic_account_box_24dp)
                ?.error(R.drawable.ic_account_box_24dp)
                ?.into(avatar)
        name.text = host?.nickname
        lurk.visibleGone(!host?.lurk.isNullOrBlank())
        twitter.visibleGone(!host?.twitter.isNullOrBlank())
        instagram.visibleGone(!host?.instagram.isNullOrBlank())
        this.host = host

    }

    @OnClick(R.id.lurk)
    fun onLurkClick() {
        if (host?.lurk != null) {
            callback.onSocialNetClick(host!!.lurk!!)
        }
    }

    @OnClick(R.id.twitter)
    fun onTwitterClick() {
        if (host?.twitter != null) {
            callback.onSocialNetClick(host!!.twitter!!)
        }
    }

    @OnClick(R.id.instagram)
    fun onInstagramClick() {
        if (host?.instagram != null) {
            callback.onSocialNetClick(host!!.instagram!!)
        }
    }
}