package com.wawi.api.http.bean

/**
 * Created by caowen on 2017/11/1.
 */
data class BaseBean<out T>(
    val resCode: Int,
    val resState: String,
    val resMsg: String,
    val resData: T?)

data class BaseLoginBean<out T, out E>(
    val resCode: Int,
    val resState: String,
    val resMsg: String,
    val resData: T?,
    val resExtra: E?)

data class Rows<out T>(
    val total: Int,
    val rows: List<T>
)