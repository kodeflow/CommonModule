package com.wawi.api.http

import com.google.gson.Gson
import com.wawi.api.CommonModule
import okhttp3.*
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

    /**
     * @see RetrofitServiceFactory.create(clazz: Class<T>, baseUrl: String, headerInterceptor: Interceptor = UserAgentInterceptor(), readTimeOut: Int = TIME_TO_READ, connectTimeOut: Int = TIME_TO_CONNECT)
     */
    fun <T> create(clazz: Class<T>, baseUrl: String, headerInterceptor: Interceptor = UserAgentInterceptor()): T {
        return create(clazz, baseUrl, headerInterceptor, TIME_TO_READ, TIME_TO_CONNECT)
    }

    /**
     * 拓展创建http句柄
     *
     * @param readTimeOut 读取连接超时设置（单位：秒）
     * @param connectTimeOut 连接超时设置（单位：秒）
     */
    fun <T> create(clazz: Class<T>, baseUrl: String, headerInterceptor: Interceptor = UserAgentInterceptor(), readTimeOut: Int = TIME_TO_READ, connectTimeOut: Int = TIME_TO_CONNECT): T {
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .addInterceptor(headerInterceptor)
            .addInterceptor(LoggerInterceptor())
            .addNetworkInterceptor(headerInterceptor)
//            .addInterceptor(GzipRequestInterceptor())
            .readTimeout(readTimeOut.toLong(), TimeUnit.SECONDS)
            .connectTimeout(connectTimeOut.toLong(), TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client).build()
        return retrofit.create(clazz)
    }
}

/** 这个interceptor是处理调试信息的 */
open class LoggerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response? {
        val response = chain.proceed(chain.request())
        val body = response.body()?.string()
        if (CommonModule.debugModeEnabled) println("------ DEBUG INFO: \n$body")
        return response.newBuilder().body(ResponseBody.create(MediaType.parse("UTF-8"), body ?: "{}")).build()
    }

}

/** 这个interceptor是修改Header头信息 */
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