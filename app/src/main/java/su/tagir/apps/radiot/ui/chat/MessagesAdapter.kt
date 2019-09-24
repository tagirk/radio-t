package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.entries.User
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.utils.longDateTimeFormat

class MessagesAdapter(private val glide: GlideRequests?,
                      private val callback: Callback) : DataBoundListAdapter<MessageFull>() {


    override val differ: AsyncListDiffer<MessageFull> = AsyncListDiffer(this, object: DiffUtil.ItemCallback<MessageFull>(){

        override fun areItemsTheSame(oldItem: MessageFull, newItem: MessageFull): Boolean = oldItem.message?.id == newItem.message?.id

        override fun areContentsTheSame(oldItem: MessageFull, newItem: MessageFull): Boolean = oldItem.message == newItem.message

    })

    override fun bind(viewHolder: DataBoundViewHolder<MessageFull>, position: Int) {
        viewHolder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<MessageFull> {
        val inflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(inflater.inflate(R.layout.item_chat_message, parent, false), glide, callback)
    }

    class MessageViewHolder(itemView: View,
                            private val glide: GlideRequests?,
                            private val callback: Callback) : DataBoundViewHolder<MessageFull>(itemView) {

        val avatar: ImageView = itemView.findViewById(R.id.avatar_image)

        val nicknameText: TextView = itemView.findViewById(R.id.nickname_text)

        val timeText: TextView = itemView.findViewById(R.id.time_text)

        val messageText: TextView = itemView.findViewById(R.id.message_text)

        private val cornerRadius = itemView.resources.getDimensionPixelSize(R.dimen.item_image_corner_radius)

        private var user: User? = null

        init {
            nicknameText.setOnClickListener { callback.onUserNameClick("@${user?.username}") }
            avatar.setOnClickListener { callback.onMentionClick(user?.username) }
        }

        @SuppressLint("SetTextI18n")
        override fun bind(t: MessageFull?) {
            user = if (t?.user?.isNotEmpty() == true) t.user?.get(0) else null
            timeText.text = t?.message?.sent?.longDateTimeFormat()
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


            Markwon.setMarkdown(messageText, spannableConfiguration, t?.message?.html ?: "")
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