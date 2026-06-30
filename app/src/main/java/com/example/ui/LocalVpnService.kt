package com.example.ui

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.*

class LocalVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.example.ui.START_VPN"
        const val ACTION_DISCONNECT = "com.example.ui.STOP_VPN"
        private const val TAG = "LocalVpnService"
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_CONNECT -> {
                    startVpn()
                }
                ACTION_DISCONNECT -> {
                    stopVpn()
                }
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning.get()) return
        
        try {
            val builder = Builder()
                .setSession(getString(com.example.R.string.app_name))
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")

            vpnInterface = builder.establish()
            isRunning.set(true)
            
            Log.d(TAG, "VPN Interface established")
            
            startTrafficLoop()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }
    
    private fun startTrafficLoop() {
        scope.launch {
            try {
                val fileDescriptor = vpnInterface?.fileDescriptor ?: return@launch
                val inputStream = FileInputStream(fileDescriptor)
                val packet = ByteBuffer.allocate(32767)

                while (isRunning.get() && isActive) {
                    val length = inputStream.read(packet.array())
                    if (length > 0) {
                        packet.limit(length)
                        packet.clear()
                    } else {
                        delay(10)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "VPN traffic loop error", e)
                if (isRunning.get()) {
                    stopVpn()
                }
            }
        }
    }

    private fun stopVpn() {
        isRunning.set(false)
        scope.coroutineContext.cancelChildren()
        
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        
        Log.d(TAG, "VPN Stopped")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        scope.cancel()
    }
}
