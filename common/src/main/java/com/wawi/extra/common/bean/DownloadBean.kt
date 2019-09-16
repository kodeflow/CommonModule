package com.wawi.extra.common.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "download")
data class DownloadBean(
    @PrimaryKey val url: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "content_length") val contentLength: Long = 0,
    @ColumnInfo(name = "read_length") var readLength: Long = 0
)