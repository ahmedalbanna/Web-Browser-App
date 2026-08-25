package com.example.ui

/**
 * Consolidated UI state — Milestone 3 v1.2
 * Replaces scattered MutableStateFlows with a single source of truth for top-level chrome.
 * Lists (bookmarks/history/downloads) remain as separate Flows per roadmap.
 */
data class BrowserUiState(
    val isIncognito: Boolean = false,
    val isVpnConnected: Boolean = false,
    val selectedVpnServer: VpnServer = vpnServersList[0],
    val simulatedPing: Int = 22,
    val simulatedDataUsage: Float = 0f,
    val isAdBlockerEnabled: Boolean = true,
    val blockedAdsCount: Int = 0,
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val isSecure: Boolean = true,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val urlInput: String = "https://www.google.com",
    val activeTabId: String = "",
    val errorMessage: String? = null,
    val isTranslating: Boolean = false,
    val translatedResult: String = "",
    val translationError: String? = null,
    val isExtractingImages: Boolean = false,
    val isExtractingVideos: Boolean = false,
    // Milestone 4 — v1.3 polish
    val selectedSearchEngine: SearchEngine = SearchEngine.GOOGLE,
    val isReaderMode: Boolean = false,
    val textZoom: Int = 100,
    val isFindBarVisible: Boolean = false,
    val findQuery: String = "",
    val findActiveMatch: Int = 0,
    val findMatchCount: Int = 0
)
