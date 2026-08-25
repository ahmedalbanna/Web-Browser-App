package com.example.userscript

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Milestone 5.3 — Userscripts (JS injection presets)
 * TODO v1.4: Wire to BrowserWebView onPageFinished via UserScriptManager
 */
@Entity(tableName = "user_scripts")
data class UserScript(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val jsCode: String = "",
    val cssCode: String = "",
    val matches: String = ".*", // regex for URL
    val runAt: RunAt = RunAt.DOCUMENT_END,
    val enabled: Boolean = true,
    val isPreset: Boolean = false
) {
    enum class RunAt { DOCUMENT_START, DOCUMENT_END, DOCUMENT_IDLE }

    fun matchesUrl(url: String): Boolean = try {
        Regex(matches).containsMatchIn(url)
    } catch (_: Exception) { false }

    companion object {
        val PRESETS = listOf(
            UserScript(
                name = "Dark Reader",
                description = "وضع ليلي — عكس الألوان",
                jsCode = """document.documentElement.style.filter='invert(1) hue-rotate(180deg)'; document.body.style.background='#111';""",
                matches = ".*",
                isPreset = true
            ),
            UserScript(
                name = "Readability",
                description = "تبسيط المقال — مستخرج من وضع القراءة",
                jsCode = """var s=document.createElement('style');s.innerHTML='body{max-width:800px;margin:auto!important;line-height:1.9!important;font-family:serif!important} header,nav,footer,.ad{display:none!important}';document.head.appendChild(s);""",
                isPreset = true
            ),
            UserScript(
                name = "AdGuard Extra",
                description = "حظر إضافي — يخفي عناصر الإعلانات المتقدمة",
                cssCode = """.ad, .ads, [class*="sponsored"], iframe[src*="ad"] { display:none!important }""",
                isPreset = true
            )
        )
    }
}
