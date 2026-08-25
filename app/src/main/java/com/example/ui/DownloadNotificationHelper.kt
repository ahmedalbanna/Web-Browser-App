package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File

object DownloadNotificationHelper {
    private const val CHANNEL_ID = "downloads_channel"
    private const val CHANNEL_NAME = "التحميلات"
    const val CHANNEL_DESC = "إشعارات تقدم التحميلات"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                    description = CHANNEL_DESC
                    setShowBadge(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun showProgress(context: Context, id: String, fileName: String, progress: Int, downloaded: Long, total: Long) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = id.hashCode()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(fileName)
            .setContentText("$progress% • ${formatBytes(downloaded)}/${formatBytes(total)}")
            .setProgress(100, progress, total <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        nm.notify(notifId, notification)
    }

    fun showCompleted(context: Context, id: String, fileName: String, file: File) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = id.hashCode()
        // Build pending intent to open file via FileProvider
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) { null }
        val intent = uri?.let {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(it, guessMimeType(fileName))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        val pending = intent?.let {
            PendingIntent.getActivity(context, notifId, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("اكتمل التحميل")
            .setContentText(fileName)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
        if (pending != null) {
            builder.setContentIntent(pending)
            builder.addAction(android.R.drawable.ic_menu_view, "فتح", pending)
        }
        nm.notify(notifId, builder.build())
    }

    fun showFailed(context: Context, id: String, fileName: String, reason: String) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = id.hashCode()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("فشل التحميل")
            .setContentText("$fileName: $reason")
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        nm.notify(notifId, notification)
    }

    fun cancel(context: Context, id: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id.hashCode())
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = listOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).coerceAtLeast(0.0).toInt().coerceAtMost(units.size - 1)
        return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "zip" -> "application/zip"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
