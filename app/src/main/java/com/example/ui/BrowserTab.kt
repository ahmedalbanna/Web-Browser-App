package com.example.ui

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "علامة تبويب جديدة",
    val url: String = "https://www.google.com"
)
