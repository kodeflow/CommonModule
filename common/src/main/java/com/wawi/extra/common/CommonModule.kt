package com.wawi.api

import android.content.Context
import java.lang.NullPointerException

object CommonModule {
    private var sAppContext: Context? = null
    fun regist(appContext: Context) {
        if (sAppContext == null) {
            sAppContext = appContext.applicationContext
        }
    }

    fun getContext(): Context {
        return sAppContext ?: throw NullPointerException("CommonModule.sAppContext can not be null.")
    }
}