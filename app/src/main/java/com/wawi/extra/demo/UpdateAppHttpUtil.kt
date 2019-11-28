package com.wawi.android.wwsd.http

import com.google.gson.Gson
import com.vector.update_app.HttpManager
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.FileCallBack
import com.zhy.http.okhttp.callback.StringCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File

class UpdateAppHttpUtil : HttpManager {
    override fun download(
        url: String,
        path: String,
        fileName: String,
        callback: HttpManager.FileCallback
    ) {
        OkHttpUtils.get()
            .url(url)
            .build()
            .execute(object : FileCallBack(path, fileName) {
                override fun onBefore(request: Request?, id: Int) {
                    super.onBefore(request, id)
                    callback.onBefore()
                }

                override fun inProgress(progress: Float, total: Long, id: Int) {
                    callback.onProgress(progress, total)
                }

                override fun onResponse(response: File?, id: Int) {
                    callback.onResponse(response)
                }

                override fun onError(call: Call?, e: Exception?, id: Int) {
                    callback.onError("下载失败")
                }

            })
    }

    override fun asyncGet(
        url: String,
        params: MutableMap<String, String>,
        callBack: HttpManager.Callback
    ) {

        asyncPost(url, params, callBack)
    }

    override fun asyncPost(
        url: String,
        params: MutableMap<String, String>,
        callBack: HttpManager.Callback
    ) {

        val json = Gson().toJson(params)
        val body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json;charset=utf-8")
            .addHeader("Content-Type", "application/json;charset=utf-8")
            .post(body)
            .build()
        GlobalScope.launch {
            try {

                val res = OkHttpClient.Builder()
//                    .addNetworkInterceptor(UserAgentInterceptor())
                    .build()
                    .newCall(request).execute()
                callBack.onResponse(res.body()!!.string())
            } catch (e: Exception) {
                callBack.onError("更新失败")
            }
        }

    }

}