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
        CommonModule.regist(shared)
    }
}

// ---------------- extensions ----------------

/**
 *
 * @in Int.this 输入为dp
 * @out px 值
 *
 * 将dip转成px
 */
val Int.dp: Int
    get() = (this * App.shared.resources.displayMetrics.density + 0.5f).toInt()