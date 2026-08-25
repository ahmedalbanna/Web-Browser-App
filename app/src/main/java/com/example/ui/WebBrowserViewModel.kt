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
import kotlinx.coroutines.isActive
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
    private val downloadClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val allDownloads: StateFlow<List<DownloadItem>> = repository.allDownloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchProxies()
        viewModelScope.launch {
            repository.allDownloads.first().forEach { item ->
                if (item.status == "DOWNLOADING") {
                    repository.addDownload(item.copy(status = "PAUSED", speed = "موقف مؤقتاً"))
                }
            }
        }
    }

    private fun fetchProxies() {
        viewModelScope.launch {
            try {
                val response = ProxyApiClient.api.getProxies()
                val newServers = response.data.mapNotNull { proxy ->
                    if (proxy.country.isNullOrEmpty() || proxy.latency == null) return@mapNotNull null
                    VpnServer(
                        name = proxy.country,
                        countryCode = proxy.country,
                        flag = "🌐",
                        ipAddress = proxy.ip,
                        port = proxy.port,
                        ping = proxy.latency.toInt(),
                        speed = "High Mbps"
                    )
                }.filter { it.ping > 0 }.take(20)

                if (newServers.isNotEmpty()) {
                    _availableVpnServers.value = newServers
                    _selectedVpnServer.value = newServers[0]
                }
            } catch (e: Exception) {
                // Fallback to defaults
                showError("فشل في جلب الخوادم الوكيلة، تم استخدام الخوادم الافتراضية")
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

    private val _availableVpnServers = MutableStateFlow(vpnServersList)
    val availableVpnServers: StateFlow<List<VpnServer>> = _availableVpnServers.asStateFlow()

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
    private val _canGoForward = MutableStateFlow(false)
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

    // AI Translation States
    private val translateService = GeminiTranslateService()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translatedResult = MutableStateFlow("")
    val translatedResult: StateFlow<String> = _translatedResult.asStateFlow()

    private val _translationError = MutableStateFlow<String?>(null)
    val translationError: StateFlow<String?> = _translationError.asStateFlow()

    // Image Browser / Extractor states
    private val _extractedImages = MutableStateFlow<List<String>>(emptyList())
    val extractedImages: StateFlow<List<String>> = _extractedImages.asStateFlow()

    private val _errorMessages = MutableStateFlow<String?>(null)
    val errorMessages: StateFlow<String?> = _errorMessages.asStateFlow()

    fun showError(message: String) {
        _errorMessages.value = message
    }

    fun dismissError() {
        _errorMessages.value = null
    }

    private val _isExtractingImages = MutableStateFlow(false)
    val isExtractingImages: StateFlow<Boolean> = _isExtractingImages.asStateFlow()

    fun triggerImageExtraction() {
        viewModelScope.launch {
            _isExtractingImages.value = true
            _extractedImages.value = emptyList()
            _commands.emit(BrowserCommand.ExtractImages)
            // Safety timeout after 5 seconds to not get stuck endlessly in loading state if no callback triggers
            kotlinx.coroutines.delay(5000)
            if (_isExtractingImages.value) {
                _isExtractingImages.value = false
            }
        }
    }

    fun setExtractedImages(urls: List<String>) {
        _extractedImages.value = urls
        _isExtractingImages.value = false
    }

    fun clearExtractedImages() {
        _extractedImages.value = emptyList()
        _isExtractingImages.value = false
    }

    // Video Browser / Extractor states
    private val _extractedVideos = MutableStateFlow<List<com.example.data.ExtractedVideo>>(emptyList())
    val extractedVideos: StateFlow<List<com.example.data.ExtractedVideo>> = _extractedVideos.asStateFlow()

    private val _isExtractingVideos = MutableStateFlow(false)
    val isExtractingVideos: StateFlow<Boolean> = _isExtractingVideos.asStateFlow()

    fun triggerVideoExtraction() {
        viewModelScope.launch {
            _isExtractingVideos.value = true
            _extractedVideos.value = emptyList()
            _commands.emit(BrowserCommand.ExtractVideos)
            // Safety timeout after 5 seconds to not get stuck endlessly in loading state if no callback triggers
            kotlinx.coroutines.delay(5000)
            if (_isExtractingVideos.value) {
                _isExtractingVideos.value = false
            }
        }
    }

    fun setExtractedVideos(videos: List<com.example.data.ExtractedVideo>) {
        _extractedVideos.value = videos
        _isExtractingVideos.value = false
    }

    fun clearExtractedVideos() {
        _extractedVideos.value = emptyList()
        _isExtractingVideos.value = false
    }

    // Saved Pages state and interactions
    val allSavedPages: StateFlow<List<com.example.data.SavedPage>> = repository.allSavedPages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun triggerSavePage() {
        val activeId = _activeTabId.value
        val activeTab = _tabs.value.find { it.id == activeId }
        val title = activeTab?.title?.ifEmpty { activeTab.url } ?: "صفحة غير معروفة"
        val url = activeTab?.url ?: ""
        if (url.isNotEmpty() && !url.startsWith("about:blank")) {
            viewModelScope.launch {
                _commands.emit(BrowserCommand.SavePage(title, url))
            }
        }
    }

    fun onPageSavedSuccessfully(title: String, url: String, filePath: String) {
        viewModelScope.launch {
            repository.addSavedPage(title, url, filePath)
        }
    }

    fun deleteSavedPage(page: com.example.data.SavedPage) {
        viewModelScope.launch {
            try {
                val file = java.io.File(page.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.removeSavedPage(page)
        }
    }

    fun clearSavedPages() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val savedPagesDir = java.io.File(context.filesDir, "saved_pages")
                if (savedPagesDir.exists()) {
                    savedPagesDir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.clearAllSavedPages()
        }
    }

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

    private var startVpnBytes = 0L
    private var vpnSwitchJob: kotlinx.coroutines.Job? = null
    private var isTrafficStatsSupported = true

    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { p -> p.toIntOrNull()?.let { it in 0..255 } == true }
    }

    private fun isValidPort(port: String): Boolean {
        val p = port.toIntOrNull() ?: return false
        return p in 1..65535
    }

    private fun isProxySupported(): Boolean {
        return try {
            androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.PROXY_OVERRIDE)
        } catch (_: Exception) { false }
    }

    fun toggleVpn() {
        if (!isProxySupported()) {
            showError("عذراً، جهازك لا يدعم ميزة البروكسي (WebView قديم).")
            return
        }
        val server = _selectedVpnServer.value
        if (!isValidIp(server.ipAddress) || !isValidPort(server.port)) {
            showError("عنوان الخادم أو المنفذ غير صالح: ${server.ipAddress}:${server.port}")
            return
        }
        val isNowConnected = !_isVpnConnected.value
        _isVpnConnected.value = isNowConnected
        // Debounce rapid toggles
        vpnSwitchJob?.cancel()
        vpnSwitchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            if (isNowConnected) {
                ProxyHelper.applyProxy(server.ipAddress, server.port) {
                    viewModelScope.launch { _commands.emit(BrowserCommand.Reload) }
                }
                startVpnSimulation()
            } else {
                ProxyHelper.clearProxy {
                    viewModelScope.launch { _commands.emit(BrowserCommand.Reload) }
                }
                stopVpnSimulation()
            }
        }
    }

    fun setVpnServer(server: VpnServer) {
        if (!isValidIp(server.ipAddress) || !isValidPort(server.port)) {
            showError("خادم غير صالح: ${server.ipAddress}:${server.port}")
            return
        }
        _selectedVpnServer.value = server
        _simulatedPing.value = server.ping
        
        if (_isVpnConnected.value) {
            if (!isProxySupported()) {
                showError("البروكسي غير مدعوم على هذا الجهاز.")
                return
            }
            vpnSwitchJob?.cancel()
            vpnSwitchJob = viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                ProxyHelper.applyProxy(server.ipAddress, server.port) {
                    viewModelScope.launch { _commands.emit(BrowserCommand.Reload) }
                }
                stopVpnSimulation()
                startVpnSimulation()
            }
        }
    }

    private fun startVpnSimulation() {
        vpnJob?.cancel()
        val uid = android.os.Process.myUid()
        val rx = android.net.TrafficStats.getUidRxBytes(uid)
        val tx = android.net.TrafficStats.getUidTxBytes(uid)
        isTrafficStatsSupported = rx != android.net.TrafficStats.UNSUPPORTED.toLong() && tx != android.net.TrafficStats.UNSUPPORTED.toLong()
        startVpnBytes = if (isTrafficStatsSupported) rx + tx else 0L
        // reset usage on new connection
        _simulatedDataUsage.value = 0f
        
        vpnJob = viewModelScope.launch {
            val currentServer = _selectedVpnServer.value
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (isTrafficStatsSupported) {
                    val curRx = android.net.TrafficStats.getUidRxBytes(uid)
                    val curTx = android.net.TrafficStats.getUidTxBytes(uid)
                    if (curRx != android.net.TrafficStats.UNSUPPORTED.toLong() && curTx != android.net.TrafficStats.UNSUPPORTED.toLong()) {
                        val currentBytes = curRx + curTx
                        val diff = (currentBytes - startVpnBytes).coerceAtLeast(0L)
                        val actualUsageMb = diff / (1024f * 1024f)
                        if (actualUsageMb > 0f) {
                            _simulatedDataUsage.value = actualUsageMb
                        } else {
                            // fallback small increment to show activity
                            _simulatedDataUsage.value += ((5..15).random() / 1000f)
                        }
                    } else {
                        _simulatedDataUsage.value += ((5..15).random() / 1000f)
                    }
                } else {
                    // Device does not support TrafficStats — simulated only
                    _simulatedDataUsage.value += ((5..15).random() / 1000f)
                }
                
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
        _blockedAdsCount.update { it + 1 }
    }

    // Download Manager Actions — real network via OkHttp streaming to internal storage
    fun startDownload(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        contentLength: Long = -1L
    ) {
        val id = UUID.randomUUID().toString()
        val rawName = getFileNameFromUrl(url, contentDisposition)
        val fileName = sanitizeFileName(rawName)

        // Use provided length if >0, otherwise 0 = unknown until response
        val initialLength = if (contentLength > 0) contentLength else 0L

        val item = DownloadItem(
            id = id,
            fileName = fileName,
            url = url,
            totalBytes = initialLength,
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
            val foundItem = allDownloads.value.find { it.id == id } ?: return@launch
            // If already completed/canceled, ignore
            if (foundItem.status != "DOWNLOADING") return@launch
            val updated = foundItem.copy(status = "PAUSED", speed = "موقف مؤقتاً")
            repository.addDownload(updated)
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            val foundItem = allDownloads.value.find { it.id == id } ?: return@launch
            if (foundItem.status == "COMPLETED") return@launch
            val updated = foundItem.copy(status = "DOWNLOADING", speed = "جاري الاستئناف...")
            repository.addDownload(updated)
            runDownloadJob(updated)
        }
    }

    fun cancelDownload(id: String) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        viewModelScope.launch {
            val foundItem = allDownloads.value.find { it.id == id } ?: return@launch
            val updated = foundItem.copy(status = "CANCELED", speed = "ملغى")
            repository.addDownload(updated)
            // delete partial file
            try {
                val ctx = getApplication<Application>()
                val f = java.io.File(java.io.File(ctx.filesDir, "downloads"), foundItem.fileName)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
    }

    fun deleteDownload(download: DownloadItem) {
        activeDownloadJobs[download.id]?.cancel()
        activeDownloadJobs.remove(download.id)
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                val f = java.io.File(java.io.File(ctx.filesDir, "downloads"), download.fileName)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
            repository.removeDownload(download)
        }
    }

    fun clearAllDownloads() {
        activeDownloadJobs.forEach { (_, job) -> job.cancel() }
        activeDownloadJobs.clear()
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                val dir = java.io.File(ctx.filesDir, "downloads")
                if (dir.exists()) dir.listFiles()?.forEach { it.delete() }
            } catch (_: Exception) {}
            repository.clearAllDownloads()
        }
    }

    private fun runDownloadJob(item: DownloadItem) {
        activeDownloadJobs[item.id]?.cancel()

        val job = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            val downloadDir = java.io.File(appCtx.filesDir, "downloads").apply { if (!exists()) mkdirs() }
            val destFile = resolveDestFile(downloadDir, item.fileName)

            // If this is a resume, we may have partial file already
            var downloaded = if (item.downloadedBytes > 0 && destFile.exists()) {
                minOf(item.downloadedBytes, destFile.length())
            } else {
                // fresh download – ensure empty file
                if (item.downloadedBytes == 0L && destFile.exists()) destFile.delete()
                0L
            }
            var totalBytes = item.totalBytes
            val id = item.id

            var input: java.io.InputStream? = null
            var output: java.io.OutputStream? = null
            var response: okhttp3.Response? = null

            try {
                val reqBuilder = okhttp3.Request.Builder().url(item.url).get()
                if (downloaded > 0) {
                    reqBuilder.header("Range", "bytes=$downloaded-")
                }
                // Some servers require UA
                reqBuilder.header("User-Agent", "Mozilla/5.0 (Android) المتصفح/1.0")

                val request = reqBuilder.build()
                response = downloadClient.newCall(request).execute()

                if (!response.isSuccessful && response.code != 206) {
                    // handle 416 (range not satisfiable) as completed
                    if (response.code == 416) {
                        val completed = allDownloads.value.find { it.id == id }?.copy(
                            status = "COMPLETED", progress = 100, speed = "مكتمل"
                        )
                        if (completed != null) repository.addDownload(completed)
                        return@launch
                    }
                    throw java.io.IOException("HTTP ${response.code} ${response.message}")
                }

                val body = response.body ?: throw java.io.IOException("Empty body")
                val contentLength = body.contentLength()
                val contentRange = response.header("Content-Range")
                val inferredTotal: Long = when {
                    contentRange != null -> {
                        // format: bytes 500-999/2000
                        contentRange.substringAfterLast('/').toLongOrNull() ?: -1L
                    }
                    contentLength != -1L -> if (downloaded > 0) downloaded + contentLength else contentLength
                    else -> totalBytes
                }
                if (inferredTotal > 0) totalBytes = inferredTotal
                val mimeFromServer = body.contentType()?.toString()?.substringBefore(';')?.trim()
                val effectiveMime = mimeFromServer ?: item.mimeType

                // update initial total if we now know it
                if (totalBytes > 0 && totalBytes != item.totalBytes) {
                    val cur = allDownloads.value.find { it.id == id }
                    if (cur != null) repository.addDownload(cur.copy(totalBytes = totalBytes, mimeType = effectiveMime))
                }

                input = body.byteStream()
                // Append if resuming, else overwrite
                output = java.io.FileOutputStream(destFile, downloaded > 0)

                val buffer = ByteArray(8 * 1024)
                var lastUpdateTime = System.currentTimeMillis()
                var bytesSinceUpdate = 0L
                var lastSavedDownloaded = downloaded

                while (true) {
                    // Cooperatively check cancellation / external pause
                    if (!isActive) break
                    val current = allDownloads.value.find { it.id == id }
                    if (current == null || current.status != "DOWNLOADING") break

                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    bytesSinceUpdate += read

                    val now = System.currentTimeMillis()
                    val elapsed = now - lastUpdateTime
                    // Throttle DB writes to ~500ms or on completion
                    if (elapsed >= 500 || downloaded >= totalBytes) {
                        val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0
                        val speedBps = if (elapsed > 0) (bytesSinceUpdate * 1000 / elapsed) else 0L
                        val speedStr = formatSpeed(speedBps)
                        val curItem = allDownloads.value.find { it.id == id } ?: break
                        val toSave = curItem.copy(
                            downloadedBytes = downloaded,
                            totalBytes = if (totalBytes > 0) totalBytes else curItem.totalBytes,
                            progress = progress,
                            speed = speedStr,
                            mimeType = effectiveMime
                        )
                        repository.addDownload(toSave)
                        lastUpdateTime = now
                        bytesSinceUpdate = 0L
                        lastSavedDownloaded = downloaded
                    }
                    // Ensure data flushed periodically
                    if (downloaded - lastSavedDownloaded > 256 * 1024) {
                        output.flush()
                    }
                }
                output.flush()

                val finalItem = allDownloads.value.find { it.id == id }
                if (finalItem != null && finalItem.status == "DOWNLOADING") {
                    val isComplete = totalBytes in 1..downloaded || (totalBytes <= 0 && downloaded > 0)
                    // If server gave us exact total, check; otherwise treat EOF as complete
                    val newStatus = if (isComplete || (totalBytes > 0 && downloaded >= totalBytes)) "COMPLETED" else "FAILED"
                    val finalProgress = if (newStatus == "COMPLETED") 100 else finalItem.progress
                    val finalSpeed = if (newStatus == "COMPLETED") "مكتمل" else "مكتمل مع تحذير"
                    repository.addDownload(finalItem.copy(
                        downloadedBytes = downloaded,
                        totalBytes = if (totalBytes > 0) totalBytes else downloaded,
                        progress = finalProgress,
                        status = newStatus,
                        speed = finalSpeed
                    ))
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Paused/canceled externally – keep DB state as set by pause/cancel
                try { output?.flush() } catch (_: Exception) {}
                throw e
            } catch (e: Exception) {
                val cur = allDownloads.value.find { it.id == id }
                if (cur != null && cur.status == "DOWNLOADING") {
                    repository.addDownload(cur.copy(status = "FAILED", speed = "فشل: ${e.message?.take(60) ?: "خطأ"}"))
                }
                // keep partial file for resume; don't delete
            } finally {
                try { input?.close() } catch (_: Exception) {}
                try { output?.close() } catch (_: Exception) {}
                try { response?.close() } catch (_: Exception) {}
                activeDownloadJobs.remove(id)
            }
        }

        activeDownloadJobs[item.id] = job
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 B/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format(java.util.Locale.US, "%.1f MB/s", mb)
            kb >= 1 -> String.format(java.util.Locale.US, "%.0f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun resolveDestFile(dir: java.io.File, fileName: String): java.io.File {
        var f = java.io.File(dir, fileName)
        if (!f.exists()) return f
        // If file exists but we are not resuming (downloadedBytes==0), make unique name
        val dot = fileName.lastIndexOf('.')
        val base = if (dot > 0) fileName.substring(0, dot) else fileName
        val ext = if (dot > 0) fileName.substring(dot) else ""
        var idx = 1
        while (f.exists() && idx < 100) {
            f = java.io.File(dir, "${base}_$idx$ext")
            idx++
        }
        return f
    }

    private fun sanitizeFileName(name: String): String {
        var n = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (n.isEmpty()) n = "download_file"
        // limit length
        if (n.length > 100) {
            val dot = n.lastIndexOf('.')
            val ext = if (dot > 0) n.substring(dot) else ""
            n = n.take(100 - ext.length) + ext
        }
        return n
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

    // Translate User Custom Text via Gemini
    fun translateText(text: String, sourceLang: String, targetLang: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isTranslating.value = true
            _translationError.value = null
            _translatedResult.value = ""
            try {
                val res = translateService.translateText(text, sourceLang, targetLang)
                if (res == "Error_No_Key") {
                    _translationError.value = "يرجى تهيئة مفتاح API الخاص بـ Gemini في لوحة الأسرار (Secrets) باسم GEMINI_API_KEY لتفعيل الترجمة الذكية."
                } else if (res.startsWith("فشل") || res.startsWith("خطأ") || res.contains("Network") || res.contains("Error")) {
                    _translationError.value = res
                } else {
                    _translatedResult.value = res
                }
            } catch (e: Exception) {
                _translationError.value = "حدث خطأ أثناء الاتصال: ${e.localizedMessage}"
                showError("فشل الترجمة: ${e.localizedMessage}")
            } finally {
                _isTranslating.value = false
            }
        }
    }

    fun clearTranslation() {
        _translatedResult.value = ""
        _translationError.value = null
    }

    // Translate entire web page to Arabic on google translate proxies
    fun translateActiveWebPage() {
        val activeId = _activeTabId.value
        val activeTab = _tabs.value.find { it.id == activeId }
        val currentUrl = activeTab?.url ?: ""
        if (currentUrl.isNotEmpty() && currentUrl.startsWith("http")) {
            if (currentUrl.contains("translate.google.com/translate")) {
                return // avoid dual loop
            }
            val encodedUrl = try {
                java.net.URLEncoder.encode(currentUrl, "UTF-8")
            } catch (e: Exception) {
                currentUrl
            }
            val translatedUrl = "https://translate.google.com/translate?sl=auto&tl=ar&u=$encodedUrl"
            updateActiveTabUrl(translatedUrl)
            _urlInput.value = translatedUrl
            viewModelScope.launch {
                _commands.emit(BrowserCommand.LoadUrl(translatedUrl))
            }
        }
    }
}
