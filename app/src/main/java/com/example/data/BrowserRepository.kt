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

    // Downloads API
    val allDownloads: Flow<List<DownloadItem>> = browserDao.getAllDownloads()

    suspend fun addDownload(download: DownloadItem) {
        browserDao.insertDownload(download)
    }

    suspend fun removeDownload(download: DownloadItem) {
        browserDao.deleteDownload(download)
    }

    suspend fun clearAllDownloads() {
        browserDao.clearAllDownloads()
    }

    // Saved Pages API
    val allSavedPages: Flow<List<SavedPage>> = browserDao.getAllSavedPages()

    suspend fun addSavedPage(title: String, url: String, filePath: String) {
        browserDao.insertSavedPage(SavedPage(title = title.ifEmpty { url }, url = url, filePath = filePath))
    }

    suspend fun removeSavedPage(page: SavedPage) {
        browserDao.deleteSavedPage(page)
    }

    suspend fun clearAllSavedPages() {
        browserDao.clearAllSavedPages()
    }
}
