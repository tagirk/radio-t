package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.entries.User
import su.tagir.apps.radiot.utils.longDateTimeFormat


class MessagesAdapter(private val glide: GlideRequests?,
                      private val callback: Callback) : PagedListAdapter<MessageFull, MessagesAdapter.MessageViewHolder>(MessagesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(inflater.inflate(R.layout.item_chat_message, parent, false), glide, callback)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class MessageViewHolder(itemView: View,
                            private val glide: GlideRequests?,
                            private val callback: Callback) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.avatar_image)
        lateinit var avatar: ImageView


        @BindView(R.id.nickname_text)
        lateinit var nicknameText: TextView

        @BindView(R.id.time_text)
        lateinit var timeText: TextView

        @BindView(R.id.message_text)
        lateinit var messageText: TextView

        @JvmField
        @BindDimen(R.dimen.item_image_corner_radius)
        var cornerRadius: Int = 0

        private var user: User? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        @SuppressLint("SetTextI18n")
        fun bind(message: MessageFull?) {
            user = if (message?.user?.isNotEmpty() == true) message.user?.get(0) else null
            timeText.text = message?.message?.sent?.longDateTimeFormat()
            nicknameText.text = "${user?.displayName} @${user?.username}"
            glide
                    ?.load(user?.avatarUrlSmall)
                    ?.transform(RoundedCorners(cornerRadius))
                    ?.placeholder(R.drawable.ic_account_box_24dp)
                    ?.error(R.drawable.ic_account_box_24dp)
                    ?.into(avatar)


            val spannableConfiguration = SpannableConfiguration.builder(itemView.context)
                    .linkResolver { _, link -> callback.onUrlClick(link) }
                    .build()


            Markwon.setMarkdown(messageText, spannableConfiguration, message?.message?.html ?: "")
        }

        @OnClick(R.id.nickname_text)
        fun onNickClick() {
            callback.onUserNameClick("@${user?.username}")
        }

        @OnClick(R.id.avatar_image)
        fun onAvatarClick() {
            callback.onMentionClick(user?.username)
        }
    }

    class MessagesDiffCallback : DiffUtil.ItemCallback<MessageFull>() {

        override fun areItemsTheSame(oldItem: MessageFull, newItem: MessageFull): Boolean {
            return oldItem.message?.id == newItem.message?.id
        }

        override fun areContentsTheSame(oldItem: MessageFull, newItem: MessageFull) = oldItem.message == newItem.message
    }

    interface Callback {
        fun onMentionClick(mention: String?)
        fun onUrlClick(url: String?)
        fun onUserNameClick(mention: String?)
    }
}