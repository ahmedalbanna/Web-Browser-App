package com.example.ui

sealed class BrowserCommand {
    object GoBack : BrowserCommand()
    object GoForward : BrowserCommand()
    object Reload : BrowserCommand()
    object StopLoading : BrowserCommand()
    data class LoadUrl(val url: String) : BrowserCommand()
    object ExtractImages : BrowserCommand()
}
