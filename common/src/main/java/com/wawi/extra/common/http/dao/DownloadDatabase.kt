package com.wawi.extra.common.http.dao

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.wawi.extra.common.bean.DownloadBean

@Database(entities = [DownloadBean::class], version = 1)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}