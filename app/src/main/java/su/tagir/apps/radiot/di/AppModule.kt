package su.tagir.apps.radiot.di

import android.app.Application
import android.content.SharedPreferences
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.daead.DeterministicAeadFactory
import com.google.crypto.tink.daead.DeterministicAeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import su.tagir.apps.radiot.BuildConfig
import su.tagir.apps.radiot.encryption.TinkKeyEncryption
import su.tagir.apps.radiot.encryption.TinkValueEncryption
import su.tagir.apps.radiot.model.api.*
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.repository.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {

    companion object {
        private const val KEYSET_NAME = "master_keyset"
        private const val PREFERENCE_FILE = "master_key_preference"
        private const val MASTER_KEY_URI = "android-keystore://master_key"

        private const val DKEYSET_NAME = "dmaster_keyset"
        private const val DPREFERENCE_FILE = "dmaster_key_preference"
        private const val DMASTER_KEY_URI = "android-keystore://dmaster_key"

    }

    @Singleton
    @Provides
    fun providePreferences(application: Application): SharedPreferences {

        val aead by lazy {
            val keysetHandle = AndroidKeysetManager.Builder()
                    .withSharedPref(application, KEYSET_NAME, PREFERENCE_FILE)
                    .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                    .withMasterKeyUri(MASTER_KEY_URI)
                    .build()
                    .keysetHandle

            AeadFactory.getPrimitive(keysetHandle)
        }

        val daead by lazy {
            val keysetHandle = AndroidKeysetManager.Builder()
                    .withSharedPref(application, DKEYSET_NAME, DPREFERENCE_FILE)
                    .withKeyTemplate(DeterministicAeadKeyTemplates.AES256_SIV)
                    .withMasterKeyUri(DMASTER_KEY_URI)
                    .build()
                    .keysetHandle

            DeterministicAeadFactory.getPrimitive(keysetHandle)
        }

        return BinaryPreferencesBuilder(application)
                .keyEncryption(TinkKeyEncryption(application, daead))
                .valueEncryption(TinkValueEncryption(application, aead))
                .build()
    }


    @Singleton
    @Provides
    fun gitterAuthHolder(prefs: SharedPreferences): GitterAuthHolder = GitterAuthHolder(prefs)

    @Singleton
    @Provides
    fun provideApiClient(): RestClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://radio-t.com/site-api/")
                .addConverterFactory(GsonConverterFactory.create())

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
    fun provideRemarkClient(): RemarkClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://remark42.radio-t.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())

        val httpClient = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            httpClient
                    .addInterceptor(loggingInterceptor)

        }
        return builder.client(httpClient.build())
                .build()
                .create(RemarkClient::class.java)
    }

    @Singleton
    @Provides
    fun provideNewsApiClient(): NewsRestClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://news.radio-t.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())

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
    fun provideGitterAuthClient(): GitterAuthClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())

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
    fun provideGitterClient(authHolder: GitterAuthHolder): GitterClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://api.gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(ApiHeadersInterceptor(authHolder))
                .authenticator(ApiAuthenticator(authHolder))

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
    fun provideGitterStreamClient(authHolder: GitterAuthHolder): GitterStreamClient {
        val builder = Retrofit.Builder()
                .baseUrl("https://stream.gitter.im/")
                .addConverterFactory(GsonConverterFactory.create())

        val httpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(ApiHeadersInterceptor(authHolder))
                .authenticator(ApiAuthenticator(authHolder))
                .readTimeout(10, TimeUnit.MINUTES)

        return builder.client(httpClient.build())
                .build()
                .create(GitterStreamClient::class.java)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson =
            GsonBuilder()
                    .create()

    @Singleton
    @Provides
    fun provideChatRepository(authClient: GitterAuthClient,
                              streamClient: GitterStreamClient,
                              gitterClient: GitterClient,
                              database: RadiotDb,
                              authHolder: GitterAuthHolder,
                              gson: Gson): ChatRepository =

            ChatRepositoryImpl(authClient,
                    streamClient,
                    gitterClient,
                    database,
                    authHolder,
                    gson)

    @Singleton
    @Provides
    fun provideEntryRepository(restClient: RestClient,
                               remarkClient: RemarkClient,
                               database: RadiotDb,
                               downloadManager: DownloadManager,
                               application: Application): EntryRepository =

            EntryRepositoryImpl(restClient, remarkClient, database, downloadManager, application)

    @Singleton
    @Provides
    fun provideNewsRepository(newsRestClient: NewsRestClient,
                              database: RadiotDb): NewsRepository =

            NewsRepositoryImpl(newsRestClient, database)

    @Singleton
    @Provides
    fun provideDownloadManager(application: Application): DownloadManager = DownloadManagerImpl(application)


}