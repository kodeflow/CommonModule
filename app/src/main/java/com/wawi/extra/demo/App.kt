package com.wawi.extra.demo

import android.app.Application
import com.wawi.api.CommonModule
import com.wawi.api.delegate.NotNullSingleValueVar

class App: Application() {
    companion object {
        var shared: App by NotNullSingleValueVar()
    }

    override fun onCreate() {
        super.onCreate()
        shared = this
        CommonModule.setDebugModeEnable(true)
        CommonModule.regist(shared)
    }
}