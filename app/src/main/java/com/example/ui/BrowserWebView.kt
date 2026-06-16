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
                is BrowserCommand.ExtractImages -> {
                    val js = """
                        (function() {
                            var imgs = document.getElementsByTagName('img');
                            var urls = [];
                            for (var i = 0; i < imgs.length; i++) {
                                var src = imgs[i].src || imgs[i].getAttribute('data-src') || imgs[i].getAttribute('data-lazy-src') || imgs[i].getAttribute('lazy-src');
                                if (src && src.trim().length > 0) {
                                    urls.push(src);
                                }
                            }
                            AndroidImageExtractor.onImagesFound(JSON.stringify(urls));
                        })()
                    """.trimIndent()
                    webViewInstance?.evaluateJavascript(js, null)
                }
                is BrowserCommand.ExtractVideos -> {
                    val js = """
                        (function() {
                            var videos = document.getElementsByTagName('video');
                            var urls = [];
                            for (var i = 0; i < videos.length; i++) {
                                var src = videos[i].src;
                                if (src && src.trim().length > 0) {
                                    urls.push({src: src, title: videos[i].title || 'فيديو صفحة الويب'});
                                }
                                var sources = videos[i].getElementsByTagName('source');
                                for (var j = 0; j < sources.length; j++) {
                                    var sSrc = sources[j].src;
                                    if (sSrc && sSrc.trim().length > 0) {
                                        urls.push({src: sSrc, title: videos[i].title || 'فيديو صفحة الويب'});
                                    }
                                }
                            }
                            var iframes = document.getElementsByTagName('iframe');
                            for (var i = 0; i < iframes.length; i++) {
                                var src = iframes[i].src;
                                if (src && (src.includes('youtube.com/embed/') || src.includes('youtu.be/') || src.includes('vimeo.com/video/'))) {
                                    urls.push({src: src, title: 'فيديو يوتيوب أو ميديا مدمجة'});
                                }
                            }
                            AndroidVideoExtractor.onVideosFound(JSON.stringify(urls));
                        })()
                    """.trimIndent()
                    webViewInstance?.evaluateJavascript(js, null)
                }
                is BrowserCommand.SavePage -> {
                    webViewInstance?.let { webView ->
                        val context = webView.context
                        val savedPagesDir = java.io.File(context.filesDir, "saved_pages")
                        if (!savedPagesDir.exists()) {
                            savedPagesDir.mkdirs()
                        }
                        val fileName = "page_${System.currentTimeMillis()}.mhtml"
                        val destFile = java.io.File(savedPagesDir, fileName)
                        webView.saveWebArchive(destFile.absolutePath, false) { savedPath ->
                            if (savedPath != null) {
                                viewModel.onPageSavedSuccessfully(
                                    title = command.title,
                                    url = command.url,
                                    filePath = savedPath
                                )
                                android.widget.Toast.makeText(context, "تم حفظ الصفحة بنجاح للقراءة دون إنترنت", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "فشل حفظ الصفحة", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
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

                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onImagesFound(urlsJson: String) {
                        try {
                            val jsonArray = org.json.JSONArray(urlsJson)
                            val urls = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                val url = jsonArray.getString(i)
                                if (url.startsWith("http") || url.startsWith("data:image")) {
                                    urls.add(url)
                                }
                            }
                            viewModel.setExtractedImages(urls)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, "AndroidImageExtractor")

                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onVideosFound(videosJson: String) {
                        try {
                            val jsonArray = org.json.JSONArray(videosJson)
                            val videos = mutableListOf<com.example.data.ExtractedVideo>()
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val src = obj.getString("src")
                                val title = obj.optString("title", "فيديو صفحة الويب")
                                if (src.startsWith("http") || src.contains("youtube.com") || src.contains("youtu.be")) {
                                    videos.add(com.example.data.ExtractedVideo(src, title))
                                }
                            }
                            viewModel.setExtractedVideos(videos)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, "AndroidVideoExtractor")

                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    viewModel.startDownload(url, contentDisposition, mimetype, contentLength)
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false // Force links to load inside this WebView
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): android.webkit.WebResourceResponse? {
                        val isBlockedEnabled = viewModel.isAdBlockerEnabled.value
                        if (isBlockedEnabled) {
                            val url = request?.url?.toString() ?: ""
                            if (AdBlockerHelper.isAdUrl(url)) {
                                viewModel.incrementBlockedAds()
                                return android.webkit.WebResourceResponse(
                                    "text/javascript",
                                    "UTF-8",
                                    java.io.ByteArrayInputStream("".toByteArray())
                                )
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
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
                        // Injects CSS to hide ad containers if ad blocker is active
                        if (viewModel.isAdBlockerEnabled.value) {
                            val hideAdsCss = """
                                (function() {
                                    var style = document.createElement('style');
                                    style.type = 'text/css';
                                    style.innerHTML = 'img[src*="ad"], iframe[src*="ad"], div[class*="ad-"], div[class*="ads-"], div[id*="ad-"], div[id*="ads-"], .ad, .ads, .ad-banner, .ad-box, .ad-card, .ad-container, .google-panel, #google_ads_iframe, .sponsor, .sponsored, [class*="sponsored"] { display: none !important; height: 0 !important; width: 0 !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }';
                                    document.head.appendChild(style);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(hideAdsCss, null)
                        }
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

object AdBlockerHelper {
    private val adKeywords = hashSetOf(
        "doubleclick.net", "googleads", "googlesyndication.com", "googleadservices.com",
        "adservices.google", "adnxs.com", "moatads.com", "taboola.com", "outbrain.com",
        "scorecardresearch.com", "pubmatic.com", "rubiconproject.com", "openx.net",
        "amazon-adsystem.com", "criteo.com", "adcolony.com", "adsrvr.org", "vungle.com",
        "applovin.com", "unityads", "admob", "bidswitch.net", "casalemedia.com",
        "popads.net", "onclickads.net", "propellerads.com", "exoclick.com", "adsterra.com",
        "mgid.com", "revcontent.com", "buysellads.com", "smartadserver.com", "advertising.com",
        "/pagead/", "/popunder", "hotjar.com", "addthis.com", "quantserve.com",
        "banners", "adserver", "adsystem", "adservice"
    )

    fun isAdUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        for (keyword in adKeywords) {
            if (lowerUrl.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
