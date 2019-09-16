package com.wawi.extra.common.http.download

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

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
            bufferedSource = Okio.buffer(WrapSource(responseBody, callback))
        }
        return bufferedSource!!
    }

}