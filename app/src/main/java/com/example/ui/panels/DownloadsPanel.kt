package com.example.ui.panels


import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.Bookmark
import com.example.data.HistoryEntry
import com.example.data.DownloadItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.content.Intent
import androidx.core.content.FileProvider
import com.example.ui.*
import com.example.data.*

@Composable
fun DownloadsManagerPanel(
    downloads: List<DownloadItem>,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onStartManualDownload: (String) -> Unit,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteDownload: (DownloadItem) -> Unit,
    onClearAllDownloads: () -> Unit
) {
    var manualUrl by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Header with icon & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = currentAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مدير التحميلات والملفات",
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }

            if (downloads.isNotEmpty()) {
                TextButton(
                    onClick = onClearAllDownloads,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "مسح الكل", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct manual download card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, currentBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "إضافة تنزيل مباشر يدوياً",
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualUrl,
                        onValueChange = { manualUrl = it },
                        placeholder = { 
                            Text(
                                "أدخل رابط مباشر (مثال: file.mp4)...", 
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium, 
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = currentLightElement,
                            unfocusedContainerColor = currentLightElement,
                            focusedBorderColor = currentAccent,
                            unfocusedBorderColor = currentBorder,
                            focusedTextColor = currentText,
                            unfocusedTextColor = currentText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualUrl.trim().isNotEmpty()) {
                                onStartManualDownload(manualUrl.trim())
                                manualUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "تنزيل",
                            color = if (isIncognito) Color.Black else Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Downloads List
        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = currentSecText.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد أي تنزيلات حالياً",
                        color = currentText,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سيظهر تفاصيل الملفات، وسرعة تنزيلها، ونسب تقدمها تلقائياً هنا في اللحظة التي تضغط فيها على محتوى تنزيل بمواقع الويب!",
                        color = currentSecText,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloads, key = { it.id }) { item ->
                    DownloadRowItem(
                        item = item,
                        isIncognito = isIncognito,
                        currentLightElement = currentLightElement,
                        currentAccent = currentAccent,
                        currentText = currentText,
                        currentSecText = currentSecText,
                        currentBorder = currentBorder,
                        onPause = { onPauseDownload(item.id) },
                        onResume = { onResumeDownload(item.id) },
                        onCancel = { onCancelDownload(item.id) },
                        onDelete = { onDeleteDownload(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadRowItem(
    item: DownloadItem,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val ext = item.fileName.substringAfterLast('.', "").lowercase()
    val (fileIcon, fileIconColor) = when {
        item.mimeType.startsWith("image/") || listOf("jpg", "jpeg", "png", "gif", "webp").contains(ext) -> {
            Icons.Default.Image to Color(0xFF10B981)
        }
        item.mimeType.startsWith("video/") || listOf("mp4", "mkv", "avi", "webm").contains(ext) -> {
            Icons.Default.Movie to Color(0xFF8B5CF6)
        }
        item.mimeType.startsWith("audio/") || listOf("mp3", "wav", "m4a", "ogg").contains(ext) -> {
            Icons.Default.MusicNote to Color(0xFFEC4899)
        }
        listOf("pdf", "doc", "docx", "txt", "xlsx", "ppt").contains(ext) -> {
            Icons.Default.Description to Color(0xFFEF4444)
        }
        listOf("zip", "rar", "tar", "gz").contains(ext) -> {
            Icons.Default.FolderZip to Color(0xFFF59E0B)
        }
        else -> {
            Icons.Default.InsertDriveFile to Color(0xFF6B7280)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(fileIconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = fileIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        color = currentText,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.url,
                        color = currentSecText,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.status) {
                        "DOWNLOADING" -> {
                            IconButton(onClick = onPause, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "إيقاف مؤقت",
                                    tint = currentAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إلغاء",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        "PAUSED" -> {
                            IconButton(onClick = onResume, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "استئناف",
                                    tint = SecureGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = currentSecText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        "CANCELED" -> {
                            IconButton(onClick = onResume, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "إعادة التنزيل",
                                    tint = currentAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = currentSecText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        "COMPLETED" -> {
                            IconButton(
                                onClick = {
                                    try {
                                        val dir = java.io.File(context.filesDir, "downloads")
                                        val file = java.io.File(dir, item.fileName)
                                        // Fallback: try to find file with similar name if not found (handles unique suffix)
                                        val targetFile = if (file.exists()) file else {
                                            dir.listFiles()?.find { it.name.startsWith(item.fileName.substringBeforeLast('.')) } ?: file
                                        }
                                        if (!targetFile.exists()) {
                                            android.widget.Toast.makeText(context, "الملف غير موجود", android.widget.Toast.LENGTH_SHORT).show()
                                            return@IconButton
                                        }
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                                        val mime = when {
                                            item.mimeType.isNotEmpty() && item.mimeType != "application/octet-stream" -> item.mimeType
                                            else -> {
                                                val ext = item.fileName.substringAfterLast('.', "").lowercase()
                                                when (ext) {
                                                    "pdf" -> "application/pdf"
                                                    "jpg", "jpeg" -> "image/jpeg"
                                                    "png" -> "image/png"
                                                    "mp4" -> "video/mp4"
                                                    else -> "application/octet-stream"
                                                }
                                            }
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, mime)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "فتح الملف"))
                                        android.widget.Toast.makeText(context, "تم فتح الملف بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر فتح الملف: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }, 
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "فتح الملف",
                                    tint = SecureGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = currentSecText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatBytes(item.downloadedBytes)} / ${formatBytes(item.totalBytes)}",
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.status == "DOWNLOADING") {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = when (item.status) {
                            "DOWNLOADING" -> item.speed
                            "PAUSED" -> "موقف مؤقتاً"
                            "CANCELED" -> "ملغى"
                            "COMPLETED" -> "مكتمل ✓"
                            else -> "متوقف"
                        },
                        color = when (item.status) {
                            "DOWNLOADING" -> currentAccent
                            "COMPLETED" -> SecureGreen
                            "CANCELED" -> Color.Red
                            else -> currentSecText
                        },
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (item.status == "DOWNLOADING" || item.progress > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (item.status == "PAUSED") currentSecText else currentAccent,
                    trackColor = currentLightElement
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).coerceAtLeast(0.0).toInt()
    if (digitGroups >= units.size) return "$bytes B"
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

