package su.tagir.apps.radiot.model.api

import okhttp3.Interceptor
import okhttp3.Response
import su.tagir.apps.radiot.model.api.auth.AuthHolder
import java.io.IOException


class ApiHeadersInterceptor(private val authHolder: AuthHolder) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authHeader = authHolder.authHeader
        val requestBuilder = request.newBuilder()
        if(request.header(authHeader) == null && authHolder.accessToken != null && authHolder.tokenType != null) {
            requestBuilder.addHeader(authHeader, "${authHolder.tokenType} ${authHolder.accessToken}")
        }
        return chain.proceed(requestBuilder.build())
    }
}