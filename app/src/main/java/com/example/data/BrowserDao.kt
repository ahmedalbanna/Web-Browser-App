package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // Bookmarks Operations
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    fun isBookmarkedFlow(url: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    suspend fun isBookmarked(url: String): Boolean


    // History Operations
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Delete
    suspend fun deleteHistoryEntry(entry: HistoryEntry)

    @Query("DELETE FROM history_entries")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHistoryEntry(): HistoryEntry?

    @Query("SELECT COUNT(*) FROM history_entries")
    suspend fun getHistoryCount(): Int

    @Query("DELETE FROM history_entries WHERE id IN (SELECT id FROM history_entries ORDER BY timestamp ASC LIMIT :n)")
    suspend fun deleteOldestHistory(n: Int)

    @Query("UPDATE history_entries SET timestamp = :timestamp, title = :title WHERE id = :id")
    suspend fun updateHistoryTimestamp(id: Long, title: String, timestamp: Long)

    // Download Operations
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadItem)

    @Delete
    suspend fun deleteDownload(download: DownloadItem)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()

    // Saved Pages Operations
    @Query("SELECT * FROM saved_pages ORDER BY timestamp DESC")
    fun getAllSavedPages(): Flow<List<SavedPage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPage(page: SavedPage)

    @Delete
    suspend fun deleteSavedPage(page: SavedPage)

    @Query("DELETE FROM saved_pages")
    suspend fun clearAllSavedPages()
}
