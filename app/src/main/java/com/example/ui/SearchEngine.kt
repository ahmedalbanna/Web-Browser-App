package com.example.ui

import java.net.URLEncoder

enum class SearchEngine(
    val displayName: String,
    val displayNameAr: String,
    val icon: String,
    private val searchUrlTemplate: String
) {
    GOOGLE("Google", "جوجل", "G", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "دك دك جو", "🦆", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "بينج", "B", "https://www.bing.com/search?q=%s");

    fun buildSearchUrl(query: String): String {
        val encoded = try { URLEncoder.encode(query, "UTF-8") } catch (_: Exception) { query }
        return searchUrlTemplate.replace("%s", encoded)
    }

    companion object {
        fun fromName(name: String): SearchEngine =
            entries.find { it.name.equals(name, true) || it.displayName.equals(name, true) } ?: GOOGLE
    }
}
