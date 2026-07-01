package com.example.ui

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.concurrent.Executor
import android.util.Log

object ProxyHelper {
    private const val TAG = "ProxyHelper"

    fun applyProxy(ip: String, port: String, onComplete: () -> Unit) {
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                val proxyConfig = ProxyConfig.Builder()
                    .addProxyRule("$ip:$port")
                    .addDirect()
                    .build()
                    
                ProxyController.getInstance().setProxyOverride(
                    proxyConfig,
                    Executor { it.run() },
                    Runnable {
                        Log.d(TAG, "Proxy set to $ip:$port")
                        onComplete()
                    }
                )
            } else {
                Log.w(TAG, "Proxy override not supported")
                onComplete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply proxy", e)
            onComplete()
        }
    }

    fun clearProxy(onComplete: () -> Unit) {
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                ProxyController.getInstance().clearProxyOverride(
                    Executor { it.run() },
                    Runnable {
                        Log.d(TAG, "Proxy cleared")
                        onComplete()
                    }
                )
            } else {
                onComplete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear proxy", e)
            onComplete()
        }
    }
}
