package com.wawi.api

import androidx.room.Room
import android.content.Context
import com.wawi.extra.common.http.dao.DownloadDatabase
import java.lang.NullPointerException

object CommonModule {
    private var sAppContext: Context? = null
    private var sDatabase: DownloadDatabase? = null
    /**
     * 在Application.onCreate 中注册
     * <br/>
     * <code>
     *     class App: Application() {
     *       override fun onCreate() {
     *         super.onCreate()
     *         // regist after super call.
     *         regist(this)
     *       }
     *     }
     * </code>
     */
    fun regist(appContext: Context) {
        if (sAppContext == null) {
            sAppContext = appContext.applicationContext
            // 初始化下载控件
            sDatabase = Room.databaseBuilder(
                sAppContext!!,
                DownloadDatabase::class.java,
                "_download.db"
            ).build()
        }
    }

    fun getDownloadDatabase(): DownloadDatabase {
        return sDatabase ?: throw NullPointerException("CommonModule.sAppContext can not be null. Use after registration.")
    }

    /**
     * 获取主module的context
     *
     * 注意：必须在注册后使用
     */
    fun getContext(): Context {
        return sAppContext ?: throw NullPointerException("CommonModule.sAppContext can not be null. Use after registration.")
    }

    private var _debugModeEnabled = false
    var debugModeEnabled = _debugModeEnabled
        get() = _debugModeEnabled
        private set
    fun setDebugModeEnable(enable: Boolean) {
        _debugModeEnabled = enable
    }


}