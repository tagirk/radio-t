package su.tagir.apps.radiot.ui.comments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.appbar.AppBarLayout
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.utils.NoCopySpannableFactory
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.ui.FragmentsInteractionListener
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.utils.BetterLinkMovementMethod
import su.tagir.apps.radiot.utils.visibleGone

class CommentsFragment : BaseMvpListFragment<Node, CommentsContract.View, CommentsContract.Presenter>(),
        CommentsContract.View,
        Toolbar.OnMenuItemClickListener{

    private var interactionListener: FragmentsInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = context as FragmentsInteractionListener
    }

    override fun onDetach() {
        interactionListener = null
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        interactionListener?.lockDrawer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<AppBarLayout>(R.id.app_bar).visibility = View.VISIBLE
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_comment)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbar.setTitle(R.string.comments)
        toolbar.subtitle = arguments?.getString("title")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add -> AddCommentFragment.newInstance(null).show(childFragmentManager, "add_comment")
        }
        return false
    }

    override fun createPresenter(): CommentsContract.Presenter {
        val postUrl = arguments!!.getString("url")!!
        val appComponent: AppComponent = (activity!!.application as App).appComponent
        return CommentsPresenter(postUrl, appComponent.commentsRepository, appComponent.router)
    }


    override fun createAdapter(): DataBoundListAdapter<Node> =
            CommentsAdapter(
                    object : CommentsAdapter.Callback {
                        override fun expand(position: Int, node: Node) {
                            presenter.showReplies(position, node)
                        }

                        override fun hide(position: Int, node: Node) {
                            presenter.hideReplies(position, node)
                        }

                    },
                    BetterLinkMovementMethod
                            .linkify(Linkify.WEB_URLS, activity)
                            .setOnLinkClickListener { _, url ->
                                presenter.openUrl(url)
                                true
                            })

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

    class CommentsAdapter(private val callback: Callback,
                          private val linkMethod: LinkMovementMethod) : DataBoundListAdapter<Node>() {

        override val differ: AsyncListDiffer<Node> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Node>() {

            override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean =
                    oldItem.comment.id == newItem.comment.id

            override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean =
                    oldItem == newItem

        })

        override fun bind(viewHolder: DataBoundViewHolder<Node>, position: Int) {
            viewHolder.bind(items[position])
            (viewHolder as CommentViewHolder).expand.setOnClickListener {
                val node = items[viewHolder.adapterPosition]
                if (node.expanded) {
                    callback.hide(viewHolder.adapterPosition, node)
                    return@setOnClickListener
                }
                callback.expand(viewHolder.adapterPosition, node)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(v, linkMethod)
        }

        interface Callback {
            fun expand(position: Int, node: Node)
            fun hide(position: Int, node: Node)
        }
    }

    class CommentViewHolder(view: View, linkMethod: LinkMovementMethod) : DataBoundViewHolder<Node>(view) {

        private val avatar: ImageView = itemView.findViewById(R.id.avatar)

        private val name: TextView = itemView.findViewById(R.id.name)

        private val date: TextView = itemView.findViewById(R.id.date)

        private val comment: TextView = itemView.findViewById(R.id.comment)

        val expand: TextView = itemView.findViewById(R.id.expand)

        private val votes: TextView = itemView.findViewById(R.id.votes)

        private val padding: Int = itemView.resources.getDimensionPixelSize(R.dimen.comment_padding)

        private val markwon = Markwon.builder(itemView.context)
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(ImagesPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build()

        init {
            comment.setSpannableFactory(NoCopySpannableFactory.getInstance()) //https://noties.io/Markwon/docs/v4/recipes.html#spannablefactory
            comment.movementMethod = linkMethod
        }

        @SuppressLint("SetTextI18n")
        override fun bind(t: Node?) {
            itemView.setPadding(((t?.level?.plus(1))
                    ?: 1) * padding, padding / 4, padding, padding / 4)
            expand.visibleGone(t?.replies != null && t.replies.isNotEmpty())
            expand.text = if (t?.expanded == true) "----------Скрыть ответы----------" else "----------Показать ответы(${t?.replies?.size
                    ?: 0})----------"

            t?.comment?.user?.picture?.let{url ->
                val config = ImageConfig(placeholder = R.drawable.ic_account_box_24dp, error = R.drawable.ic_account_box_24dp)
                ImageLoader.display(url, avatar, config)
            }

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

            val span = markwon.toMarkdown(t?.comment?.text ?: "")
            comment.text = span
        }

    }
}