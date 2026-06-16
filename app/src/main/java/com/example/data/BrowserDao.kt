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
