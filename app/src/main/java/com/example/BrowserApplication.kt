package com.example

import android.app.Application
import com.example.data.AppContainer

class BrowserApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
