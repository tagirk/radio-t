package su.tagir.apps.radiot.ui.stream

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedListAdapter
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.*
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.ui.common.PagedListFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import su.tagir.apps.radiot.utils.shortDateFormat
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject

class StreamFragment : PagedListFragment<Article>(), Injectable {

    @Inject
    internal lateinit var router: Router

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.timer)
    internal lateinit var timer: TextView

    @BindView(R.id.show_timer)
    lateinit var showTimer: ImageView

    @BindView(R.id.hide_timer)
    lateinit var hideTimer: ImageView

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var streamViewModel: StreamViewModel


    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_stream, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        streamViewModel = listViewModel as StreamViewModel
        playerViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(PlayerViewModel::class.java)
        observeViewModel()

    }

    override fun createViewModel() = ViewModelProviders.of(activity!!, viewModelFactory).get(StreamViewModel::class.java)

    override fun createAdapter() = ArticlesAdapter(object : ArticlesAdapter.Callback {
        override fun onArticleClick(article: Article?) {
            playerViewModel.onArticleClick(article)
        }
    })

    override fun onResume() {
        super.onResume()
        initTimer()
        streamViewModel.updateActiveTheme()
    }

    override fun onPause() {
        super.onPause()
        streamViewModel.dispose()
    }

    @OnClick(R.id.timer_layout)
    fun showHideTimer() {
        if (timer.visibility == View.GONE) {
            streamViewModel.showTimer()
        } else {
            streamViewModel.hideTimer()
        }
    }

    private fun initTimer() {
        streamViewModel
                .showTimer
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            if (it == null) {
                                return@Observer
                            }
                            timer.visibleGone(it)
                            hideTimer.visibleGone(it)
                            showTimer.visibleGone(!it)
                        })
        streamViewModel.startTimer()
    }

    private fun observeViewModel() {
        streamViewModel
                .timer
                .observe(getViewLifecycleOwner()!!, Observer { timer.text = it })
    }


   class ArticlesAdapter(private val callback: Callback) : PagedListAdapter<Article, ArticleViewHolder>(ArticleDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false))
        }

        override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
            holder.bind(getItem(position))
            holder.itemView?.setOnClickListener { callback.onArticleClick(getItem(position)) }

        }

        interface Callback {
            fun onArticleClick(article: Article?)
        }

    }

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.snippet)
        lateinit var snippet: TextView

        @BindView(R.id.root)
        lateinit var root: ConstraintLayout

        @JvmField
        @BindColor((R.color.colorActiveArticle))
        var activeColor: Int = 0

        @JvmField
        @BindDimen(R.dimen.item_image_corner_radius)
        var cornerRadius: Int = 0

        @JvmField
        @BindColor(R.color.colorPrimaryText)
        var primaryTextColor: Int = 0

        @JvmField
        @BindColor(R.color.colorPrimaryDark)
        var accentColor: Int = 0

        init {
            ButterKnife.bind(this, view)
        }

        internal fun bind(article: Article?) {
            title.text = article?.title

            val date = article?.addedDate?.shortDateFormat()
            val sb = SpannableStringBuilder()
                    .append(article?.domain)
                    .append(", ")
                    .append(date)
                    .append(" - ")
                    .append(article?.snippet?.replace("\n", ""))

            val domainLength = article?.domain?.length?.plus(2) ?: 0
            sb.setSpan(UnderlineSpan(), 0, if (domainLength >= 2) domainLength - 2 else domainLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(ForegroundColorSpan(accentColor), 0, if (domainLength >= 2) domainLength - 2 else domainLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(ForegroundColorSpan(primaryTextColor), domainLength,
                    domainLength.plus(date?.length?.plus(3) ?: 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            snippet.text = sb
            if (article?.active == true) {
                root.setBackgroundColor(activeColor)
            } else {
                root.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem

        override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.slug == newItem.slug

    }
}