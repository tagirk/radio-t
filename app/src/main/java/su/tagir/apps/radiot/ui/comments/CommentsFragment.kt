package su.tagir.apps.radiot.ui.comments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.GlideRequests
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.ui.common.ListFragment
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject

class CommentsFragment : ListFragment<Node>(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var commentsViewModel: CommentsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_comment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add -> AddCommentFragment.newInstance(null).show(childFragmentManager, "add_comment")
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(arguments?.getString("title") ?: Screens.COMMENTS_SCREEN)
    }

    override fun createViewModel(): ListViewModel<Node> {
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
        commentsViewModel = ViewModelProviders.of(this, viewModelFactory).get(CommentsViewModel::class.java)
        commentsViewModel.setUrl(arguments?.getString("url"))
        return commentsViewModel
    }

    override fun createAdapter(): DataBoundListAdapter<Node> =
            CommentsAdapter(GlideApp.with(this), object : CommentsAdapter.Callback {
                override fun expand(position: Int, node: Node) {
                    commentsViewModel.showReplies(position, node)
                }

                override fun hide(position: Int, node: Node) {
                    commentsViewModel.hideReplies(position, node)
                }
            })

    override val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(context)

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
            viewHolder.bind(items?.get(position))
            (viewHolder as CommentViewHolder).itemView.setOnClickListener {
                val node = items?.get(viewHolder.adapterPosition) ?: return@setOnClickListener
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

        @BindView(R.id.avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.name)
        lateinit var name: TextView

        @BindView(R.id.date)
        lateinit var date: TextView

        @BindView(R.id.comment)
        lateinit var comment: TextView

        @BindView(R.id.expand)
        lateinit var expand: TextView

        @BindView(R.id.votes)
        lateinit var votes: TextView

        @JvmField
        @BindDimen(R.dimen.item_image_corner_radius)
        var cornerRadius: Int = 0

        @JvmField
        @BindDimen(R.dimen.comment_padding)
        var padding: Int = 0

        @JvmField
        @BindColor(R.color.colorPositiveComment)
        var colorPositiveComment: Int = 0

        @JvmField
        @BindColor(R.color.colorSecondaryText)
        var colorSecondaryText: Int = 0

        init {
            ButterKnife.bind(this, itemView)
        }

        @SuppressLint("SetTextI18n")
        override fun bind(t: Node?) {
            itemView.setPadding(((t?.level?.plus(1))
                    ?: 1) * padding, padding / 4, padding, padding / 4)
            expand.visibleGone(t?.replies != null && t.replies.isNotEmpty())
            expand.text = if (t?.expanded == true) "----------Скрыть ответы----------" else "----------Показать ответы(${t?.replies?.size
                    ?: 0})----------"
            glideRequests
                    ?.load(t?.comment?.user?.picture)
                    ?.transform(RoundedCorners(cornerRadius))
                    ?.placeholder(R.drawable.ic_account_box_24dp)
                    ?.error(R.drawable.ic_account_box_24dp)
                    ?.into(avatar)

            name.text = t?.comment?.user?.name
            date.text = DateUtils.formatDateTime(itemView.context, t?.comment?.time?.time
                    ?: 0, DateUtils.FORMAT_SHOW_DATE xor DateUtils.FORMAT_SHOW_TIME)

            val score = t?.comment?.score ?: 0
            votes.text = if (score > 0) "+$score" else "$score"
            when{
                score>0->votes.setTextColor(colorPositiveComment)
                score<0-> votes.setTextColor(Color.RED)
                else -> votes.setTextColor(colorSecondaryText)
            }

            val spannableConfiguration = SpannableConfiguration.builder(itemView.context)
//                    .linkResolver { _, link -> callback.onUrlClick(link) }
                    .build()


            Markwon.setMarkdown(comment, spannableConfiguration, t?.comment?.text ?: "")
        }

    }
}