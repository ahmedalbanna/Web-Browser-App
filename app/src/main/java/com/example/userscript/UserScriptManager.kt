package com.example.userscript

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Milestone 5.3 — Manager for userscripts
 * TODO v1.4: Persist via Room user_scripts table, inject in BrowserWebView
 */
object UserScriptManager {

    private val _scripts = MutableStateFlow(UserScript.PRESETS)
    val scripts: StateFlow<List<UserScript>> = _scripts.asStateFlow()

    fun getEnabledForUrl(url: String): List<UserScript> =
        _scripts.value.filter { it.enabled && it.matchesUrl(url) }

    fun toggle(id: Long) {
        _scripts.value = _scripts.value.map { if (it.id == id) it.copy(enabled = !it.enabled) else it }
    }

    fun add(script: UserScript) {
        _scripts.value = _scripts.value + script.copy(id = (_scripts.value.maxOfOrNull { it.id } ?: 0) + 1)
    }

    fun remove(id: Long) {
        _scripts.value = _scripts.value.filterNot { it.id == id }
    }

    fun buildInjectionJs(url: String): String {
        val enabled = getEnabledForUrl(url)
        if (enabled.isEmpty()) return ""
        val js = enabled.joinToString("\n") { it.jsCode }
        val css = enabled.mapNotNull { it.cssCode.takeIf { c -> c.isNotBlank() } }.joinToString("\n")
        return buildString {
            if (css.isNotBlank()) {
                append("""var s=document.createElement('style');s.innerHTML=`$css`;document.head.appendChild(s);""")
            }
            if (js.isNotBlank()) append(js)
        }
    }
}
