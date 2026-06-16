package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
@Composable
fun BrowserWebView(
    viewModel: WebBrowserViewModel,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val isIncognito by viewModel.isIncognito.collectAsState()

    // Navigation state mapping
    val canGoBack by viewModel.canGoBack.collectAsState()
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    // React to private browsing (Incognito) updates
    LaunchedEffect(isIncognito) {
        webViewInstance?.let { webView ->
            if (isIncognito) {
                webView.clearHistory()
                webView.clearCache(true)
                android.webkit.CookieManager.getInstance().removeAllCookies(null)
            }
        }
    }

    // Listen to ViewModel navigation commands
    LaunchedEffect(viewModel.commands) {
        viewModel.commands.collectLatest { command ->
            when (command) {
                is BrowserCommand.GoBack -> webViewInstance?.goBack()
                is BrowserCommand.GoForward -> webViewInstance?.goForward()
                is BrowserCommand.Reload -> webViewInstance?.reload()
                is BrowserCommand.StopLoading -> webViewInstance?.stopLoading()
                is BrowserCommand.LoadUrl -> {
                    if (webViewInstance?.url != command.url) {
                        webViewInstance?.loadUrl(command.url)
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    setBuiltInZoomControls(true)
                    setDisplayZoomControls(false)
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false // Force links to load inside this WebView
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        url?.let { viewModel.onPageStarted(it) }
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() ?: false,
                            canGoForward = view?.canGoForward() ?: false
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        url?.let { viewModel.onPageFinished(it, view?.title ?: "") }
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() ?: false,
                            canGoForward = view?.canGoForward() ?: false
                        )
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        viewModel.onProgressChanged(newProgress)
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() ?: false,
                            canGoForward = view?.canGoForward() ?: false
                        )
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        title?.let { viewModel.updateActiveTabTitle(it) }
                    }
                }

                webViewInstance = this
                // Load active tab initially
                val activeId = viewModel.activeTabId.value
                val activeTab = viewModel.tabs.value.find { it.id == activeId }
                loadUrl(activeTab?.url ?: "https://www.google.com")
            }
        },
        update = { webView ->
            webViewInstance = webView
        },
        onRelease = {
            webViewInstance = null
        }
    )
}
