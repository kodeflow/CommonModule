package com.wawi.api.http.bean

/**
 * Created by caowen on 2017/11/1.
 */

data class LoginBean(
    val total: Int
)

data class LoginExtra(
    val token: String
)

data class UploadBean(
    val url: String
)