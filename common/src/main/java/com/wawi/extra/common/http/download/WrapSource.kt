package com.wawi.extra.common.http.download

import com.wawi.api.CommonModule
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource

class WrapSource(responseBody: ResponseBody, callback: ((Long, Long, Boolean) -> Unit)?) : ForwardingSource(responseBody.source()) {
    private var totalBytesRead: Long = 0
    private val callback = callback
    private val responseBody = responseBody
    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        val done = bytesRead == -1L
        totalBytesRead = if (!done) bytesRead else 0

        callback?.let {
            it(totalBytesRead, responseBody.contentLength(), done)
        }
        return bytesRead
    }
}