package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.utils.NoCopySpannableFactory
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.entries.User
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.utils.longDateTimeFormat

class MessagesAdapter(private val glide: GlideRequests?,
                      private val callback: Callback,
                      private val linkMethod: LinkMovementMethod) : DataBoundListAdapter<MessageFull>() {


    override val differ: AsyncListDiffer<MessageFull> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<MessageFull>() {

        override fun areItemsTheSame(oldItem: MessageFull, newItem: MessageFull): Boolean = oldItem.message?.id == newItem.message?.id

        override fun areContentsTheSame(oldItem: MessageFull, newItem: MessageFull): Boolean = oldItem.message == newItem.message

    })

    override fun bind(viewHolder: DataBoundViewHolder<MessageFull>, position: Int) {
        viewHolder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<MessageFull> {
        val inflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(inflater.inflate(R.layout.item_chat_message, parent, false), glide, callback, linkMethod)
    }

    class MessageViewHolder(itemView: View,
                            private val glide: GlideRequests?,
                            private val callback: Callback,
                            linkMethod: LinkMovementMethod) : DataBoundViewHolder<MessageFull>(itemView) {

        val avatar: ImageView = itemView.findViewById(R.id.avatar_image)

        val nicknameText: TextView = itemView.findViewById(R.id.nickname_text)

        val timeText: TextView = itemView.findViewById(R.id.time_text)

        val messageText: TextView = itemView.findViewById(R.id.message_text)

        private val cornerRadius = itemView.resources.getDimensionPixelSize(R.dimen.item_image_corner_radius)

        private var user: User? = null

        private val markwon = Markwon.builder(itemView.context)
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(ImagesPlugin.create())
                .build()

        init {
            messageText.setSpannableFactory(NoCopySpannableFactory.getInstance()) //https://noties.io/Markwon/docs/v4/recipes.html#spannablefactory
            nicknameText.setOnClickListener { callback.onUserNameClick("@${user?.username}") }
            avatar.setOnClickListener { callback.onMentionClick(user?.username) }
            messageText.movementMethod = linkMethod
        }

        @SuppressLint("SetTextI18n")
        override fun bind(t: MessageFull?) {
            user = t?.user
            timeText.text = t?.message?.sent?.longDateTimeFormat()
            nicknameText.text = "${user?.displayName} @${user?.username}"
            glide
                    ?.load(user?.avatarUrlSmall)
                    ?.transform(RoundedCorners(cornerRadius))
                    ?.placeholder(R.drawable.ic_account_box_24dp)
                    ?.error(R.drawable.ic_account_box_24dp)
                    ?.into(avatar)

            val text = t?.message?.text ?: ""
            markwon.setMarkdown(messageText, text)
        }
    }


    interface Callback {
        fun onMentionClick(mention: String?)
        fun onUserNameClick(mention: String?)
    }
}