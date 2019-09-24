package su.tagir.apps.radiot.model.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class ApiHeadersInterceptor(private val authHolder: AuthHolder) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authHeader = authHolder.authHeader
        val requestBuilder = request.newBuilder()
        if(request.header(authHeader) == null) {
            requestBuilder.addHeader(authHeader, "${authHolder.tokenType} ${authHolder.accessToken}")
        }
        return chain.proceed(requestBuilder.build())
    }
}