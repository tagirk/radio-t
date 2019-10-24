package su.tagir.apps.radiot.di

import android.app.Application
import android.content.SharedPreferences
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.daead.DeterministicAeadFactory
import com.google.crypto.tink.daead.DeterministicAeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import su.tagir.apps.radiot.BuildConfig
import su.tagir.apps.radiot.encryption.TinkKeyEncryption
import su.tagir.apps.radiot.encryption.TinkValueEncryption
import su.tagir.apps.radiot.model.api.*
import su.tagir.apps.radiot.model.api.auth.AuthHolder
import su.tagir.apps.radiot.model.api.auth.GitterAuthHolder
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.db.createQueryWrapper
import su.tagir.apps.radiot.model.db.sqlOpenHelperConfiguration
import su.tagir.apps.radiot.model.repository.*
import java.security.GeneralSecurityException
import java.util.concurrent.TimeUnit

interface DataModule {


    val database: RadiotDb

    val preferences: SharedPreferences

    val chatRepository: ChatRepository

    val entryRepository: EntryRepository

    val newsRepository: NewsRepository

    val commentsRepository: CommentsRepository

    val downloadManager: DownloadManager

    class Impl(application: Application) : DataModule {

        companion object {

            private const val KEYSET_NAME = "master_keyset"
            private const val PREFERENCE_FILE = "master_key_preference"
            private const val MASTER_KEY_URI = "android-keystore://master_key"

            private const val DKEYSET_NAME = "dmaster_keyset"
            private const val DPREFERENCE_FILE = "dmaster_key_preference"
            private const val DMASTER_KEY_URI = "android-keystore://dmaster_key"

        }

        override val preferences: SharedPreferences by lazy {

            try {
                TinkConfig.register()
            } catch (e: GeneralSecurityException) {
                throw RuntimeException(e)
            }

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

            BinaryPreferencesBuilder(application)
                    .keyEncryption(TinkKeyEncryption(application, daead))
                    .valueEncryption(TinkValueEncryption(application, aead))
                    .build()
        }

        override val database: RadiotDb by lazy {
            val helper = FrameworkSQLiteOpenHelperFactory().create(sqlOpenHelperConfiguration(application))
            val driver: SqlDriver = AndroidSqliteDriver(helper)
            createQueryWrapper(driver)
        }

        override val chatRepository: ChatRepository by lazy { ChatRepositoryImpl(gitterAuthClient, gitterStreamClient, gitterRestClient, database, gitterAuthHolder, gson) }

        override val entryRepository: EntryRepository by lazy { EntryRepositoryImpl(radiotRestClient, remarkRestClient, database, downloadManager, application) }

        override val newsRepository: NewsRepository by lazy { NewsRepositoryImpl(newsRestClient, database) }

        override val commentsRepository: CommentsRepository by lazy { CommentsRepositoryImpl(remarkRestClient) }

        override val downloadManager: DownloadManager by lazy { DownloadManagerImpl(application) }

        private val gson: Gson by lazy { GsonBuilder().create() }

        private val gitterAuthHolder: AuthHolder = GitterAuthHolder(preferences)

        private val radiotRestClient: RestClient by lazy {
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
            builder.client(httpClient.build())
                    .build()
                    .create(RestClient::class.java)
        }

        private val remarkRestClient: RemarkClient by lazy {
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
            builder.client(httpClient.build())
                    .build()
                    .create(RemarkClient::class.java)
        }

        private val newsRestClient: NewsRestClient by lazy {
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
            builder.client(httpClient.build())
                    .build()
                    .create(NewsRestClient::class.java)
        }

        private val gitterAuthClient: GitterAuthClient by lazy {
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
            builder.client(httpClient.build())
                    .build()
                    .create(GitterAuthClient::class.java)
        }

        private val gitterRestClient: GitterClient by lazy {
            val builder = Retrofit.Builder()
                    .baseUrl("https://api.gitter.im/")
                    .addConverterFactory(GsonConverterFactory.create())

            val httpClient = OkHttpClient.Builder()
                    .addInterceptor(ApiHeadersInterceptor(gitterAuthHolder))
//                    .authenticator(ApiAuthenticator(gitterAuthHolder))

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                httpClient
                        .addInterceptor(loggingInterceptor)

            }
            builder.client(httpClient.build())
                    .build()
                    .create(GitterClient::class.java)
        }

        private val gitterStreamClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                    .addNetworkInterceptor(ApiHeadersInterceptor(gitterAuthHolder))
                    .authenticator(ApiAuthenticator(gitterAuthHolder))
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build()
        }
    }
}