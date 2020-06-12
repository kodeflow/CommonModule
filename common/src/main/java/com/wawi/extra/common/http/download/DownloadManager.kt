package com.wawi.extra.common.http.download

import com.google.gson.Gson
import com.wawi.api.CommonModule
import com.wawi.api.extensions.digest
import com.wawi.api.extensions.getBaseUrl
import com.wawi.api.extensions.sufix
import com.wawi.extra.common.bean.DownloadBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.reactivestreams.Subscription
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DownloadManager private constructor() {
    private val db = CommonModule.getDownloadDatabase()

    companion object {
        val compositeDisposable = CompositeDisposable()
        @Volatile private var instance: DownloadManager? = null
        fun shared() =
            instance ?: synchronized(this) {
                instance
                    ?: DownloadManager().also { instance = it }
            }
    }

    fun download(url: String, progress: (Long, Long, Boolean) -> Unit, fail: (Throwable?) -> Unit) {
        val bean = getRecord(url)
        var startPoint = bean?.readLength ?: 0

        bean?.filePath?.let {
            val file = File(it)
            if (file.exists()) {
                startPoint = file.length()
            }
        }

        val disposable = Download.downloadFile(
            "bytes=$startPoint-", url, progress, fail
        )
        compositeDisposable.add(disposable!!)
    }



    fun pause() {
        if (!compositeDisposable.isDisposed) {
            if (compositeDisposable.size() != 0) compositeDisposable.clear()
        }
    }

    private fun getRecord(url: String): DownloadBean? {
        return db.downloadDao().findByUrl(url)
    }

}

interface DownloadService {
    @Streaming
    @GET
    fun downloadFile(@Header("RANGE") start: String, @Url url: String): Observable<ResponseBody>
}

object Download {
    /**
     * 暴露出来的下载方法
     */
    fun downloadFile(range: String, url: String, progress: (Long, Long, Boolean) -> Unit, fail: (Throwable?) -> Unit): Disposable? {

        if (CommonModule.debugModeEnabled) {
            println("-----> $range")
        }

        if (verify(url)) {
            progress(0, 0, true)
            return null
        }
        prepareUrl(url)
        interceptor.addCallback("${interceptor.getUrl()}", progress)
        val baseUrl = url.getBaseUrl()
        return getRetrofit(baseUrl).downloadFile(range, url.substring(baseUrl.length))
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .map {
                writeCache(it, url)
                return@map it
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = fail)
    }

    private fun prepareUrl(url: String) {
        interceptor.setUrl(url)
    }

    private var interceptor: DownloadInterceptor =
        DownloadInterceptor()

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(baseUrl: String): DownloadService {
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .callbackExecutor(Executors.newSingleThreadExecutor())
            .client(getClient()).build()
        return retrofit.create(DownloadService::class.java)
    }

    private fun verify(url: String, md5: String? = null): Boolean {
        val dao = CommonModule.getDownloadDatabase().downloadDao()
        val bean = dao.findByUrl(url) ?: return false
        return if (md5.isNullOrEmpty()) {
            bean.readLength == bean.contentLength
        } else {
            val file = File(bean.filePath)
            file.digest() == md5
        }
    }

    private fun writeCache(it: ResponseBody, url: String) {
        val  dao = CommonModule.getDownloadDatabase().downloadDao()
        var bean = dao.findByUrl(url)

        if (CommonModule.debugModeEnabled) {
            println("---> all length: ${it.contentLength()}")
        }

        var file: File
        if (bean == null) {
            val fileName = "${System.currentTimeMillis()}${it.contentType()?.sufix()}"
            val outFile = File(CommonModule.getContext().externalCacheDir, fileName)
            if (!outFile.exists()) {
                outFile.createNewFile()
            }
            file = outFile
            bean = DownloadBean(url, outFile.absolutePath, fileName, contentLength = it.contentLength())
        } else {
            file = File(bean.filePath)
        }
        bean.readLength = file.length()
        dao.insertRecords(bean)

        val channelOut: FileChannel? = null
        var randomAccessFile: RandomAccessFile?
        randomAccessFile = RandomAccessFile(file, "rwd")
        randomAccessFile.seek(bean.readLength)
        var len: Int
        val buffer = ByteArray(1024 * 4)
        var bufferredLength = 0L

        while ((it.byteStream().read(buffer).also { readed ->
                len = readed
            }) != -1) {
            randomAccessFile.write(buffer, 0, len)
            bufferredLength += len
            bean.readLength = bufferredLength
            dao.insertRecords(bean)
        }
        bean.readLength = bean.contentLength
        dao.insertRecords(bean)
        it.byteStream().close()
        channelOut?.close()
        randomAccessFile.close()
    }
}



