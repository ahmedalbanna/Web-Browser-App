package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.BrowserApplication
import com.example.data.BrowserRepository

class BrowserViewModelFactory(
    private val application: Application,
    private val repository: BrowserRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebBrowserViewModel::class.java)) {
            val repo = repository
                ?: (application as? BrowserApplication)?.container?.repository
                ?: com.example.data.BrowserRepository(
                    com.example.data.BrowserDatabase.getDatabase(application).browserDao()
                )
            // downloadClient and geminiService will be resolved lazily inside VM via container if available
            return WebBrowserViewModel(application, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class $modelClass")
    }
}
