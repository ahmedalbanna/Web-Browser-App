package com.example.data

import android.content.Context
import com.example.ui.GeminiTranslateService
import com.example.ui.ProxyApiClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Manual DI container – Milestone 3 v1.2
 * Provides singletons for repository, network clients and services.
 * No Hilt/Koin to keep APK lean; can be swapped later.
 */
class AppContainer(private val context: Context) {

    val database: BrowserDatabase by lazy {
        BrowserDatabase.getDatabase(context)
    }

    val repository: BrowserRepository by lazy {
        BrowserRepository(database.browserDao())
    }

    val proxyApi by lazy { ProxyApiClient.api }

    val geminiService: GeminiTranslateService by lazy { GeminiTranslateService() }

    val downloadClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}
