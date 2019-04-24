package com.wawi.api.http

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * retrofit&rxjava&gson封装
 */
object RetrofitServiceFactory {
    // Time in seconds
    private const val TIME_TO_CONNECT = 10
    private const val TIME_TO_READ = 20
//    private const val BASE_URL = "http://zhkt.hbwwcc.com:8080/wc/"
//    private const val BASE_URL = "http://106.15.88.155:9090/"
//    val crowdService: CrowdService by lazy {
//        create(
//            CrowdService::class.java
//        )
//    }
    open fun <T> create(clazz: Class<T>, baseUrl: String, headerInterceptor: Interceptor = UserAgentInterceptor()): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
//            .addInterceptor(GzipRequestInterceptor())
            .readTimeout(TIME_TO_READ.toLong(), TimeUnit.SECONDS)
            .connectTimeout(TIME_TO_CONNECT.toLong(), TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client).build()
        return retrofit.create(clazz)
    }
}

/** 这个interceptor是修改User-Agent头信息 */
open class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response? {
        val originalRequest = chain.request()
        println("User-Agent：${originalRequest.header("User-Agent")}")
        val request = originalRequest.newBuilder()
            .addHeader("jsmile.mobile", "mobile")
            .addHeader("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }

}