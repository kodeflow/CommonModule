package com.wawi.extra.demo

import com.wawi.api.CommonModule
import com.wawi.api.http.RetrofitServiceFactory
import com.wawi.api.http.bean.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CrowdService {
    @POST("cf/rider/register")
    fun register(@Body paramRegister: ParamRegister): Observable<BaseBean<Rows<String>>>
    @POST("sys/sendSmsCode")
    fun sendSmsCode(@Body paramSmsCode: ParamSmsCode): Observable<BaseBean<Rows<String>>>
    @Multipart
    @POST("oss/uploadImage")
    fun uploadImage(@Part("file1") file: RequestBody): Observable<BaseBean<Rows<UploadBean>>>
    @POST("cf/rider/login")
    fun login(@Body paramLogin: ParamLogin): Observable<BaseLoginBean<LoginBean, LoginExtra>>
}

object Crowd {
    private const val BASE_URL = "http://106.15.88.155:9090/"
    private val service: CrowdService by lazy {
        RetrofitServiceFactory.create(
            CrowdService::class.java,
            BASE_URL,
            HeaderInterceptor()
        )
    }

    /**
     * 用户登录
     * @param paramLogin 登录参数
     * @param success 请求成功回调
     * @param fail 请求失败回调
     */
    fun login(paramLogin: ParamLogin, success :(BaseLoginBean<LoginBean, LoginExtra>?) -> Unit, fail: (Throwable?) -> Unit): Disposable? {
        return service.login(paramLogin)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = success, onError = fail)
    }

    fun uploadImage(file: RequestBody, success: (BaseBean<Rows<UploadBean>>?) -> Unit, fail: (Throwable?) -> Unit): Disposable? {
        return service.uploadImage(file)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = success, onError = fail)
    }
    fun sendSmsCode(paramSmsCode: ParamSmsCode, success: (BaseBean<Rows<String>>?) -> Unit, fail: (Throwable?) -> Unit): Disposable? {
        return service.sendSmsCode(paramSmsCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = success, onError = fail)
    }

    fun register(paramRegister: ParamRegister, success: (BaseBean<Rows<String>>?) -> Unit, fail: (Throwable?) -> Unit): Disposable? {
        return service.register(paramRegister)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = success, onError = fail)
    }
}

class HeaderInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response? {
        val originalRequest = chain.request()
        println("User-Agent：${originalRequest.header("User-Agent")}")
        val request = originalRequest.newBuilder()
            .addHeader("jsmile.mobile", "mobile")
            .addHeader("Content-Type", "application/json")
            .apply {
                if (User.token.isNotEmpty()) {
                    addHeader("token", User.token)
                }
            }
            .build()

        return chain.proceed(request)
    }
}