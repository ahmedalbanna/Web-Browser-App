package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.data.Bookmark
import com.example.data.HistoryEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WebBrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository

    init {
        val database = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(database.browserDao())
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
}
