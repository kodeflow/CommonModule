package com.wawi.extra.common.http.dao

import android.arch.persistence.room.*
import com.wawi.extra.common.bean.DownloadBean

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download WHERE url == :url LIMIT 1")
    fun findByUrl(url: String): DownloadBean?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecords(vararg downloads: DownloadBean)
}

