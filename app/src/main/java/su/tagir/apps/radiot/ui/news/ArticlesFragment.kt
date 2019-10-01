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
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.DataBoundViewHolder
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.utils.shortDateFormat
import javax.inject.Inject

class ArticlesFragment : BaseMvpListFragment<Article, ArticlesContract.View, ArticlesContract.Presenter>(),
        ArticlesContract.View,
        ArticlesAdapter.Callback,
        Injectable {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var newsRepository: NewsRepository

    override fun createPresenter(): ArticlesContract.Presenter =
            ArticlesPresenter(newsRepository, router)

    override fun createAdapter() = ArticlesAdapter(this)

    override fun onArticleClick(article: Article) {
        presenter.showArticle(article)
    }
}

class ArticlesAdapter(private val callback: Callback) : DataBoundListAdapter<Article>() {


    override val differ: AsyncListDiffer<Article> = AsyncListDiffer<Article>(this, object : DiffUtil.ItemCallback<Article>(){

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem.slug == newItem.slug

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem == newItem

    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val holder = ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false))
        holder.itemView.setOnClickListener {
            callback.onArticleClick(items[holder.adapterPosition])
        }
        return holder
    }

    override fun bind(viewHolder: DataBoundViewHolder<Article>, position: Int) {
        viewHolder.bind(items[position])
    }

    interface Callback {
        fun onArticleClick(article: Article)
    }

}

class ArticleViewHolder(view: View) : DataBoundViewHolder<Article>(view) {

    private val title: TextView = view.findViewById(R.id.title)

    private val snippet: TextView = view.findViewById(R.id.snippet)

    private val root: ConstraintLayout = view.findViewById(R.id.root)

    private val activeColor: Int = ContextCompat.getColor(view.context, R.color.colorActiveArticle)

    private val primaryTextColor: Int = ContextCompat.getColor(view.context, R.color.colorPrimaryText)

    private val accentColor: Int = ContextCompat.getColor(view.context, R.color.colorUrl)

    override fun bind(t: Article?) {
        title.text = t?.title

        val date = t?.addedDate?.shortDateFormat()
        val sb = SpannableStringBuilder()
                .append(t?.domain)
                .append(", ")
                .append(date)
                .append(" - ")
                .append(t?.snippet?.replace("\n", ""))

        val domainLength = t?.domain?.length?.plus(2) ?: 0
        sb.setSpan(UnderlineSpan(), 0, if (domainLength >= 2) domainLength - 2 else domainLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.setSpan(ForegroundColorSpan(accentColor), 0, if (domainLength >= 2) domainLength - 2 else domainLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.setSpan(ForegroundColorSpan(primaryTextColor), domainLength,
                domainLength.plus(date?.length?.plus(3) ?: 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        snippet.text = sb
        if (t?.active == true) {
            root.setBackgroundColor(activeColor)
        } else {
            root.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
