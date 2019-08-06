package su.tagir.apps.radiot.ui.news

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import su.tagir.apps.radiot.utils.shortDateFormat
import javax.inject.Inject

class ArticlesFragment : BaseMvpPagedListFragment<Article, ArticlesContract.View, ArticlesContract.Presenter>(), ArticlesContract.View, Injectable {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var scheduler:BaseSchedulerProvider

    @Inject
    lateinit var newsRepository: NewsRepository


    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_entry_list, container, false)



    override fun createPresenter(): ArticlesContract.Presenter =
            ArticlesPresenter(newsRepository, scheduler, router)



    override fun createAdapter() = ArticlesAdapter(presenter)



}

class ArticlesAdapter(private val callback: Callback) : PagedListAdapter<Article, ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val holder = ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false))
        holder.itemView.setOnClickListener {
            callback.onArticleClick(getItem(holder.adapterPosition))
        }
        return holder
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    interface Callback {
        fun onArticleClick(article: Article?)
    }

}

class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.title)

    private val snippet: TextView = view.findViewById(R.id.snippet)

    private val root: ConstraintLayout = view.findViewById(R.id.root)

    private val activeColor: Int = ContextCompat.getColor(view.context, R.color.colorActiveArticle)

    private val primaryTextColor: Int = ContextCompat.getColor(view.context, R.color.colorPrimaryText)

    private val accentColor: Int = ContextCompat.getColor(view.context, R.color.colorUrl)

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