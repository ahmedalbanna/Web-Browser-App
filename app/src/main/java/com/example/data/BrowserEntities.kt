package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey val id: String,
    val fileName: String,
    val url: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progress: Int, // 0-100
    val status: String, // "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED", "CANCELED"
    val timestamp: Long = System.currentTimeMillis(),
    val speed: String = "0 KB/s",
    val mimeType: String = ""
)

data class ExtractedVideo(
    val url: String,
    val title: String
)
