package com.wawi.extra.common.http

import android.util.ArrayMap
import com.google.gson.Gson
import com.wawi.api.CommonModule
import com.wawi.api.extensions.getUrl
import com.wawi.api.extensions.getScheme
import com.wawi.api.http.LoggerInterceptor
import com.wawi.api.http.RetrofitServiceFactory
import com.wawi.extra.common.bean.DownloadBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface DownloadService {
    @Streaming
    @GET
    fun downloadFile(@Header("RANGE") start: String, @Url url: String): Observable<ResponseBody>
}
object Download {
    var interceptor: DownloadInterceptor = DownloadInterceptor()

    private val service: DownloadService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder().baseUrl("http://hbwwcc.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .callbackExecutor(Executors.newFixedThreadPool(1))
            .client(client).build()
        retrofit.create(DownloadService::class.java)
    }



    fun downloadFile(range: String, url: String, progress: (Long, Long, Boolean) -> Unit, success :(ResponseBody?) -> Unit, fail: (Throwable?) -> Unit): Disposable? {

        interceptor.addCallback("${interceptor.getUrl()}$url", progress)
        return service.downloadFile(range, url)
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .map {
                println("length: ${it.contentLength()}")
                return@map it
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = fail, onNext = success)
    }
}

class DownloadManager private constructor() {
    private val db = CommonModule.getDownloadDatabase()

    companion object {
        @Volatile private var instance: DownloadManager? = null
        fun shared() =
            instance ?: synchronized(this) {
                instance ?: DownloadManager().also { instance = it }
            }
    }

    fun download(url: String) {
        val scheme = url.getScheme()
        val host = url.getUrl()
        val segments = url.substring(host.length+scheme.length+3)
        Download.interceptor.setUrl(url)
        GlobalScope.launch {
            val byUrl = db.downloadDao().findByUrl(url)
            Download.downloadFile("bytes=${byUrl?.readLength ?: 0}-", segments, { a,b,c ->
                println("success: $a, $b, $c")
                val bean = DownloadBean(url, "", "", b, a)
                db.downloadDao().insertRecords(bean)
            }, {

            }) {

            }
        }

    }

    private fun insert(vararg item: DownloadBean) {
        db.downloadDao().insertRecords(*item)
    }

    private fun getRecord(url: String): DownloadBean? {
        return db.downloadDao().findByUrl(url)
    }

}

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
        this.url?.let {
            val scheme = getUrl().getScheme()
            val host = getUrl().getUrl()
            val segments = getUrl().substring(host.length+scheme.length+3)

            val url = request.url().newBuilder()
                .scheme(scheme)
                .host(host)
                .build()
            request = request.newBuilder()
                .url(url)
                .build()
        }

        val call = callback[request.url().toString()]

        val originalResponse = chain.proceed(request)
        // 拦截后重新构建返回
        return originalResponse.newBuilder()
            .body(originalResponse.body()?.let { DownloadResponseBody(it, call) }).build()
    }
}

class DownloadResponseBody(responseBody: ResponseBody, callback: ((Long, Long, Boolean) -> Unit)? = null): ResponseBody() {
    private val responseBody = responseBody
    private var bufferedSource: BufferedSource? = null
    private var callback: ((Long, Long, Boolean) -> Unit)? = callback
    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead: Long = 0
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                val done = bytesRead != -1L
                totalBytesRead = if (done) bytesRead else 0
                callback?.let { it(totalBytesRead, contentLength(), done) }
                return bytesRead
            }
        }
    }

}