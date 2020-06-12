package com.wawi.extra.common.compat

import android.app.Application
import android.widget.Toast

class ToastCompat {
    lateinit var toast: Toast

    companion object {
        var DEFAULT_MESSAGE = "ToastHelper_default_message"
        var TIME_SHORT = Toast.LENGTH_SHORT
        var TIME_LONG = Toast.LENGTH_LONG
        var instance = ToastCompat()
        fun init(app: Application?) {
            if (null == app) {
                println("初始化失败，application = null")
                return
            }
            instance.toast = Toast.makeText(app, DEFAULT_MESSAGE, TIME_LONG)
        }

        fun show(message: String, duration: Int = TIME_SHORT) {
            instance.toast.setText(message)
            instance.toast.duration = duration
            instance.toast.show()
        }

    }
}