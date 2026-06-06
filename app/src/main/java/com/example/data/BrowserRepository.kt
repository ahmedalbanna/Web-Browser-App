package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {

    val allBookmarks: Flow<List<Bookmark>> = browserDao.getAllBookmarks()
    val allHistory: Flow<List<HistoryEntry>> = browserDao.getAllHistory()

    suspend fun addBookmark(title: String, url: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isNotEmpty()) {
            browserDao.insertBookmark(Bookmark(title = title.ifEmpty { url }, url = cleanUrl))
        }
    }

    suspend fun removeBookmark(bookmark: Bookmark) {
        browserDao.deleteBookmark(bookmark)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    fun isBookmarked(url: String): Flow<Boolean> {
        return browserDao.isBookmarkedFlow(url)
    }

    suspend fun isBookmarkedSync(url: String): Boolean {
        return browserDao.isBookmarked(url)
    }

    suspend fun addHistoryEntry(title: String, url: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isNotEmpty() && !cleanUrl.startsWith("about:blank")) {
            browserDao.insertHistory(HistoryEntry(title = title.ifEmpty { url }, url = cleanUrl))
        }
    }

    suspend fun removeHistoryEntry(entry: HistoryEntry) {
        browserDao.deleteHistoryEntry(entry)
    }

    suspend fun clearHistory() {
        browserDao.clearAllHistory()
    }
}
