package com.example.ui

sealed class BrowserCommand {
    object GoBack : BrowserCommand()
    object GoForward : BrowserCommand()
    object Reload : BrowserCommand()
    object StopLoading : BrowserCommand()
    data class LoadUrl(val url: String) : BrowserCommand()
    object ExtractImages : BrowserCommand()
    object ExtractVideos : BrowserCommand()
    data class SavePage(val title: String, val url: String) : BrowserCommand()
    object ToggleReaderMode : BrowserCommand()
    data class FindInPage(val query: String) : BrowserCommand()
    object FindNext : BrowserCommand()
    object FindPrevious : BrowserCommand()
    object ClearFind : BrowserCommand()
    data class SetTextZoom(val zoom: Int) : BrowserCommand()
}
