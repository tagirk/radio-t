package su.tagir.apps.radiot.ui.comments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.model.repository.CommentsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject

class CommentsFragment : BaseMvpListFragment<Node, CommentsContract.View, CommentsContract.Presenter>(), CommentsContract.View,
        Injectable {

    @Inject
    lateinit var commentsRepository: CommentsRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_comment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> AddCommentFragment.newInstance(null).show(childFragmentManager, "add_comment")
        }
        return super.onOptionsItemSelected(item)
    }


    override fun createPresenter(): CommentsContract.Presenter {
        val postUrl = arguments!!.getString("url")!!
        return CommentsPresenter(postUrl, commentsRepository, scheduler)
    }


    override fun createAdapter(): DataBoundListAdapter<Node> =
            CommentsAdapter(GlideApp.with(this), object : CommentsAdapter.Callback {
                override fun expand(position: Int, node: Node) {
                    presenter.showReplies(position, node)
                }

                override fun hide(position: Int, node: Node) {
                    presenter.hideReplies(position, node)
                }
            })

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_entry_list, container, false)

    companion object {

        fun newInstance(entry: Entry): CommentsFragment {
            val args = Bundle()
            args.putString("url", entry.url)
            args.putString("title", entry.title)
            val f = CommentsFragment()
            f.arguments = args
            return f
        }
    }

    class CommentsAdapter(private val glideRequests: GlideRequests?,
                          private val callback: Callback) : DataBoundListAdapter<Node>() {
        override fun bind(viewHolder: DataBoundViewHolder<Node>, position: Int) {
            viewHolder.bind(items[position])
            (viewHolder as CommentViewHolder).itemView.setOnClickListener {
                val node = items[viewHolder.adapterPosition]
                if (node.expanded) {
                    callback.hide(viewHolder.adapterPosition, node)
                    return@setOnClickListener
                }
                callback.expand(viewHolder.adapterPosition, node)
            }
        }

        override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean =
                oldItem.comment.id == newItem.comment.id

        override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean =
                oldItem == newItem

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(v, glideRequests)
        }

        interface Callback {
            fun expand(position: Int, node: Node)
            fun hide(position: Int, node: Node)
        }

    }

    class CommentViewHolder(view: View,
                            private val glideRequests: GlideRequests?) : DataBoundViewHolder<Node>(view) {

        private val avatar: ImageView = itemView.findViewById(R.id.avatar)

        private val name: TextView = itemView.findViewById(R.id.name)

        private val date: TextView = itemView.findViewById(R.id.date)

        private val comment: TextView = itemView.findViewById(R.id.date)

        private val expand: TextView = itemView.findViewById(R.id.expand)

        private val votes: TextView = itemView.findViewById(R.id.votes)

        private val padding: Int = itemView.resources.getDimensionPixelSize(R.dimen.comment_padding)


        @SuppressLint("SetTextI18n")
        override fun bind(t: Node?) {
            itemView.setPadding(((t?.level?.plus(1))
                    ?: 1) * padding, padding / 4, padding, padding / 4)
            expand.visibleGone(t?.replies != null && t.replies.isNotEmpty())
            expand.text = if (t?.expanded == true) "----------Скрыть ответы----------" else "----------Показать ответы(${t?.replies?.size
                    ?: 0})----------"
            glideRequests
                    ?.load(t?.comment?.user?.picture)
                    ?.transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.item_image_corner_radius)))
                    ?.placeholder(R.drawable.ic_account_box_24dp)
                    ?.error(R.drawable.ic_account_box_24dp)
                    ?.into(avatar)

            name.text = t?.comment?.user?.name
            date.text = DateUtils.formatDateTime(itemView.context, t?.comment?.time?.time
                    ?: 0, DateUtils.FORMAT_SHOW_DATE xor DateUtils.FORMAT_SHOW_TIME)

            val score = t?.comment?.score ?: 0
            votes.text = if (score > 0) "+$score" else "$score"
            when {
                score > 0 -> votes.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPositiveComment))
                score < 0 -> votes.setTextColor(Color.RED)
                else -> votes.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorSecondaryText))
            }

            val spannableConfiguration = SpannableConfiguration.builder(itemView.context)
//                    .linkResolver { _, link -> callback.onUrlClick(link) }
                    .build()


            Markwon.setMarkdown(comment, spannableConfiguration, t?.comment?.text ?: "")
        }

    }
}