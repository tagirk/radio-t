package su.tagir.apps.radiot.di

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import su.tagir.apps.radiot.BuildConfig
import su.tagir.apps.radiot.model.Prefs
import su.tagir.apps.radiot.model.api.*
import su.tagir.apps.radiot.model.repository.DownloadManager
import su.tagir.apps.radiot.model.repository.DownloadManagerImpl
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.schedulers.SchedulerProvider
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideApiClient(): RestClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://radio-t.com/site-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))

        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(RestClient::class.java)
    }

    @Singleton
    @Provides
    fun provideNewsApiClient(): NewsRestClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://news.radio-t.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))

        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(NewsRestClient::class.java)
    }

    @Singleton
    @Provides
    fun provideFirebaseRestClient(): FirebaseRestClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://radiot-4ac4a.firebaseio.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))


        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(FirebaseRestClient::class.java)
    }

    @Singleton
    @Provides
    fun provideGitterAuthClient(): GitterAuthClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))


        val httpClient = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(GitterAuthClient::class.java)
    }

    @Singleton
    @Provides
    fun provideGitterClient(authHolder: Prefs): GitterClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://api.gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))


        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(ApiHeadersInterceptor(authHolder))
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(GitterClient::class.java)
    }

    @Singleton
    @Provides
    fun provideGitterStreamClient(authHolder: Prefs): GitterStreamClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://stream.gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))


        val httpClient = OkHttpClient.Builder()
        httpClient
                .addNetworkInterceptor(ApiHeadersInterceptor(authHolder))
                .readTimeout(10, TimeUnit.MINUTES)
        return builder.client(httpClient.build())
                .build()
                .create(GitterStreamClient::class.java)
    }

    @Singleton
    @Provides
    fun providePrefs(application: Application): Prefs = Prefs(application)



    @Singleton
    @Provides
    fun provideScheduler(): BaseSchedulerProvider = SchedulerProvider()

    @Singleton
    @Provides
    fun provideGson():Gson =
            GsonBuilder()
                    .create()


    @Singleton
    @Provides
    fun provideDownloadManager(application: Application): DownloadManager = DownloadManagerImpl(application)

}