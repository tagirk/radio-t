package su.tagir.apps.radiot.model.api

import okhttp3.Interceptor
import okhttp3.Response
import su.tagir.apps.radiot.model.AuthHolder
import java.io.IOException


class ApiHeadersInterceptor(private val authHolder: AuthHolder) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBuilder = request.newBuilder()
        requestBuilder.addHeader("Authorization", "${authHolder.tokenType} ${authHolder.token}")


        val response = chain.proceed(requestBuilder.build())
        if (response.code() == 401) {
            authHolder.clear()
            return response
        }

        return response
    }
}