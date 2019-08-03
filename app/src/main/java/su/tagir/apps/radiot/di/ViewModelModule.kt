package su.tagir.apps.radiot.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.chat.AuthViewModel
import su.tagir.apps.radiot.ui.chat.ChatViewModel
import su.tagir.apps.radiot.ui.comments.CommentsViewModel
import su.tagir.apps.radiot.ui.hosts.HostsViewModel
import su.tagir.apps.radiot.ui.localcontent.LocalContentViewModel
import su.tagir.apps.radiot.ui.news.ArticlesViewModel
import su.tagir.apps.radiot.ui.news.NewsViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelFactory
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    abstract fun bindPodcastsNewsViewModel(newsViewModel: NewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostsViewModel::class)
    abstract fun bindHostsViewModel(hostsViewModel: HostsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LocalContentViewModel::class)
    abstract fun localContentViewModel(localContentViewModel: LocalContentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArticlesViewModel::class)
    abstract fun streamViewModel(articlesViewModel: ArticlesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun authViewModel(authViewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun chatViewModel(chatViewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    abstract fun commentsViewModel(commentsViewModel: CommentsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}


@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)