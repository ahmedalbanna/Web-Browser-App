package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.data.Bookmark
import com.example.data.HistoryEntry
import com.example.data.DownloadItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

data class VpnServer(
    val name: String,
    val countryCode: String,
    val flag: String,
    val ipAddress: String,
    val port: String,
    val ping: Int,
    val speed: String
)

val vpnServersList = listOf(
    VpnServer("ألمانيا (فرانكفورت)", "DE", "🇩🇪", "46.165.2.14", "80", 22, "112.5 Mbps"),
    VpnServer("الولايات المتحدة (نيويورك)", "US", "🇺🇸", "104.244.42.1", "443", 38, "95.4 Mbps"),
    VpnServer("المملكة المتحدة (لندن)", "GB", "🇬🇧", "185.86.151.2", "1194", 45, "87.1 Mbps"),
    VpnServer("اليابان (طوكيو)", "JP", "🇯🇵", "210.140.10.3", "500", 112, "74.8 Mbps"),
    VpnServer("سنغافورة (شانغي)", "SG", "🇸🇬", "128.199.64.12", "8080", 89, "91.2 Mbps"),
    VpnServer("فرنسا (باريس)", "FR", "🇫🇷", "195.154.120.9", "443", 28, "104.1 Mbps")
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WebBrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository = BrowserRepository(BrowserDatabase.getDatabase(application).browserDao())
    private val activeDownloadJobs = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    val allDownloads: StateFlow<List<DownloadItem>> = repository.allDownloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.allDownloads.first().forEach { item ->
                if (item.status == "DOWNLOADING") {
                    repository.addDownload(item.copy(status = "PAUSED", speed = "موقف مؤقتاً"))
                }
            }
        }
    }

    // Tabs state
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(BrowserTab()))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Active url input string (what's shown in Address Bar)
    private val _urlInput = MutableStateFlow("https://www.google.com")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    // Incognito Mode State
    private val _isIncognito = MutableStateFlow(false)
    val isIncognito: StateFlow<Boolean> = _isIncognito.asStateFlow()

    // VPN States
    private val _isVpnConnected = MutableStateFlow(false)
    val isVpnConnected: StateFlow<Boolean> = _isVpnConnected.asStateFlow()

    private val _selectedVpnServer = MutableStateFlow(vpnServersList[0])
    val selectedVpnServer: StateFlow<VpnServer> = _selectedVpnServer.asStateFlow()

    private val _simulatedPing = MutableStateFlow(22)
    val simulatedPing: StateFlow<Int> = _simulatedPing.asStateFlow()

    private val _simulatedDataUsage = MutableStateFlow(0f)
    val simulatedDataUsage: StateFlow<Float> = _simulatedDataUsage.asStateFlow()

    private var vpnJob: kotlinx.coroutines.Job? = null

    // Ad Blocker States
    private val _isAdBlockerEnabled = MutableStateFlow(true)
    val isAdBlockerEnabled: StateFlow<Boolean> = _isAdBlockerEnabled.asStateFlow()

    private val _blockedAdsCount = MutableStateFlow(0)
    val blockedAdsCount: StateFlow<Int> = _blockedAdsCount.asStateFlow()

    // Page state (loading, progress, secure)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _isSecure = MutableStateFlow(true)
    val isSecure: StateFlow<Boolean> = _isSecure.asStateFlow()

    // Back & forward state for buttons
    private val _canGoBack = MutableStateFlow(false)
    val _canGoForward = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    // Bookmarks and History from database
    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow representing command signals to the WebView
    private val _commands = MutableSharedFlow<BrowserCommand>(extraBufferCapacity = 64)
    val commands: SharedFlow<BrowserCommand> = _commands.asSharedFlow()

    // Check if current page is bookmarked
    val isCurrentBookmarked: StateFlow<Boolean> = _urlInput
        .flatMapLatest { url ->
            repository.isBookmarked(url)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setUrlInput(url: String) {
        _urlInput.value = url
    }

    fun handleSearchOrUrl(input: String) {
        val cleanInput = input.trim()
        if (cleanInput.isEmpty()) return

        val targetUrl = if (cleanInput.startsWith("http://") || cleanInput.startsWith("https://")) {
            cleanInput
        } else if (cleanInput.contains(".") && !cleanInput.contains(" ")) {
            "https://$cleanInput"
        } else {
            val encodedQuery = try {
                URLEncoder.encode(cleanInput, "UTF-8")
            } catch (e: Exception) {
                cleanInput
            }
            "https://www.google.com/search?q=$encodedQuery"
        }

        // Update selected tab URL
        updateActiveTabUrl(targetUrl)
        _urlInput.value = targetUrl
        viewModelScope.launch {
            _commands.emit(BrowserCommand.LoadUrl(targetUrl))
        }
    }

    private fun updateActiveTabUrl(url: String) {
        val currentActiveId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentActiveId) {
                tab.copy(url = url)
            } else {
                tab
            }
        }
    }

    fun updateActiveTabTitle(title: String) {
        val currentActiveId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentActiveId) {
                tab.copy(title = title.ifEmpty { "صفحة جديدة" })
            } else {
                tab
            }
        }
    }

    fun onPageStarted(url: String) {
        _isLoading.value = true
        _loadingProgress.value = 5 // Initial progress indicator jump
        _urlInput.value = url
        _isSecure.value = url.startsWith("https://")
        _blockedAdsCount.value = 0
        updateActiveTabUrl(url)
    }

    fun onPageFinished(url: String, title: String) {
        _isLoading.value = false
        _loadingProgress.value = 100
        _urlInput.value = url
        _isSecure.value = url.startsWith("https://")
        updateActiveTabUrl(url)
        if (title.isNotEmpty()) {
            updateActiveTabTitle(title)
        }

        // Save to local Room history if NOT in incognito mode
        if (!_isIncognito.value) {
            viewModelScope.launch {
                repository.addHistoryEntry(title = title.ifEmpty { url }, url = url)
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        _loadingProgress.value = progress
        if (progress >= 100) {
            _isLoading.value = false
        } else {
            _isLoading.value = true
        }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _canGoBack.value = canGoBack
        _canGoForward.value = canGoForward
    }

    fun goBack() {
        viewModelScope.launch {
            _commands.emit(BrowserCommand.GoBack)
        }
    }

    fun goForward() {
        viewModelScope.launch {
            _commands.emit(BrowserCommand.GoForward)
        }
    }

    fun reload() {
        viewModelScope.launch {
            _commands.emit(BrowserCommand.Reload)
        }
    }

    fun stop() {
        viewModelScope.launch {
            _commands.emit(BrowserCommand.StopLoading)
        }
    }

    // Bookmarks management
    fun toggleBookmark() {
        val currentUrl = _urlInput.value
        viewModelScope.launch {
            val bookmarked = repository.isBookmarkedSync(currentUrl)
            if (bookmarked) {
                repository.removeBookmarkByUrl(currentUrl)
            } else {
                // Find title from active tab
                val title = _tabs.value.find { it.id == _activeTabId.value }?.title ?: currentUrl
                repository.addBookmark(title, currentUrl)
            }
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    fun deleteHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.removeHistoryEntry(entry)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Tabs action
    fun createNewTab(url: String = "https://www.google.com") {
        val newTab = BrowserTab(url = url, title = "علامة تبويب جديدة")
        _tabs.value = _tabs.value + newTab
        switchTab(newTab.id)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        // Don't close if it's the last tab, just reset it
        if (currentTabs.size == 1) {
            val resetTab = BrowserTab()
            _tabs.value = listOf(resetTab)
            switchTab(resetTab.id)
            return
        }

        val tabToClose = currentTabs.find { it.id == tabId } ?: return
        val activeId = _activeTabId.value
        val newTabs = currentTabs.filter { it.id != tabId }
        _tabs.value = newTabs

        if (activeId == tabId) {
            // Pick another tab to make active
            switchTab(newTabs.last().id)
        }
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        val tab = _tabs.value.find { it.id == tabId } ?: return
        _urlInput.value = tab.url
        _isSecure.value = tab.url.startsWith("https://")
        viewModelScope.launch {
            _commands.emit(BrowserCommand.LoadUrl(tab.url))
        }
    }

    fun toggleIncognito() {
        _isIncognito.value = !_isIncognito.value
        // Force reload the current tab to apply the mode cleanly
        viewModelScope.launch {
            _commands.emit(BrowserCommand.Reload)
        }
    }

    fun toggleVpn() {
        val isNowConnected = !_isVpnConnected.value
        _isVpnConnected.value = isNowConnected
        if (isNowConnected) {
            startVpnSimulation()
        } else {
            stopVpnSimulation()
        }
        
        // Reload page to simulate connection refresh over secure bridge
        viewModelScope.launch {
            _commands.emit(BrowserCommand.Reload)
        }
    }

    fun setVpnServer(server: VpnServer) {
        _selectedVpnServer.value = server
        _simulatedPing.value = server.ping
        
        if (_isVpnConnected.value) {
            stopVpnSimulation()
            startVpnSimulation()
            viewModelScope.launch {
                _commands.emit(BrowserCommand.Reload)
            }
        }
    }

    private fun startVpnSimulation() {
        vpnJob?.cancel()
        vpnJob = viewModelScope.launch {
            var count = _simulatedDataUsage.value
            if (count == 0f) {
                count = (1..4).random() + (0..9).random() / 10f
            }
            val currentServer = _selectedVpnServer.value
            while (true) {
                kotlinx.coroutines.delay(1000)
                val increment = (5..65).random() / 1000f
                count += increment
                _simulatedDataUsage.value = count
                val fluctuation = (-3..3).random()
                _simulatedPing.value = (currentServer.ping + fluctuation).coerceAtLeast(4)
            }
        }
    }

    private fun stopVpnSimulation() {
        vpnJob?.cancel()
        vpnJob = null
    }

    fun toggleAdBlocker() {
        _isAdBlockerEnabled.value = !_isAdBlockerEnabled.value
        if (!_isAdBlockerEnabled.value) {
            _blockedAdsCount.value = 0
        }
        viewModelScope.launch {
            _commands.emit(BrowserCommand.Reload)
        }
    }

    fun incrementBlockedAds() {
        _blockedAdsCount.value += 1
    }

    // Download Manager Actions
    fun startDownload(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        contentLength: Long = -1L
    ) {
        val id = UUID.randomUUID().toString()
        val fileName = getFileNameFromUrl(url, contentDisposition)
        
        val targetLength = if (contentLength <= 0) {
            (5 * 1024 * 1024L .. 120 * 1024 * 1024L).random()
        } else {
            contentLength
        }

        val item = DownloadItem(
            id = id,
            fileName = fileName,
            url = url,
            totalBytes = targetLength,
            downloadedBytes = 0L,
            progress = 0,
            status = "DOWNLOADING",
            timestamp = System.currentTimeMillis(),
            speed = "جاري الاتصال...",
            mimeType = mimeType ?: "application/octet-stream"
        )

        viewModelScope.launch {
            repository.addDownload(item)
            runDownloadJob(item)
        }
    }

    fun pauseDownload(id: String) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        viewModelScope.launch {
            val downloadsList = allDownloads.value
            val foundItem = downloadsList.find { it.id == id } ?: return@launch
            val updated = foundItem.copy(status = "PAUSED", speed = "موقف مؤقتاً")
            repository.addDownload(updated)
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            val downloadsList = allDownloads.value
            val foundItem = downloadsList.find { it.id == id } ?: return@launch
            val updated = foundItem.copy(status = "DOWNLOADING", speed = "جاري التحميل...")
            repository.addDownload(updated)
            runDownloadJob(updated)
        }
    }

    fun cancelDownload(id: String) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        viewModelScope.launch {
            val downloadsList = allDownloads.value
            val foundItem = downloadsList.find { it.id == id } ?: return@launch
            val updated = foundItem.copy(status = "CANCELED", speed = "ملغى")
            repository.addDownload(updated)
        }
    }

    fun deleteDownload(download: DownloadItem) {
        activeDownloadJobs[download.id]?.cancel()
        activeDownloadJobs.remove(download.id)
        viewModelScope.launch {
            repository.removeDownload(download)
        }
    }

    fun clearAllDownloads() {
        activeDownloadJobs.forEach { (_, job) -> job.cancel() }
        activeDownloadJobs.clear()
        viewModelScope.launch {
            repository.clearAllDownloads()
        }
    }

    private fun runDownloadJob(item: DownloadItem) {
        activeDownloadJobs[item.id]?.cancel()
        
        val job = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            var downloaded = item.downloadedBytes
            val total = item.totalBytes
            val id = item.id
            
            while (downloaded < total) {
                kotlinx.coroutines.delay(1000)
                
                // Real/simulated download increments (speed of 1.5MB/s to 4.5MB/s)
                val speedMb = (15..45).random() / 10.0
                val speedBytes = (1024 * 1024 * speedMb).toLong()
                downloaded = (downloaded + speedBytes).coerceAtMost(total)
                val progress = ((downloaded.toDouble() / total) * 100).toInt()
                
                val speedStr = String.format(java.util.Locale.US, "%.1f MB/s", (speedBytes / (1024 * 1024.0)))
                
                val currentList = allDownloads.value
                val currentItem = currentList.find { it.id == id }
                if (currentItem == null || currentItem.status != "DOWNLOADING") {
                    break
                }
                
                val updatedStatus = if (downloaded >= total) "COMPLETED" else "DOWNLOADING"
                val finalSpeed = if (downloaded >= total) "مكتمل" else speedStr
                
                val updatedItem = currentItem.copy(
                    downloadedBytes = downloaded,
                    progress = progress,
                    status = updatedStatus,
                    speed = finalSpeed
                )
                
                repository.addDownload(updatedItem)
                
                if (downloaded >= total) {
                    activeDownloadJobs.remove(id)
                    break
                }
            }
        }
        
        activeDownloadJobs[item.id] = job
    }

    private fun getFileNameFromUrl(url: String, contentDisposition: String?): String {
        var filename = ""
        if (!contentDisposition.isNullOrEmpty()) {
            val index = contentDisposition.indexOf("filename=")
            if (index > 0) {
                filename = contentDisposition.substring(index + 9).trim().replace("\"", "")
            }
        }
        if (filename.isEmpty()) {
            val lastSlash = url.lastIndexOf('/')
            filename = if (lastSlash > 0 && lastSlash < url.length - 1) {
                url.substring(lastSlash + 1).split('?')[0].split('#')[0]
            } else {
                "download_file"
            }
        }
        try {
            filename = java.net.URLDecoder.decode(filename, "UTF-8")
        } catch (e: Exception) {
            // Keep as is
        }
        if (filename.isEmpty()) filename = "download_file"
        return filename
    }

    override fun onCleared() {
        super.onCleared()
        stopVpnSimulation()
        activeDownloadJobs.forEach { (_, job) -> job.cancel() }
        activeDownloadJobs.clear()
    }
}
