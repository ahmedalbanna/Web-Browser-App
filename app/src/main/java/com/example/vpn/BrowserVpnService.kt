package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor

/**
 * Milestone 5.1 — System-wide VPN stub (flavor `vpn`)
 * TODO v1.4: Implement TUN via VpnService.Builder
 * Requires: FOREGROUND_SERVICE + BIND_VPN_SERVICE (vpn flavor only)
 *
 * Build with: ./gradlew assembleVpnRelease
 * Current status: Stub — logs only, no traffic interception.
 */
class BrowserVpnService : VpnService() {

    companion object {
        const val CHANNEL_ID = "vpn_foreground"
        const val NOTIF_ID = 1001
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"
    }

    private var tunFd: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startVpn()
            ACTION_DISCONNECT -> stopVpn()
            else -> startVpn()
        }
        return START_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(CHANNEL_ID, "VPN", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "System VPN tunnel"
                }
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun startVpn() {
        if (isRunning) return
        try {
            // VpnService.Builder — stub, no real TUN yet
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setSession("المتصفح VPN")
                .setMtu(1500)
            // tunFd = builder.establish() // TODO v1.4: uncomment when flavor enabled + permission granted
            // For now, just show foreground notification without TUN
            val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("المتصفح VPN")
                    .setContentText("نفق وهمي — قيد التطوير v1.4")
                    .setSmallIcon(android.R.drawable.ic_lock_lock)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
                    .setContentTitle("المتصفح VPN")
                    .setContentText("نفق وهمي — قيد التطوير")
                    .setSmallIcon(android.R.drawable.ic_lock_lock)
                    .build()
            }
            startForeground(NOTIF_ID, notif)
            isRunning = true
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopVpn() {
        try { tunFd?.close() } catch (_: Exception) {}
        tunFd = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }
}
