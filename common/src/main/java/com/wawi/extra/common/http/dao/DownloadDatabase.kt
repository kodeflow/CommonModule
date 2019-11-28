package com.wawi.extra.common.http.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wawi.extra.common.bean.DownloadBean

@Database(entities = [DownloadBean::class], version = 1)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}