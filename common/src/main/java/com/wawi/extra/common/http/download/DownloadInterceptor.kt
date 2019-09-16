package com.wawi.extra.common.http.download

import android.util.ArrayMap
import com.wawi.api.CommonModule
import okhttp3.Interceptor
import okhttp3.Response

class DownloadInterceptor: Interceptor {
    @Volatile private var url: String? = null
    fun getUrl(): String {
        return url ?: ""
    }
    private val callback = ArrayMap<String, ((Long, Long, Boolean) -> Unit)?>()
    fun setUrl(url: String) {
        this.url = url
    }

    fun addCallback(url: String, call: ((Long, Long, Boolean) -> Unit)?) {
        callback[url] = call
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val call = callback[request.url().toString()]
        if (CommonModule.debugModeEnabled) {
            println("---> key: ${request.url()}")
            println("---> call: $call ")
            println("---> keys: ${callback.keys.first()} ")
        }

        val originalResponse = chain.proceed(request)
        // 拦截后重新构建返回
        return originalResponse.newBuilder()
            .body(originalResponse.body()?.let {
                DownloadResponseBody(it, call)
            }).build()
    }
}