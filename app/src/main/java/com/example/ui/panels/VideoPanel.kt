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

import com.example.ui.*
import com.example.data.*

@Composable
fun VideoPanel(
    viewModel: WebBrowserViewModel,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Page Extracted, 1 = Downloaded, 2 = Direct URL
    val extractedVideos by viewModel.extractedVideos.collectAsState()
    val isExtracting by viewModel.isExtractingVideos.collectAsState()
    val allDownloads by viewModel.allDownloads.collectAsState()

    var directUrlInput by remember { mutableStateOf("") }
    var activePlaybackUrl by remember { mutableStateOf<String?>(null) }
    var activePlaybackTitle by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val downloadedVideos = remember(allDownloads) {
        allDownloads.filter { item ->
            item.status == "COMPLETED" && (
                item.mimeType.startsWith("video/") ||
                item.fileName.endsWith(".mp4", ignoreCase = true) ||
                item.fileName.endsWith(".mkv", ignoreCase = true) ||
                item.fileName.endsWith(".webm", ignoreCase = true) ||
                item.fileName.endsWith(".avi", ignoreCase = true) ||
                item.fileName.endsWith(".3gp", ignoreCase = true)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Handle bar in bottom sheet
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(currentSecText.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = currentAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "مستخرج ومشغل الفيديو الذكي",
                    color = currentText,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اكتشف الفيديوهات من صفحات الويب وشغلها مع دعم البث والتحميل الذكي",
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom High-Fidelity Tab Row Selection (Arabic Style Segmented Buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentLightElement, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Page Extracted
            Button(
                onClick = { activeTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_extracted_videos"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) currentAccent else Color.Transparent,
                    contentColor = if (activeTab == 0) (if (isIncognito) Color.Black else Color.White) else currentSecText
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FeaturedVideo,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("المكتشفة بالصفحة", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            // Tab 2: Downloaded
            Button(
                onClick = { activeTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_downloaded_videos"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) currentAccent else Color.Transparent,
                    contentColor = if (activeTab == 1) (if (isIncognito) Color.Black else Color.White) else currentSecText
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("المحملة المكتملة (${downloadedVideos.size})", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            // Tab 3: Custom Direct Link to play/stream
            Button(
                onClick = { activeTab = 2 },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_custom_stream"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 2) currentAccent else Color.Transparent,
                    contentColor = if (activeTab == 2) (if (isIncognito) Color.Black else Color.White) else currentSecText
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("رابط البث", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content Dynamic Switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeTab) {
                0 -> {
                    // Category 0: Page Extracted Videos Content
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مكتشف الفيديو تلقائياً (${extractedVideos.size})",
                                color = currentText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Scan Button
                            Button(
                                onClick = { viewModel.triggerVideoExtraction() },
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("scan_page_videos_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                                shape = RoundedCornerShape(17.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                if (isExtracting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = if (isIncognito) Color.Black else Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("كشف الفيديوهات", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isExtracting) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = currentAccent)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "جاري مسح صفحة الويب الحالية عن ميديا ومشغلات فيديو مدمجة...",
                                        color = currentSecText,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        } else if (extractedVideos.isEmpty()) {
                            // Empty Extracted Layout
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FeaturedVideo,
                                        contentDescription = null,
                                        tint = currentSecText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "لا توجد فيديوهات مكتشفة حالياً",
                                        color = currentText,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "افتح أي موقع يحتوي على وسائط أو فيديوهات (مثل منصات الأخبار أو الترفيه)، ثم اضغط على زر الكشف لاستخراجها وتشغيلها بلمسة واحدة.",
                                        color = currentSecText,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(extractedVideos) { video ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                activePlaybackUrl = video.url
                                                activePlaybackTitle = video.title
                                            },
                                        colors = CardDefaults.cardColors(containerColor = currentLightElement),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, currentBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(currentAccent.copy(alpha = 0.12f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = currentAccent,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = video.title,
                                                    color = currentText,
                                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = video.url,
                                                    color = currentSecText,
                                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val uri = android.net.Uri.parse(video.url)
                                                        val request = android.app.DownloadManager.Request(uri).apply {
                                                            setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
                                                            setTitle(video.title)
                                                            setDescription("تنزيل ميديا الويب")
                                                            setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "video_${System.currentTimeMillis()}.mp4")
                                                            setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                        }
                                                        val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                                                        downloadManager.enqueue(request)
                                                        android.widget.Toast.makeText(context, "بدأ تنزيل الفيديو...", android.widget.Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        android.widget.Toast.makeText(context, "فشل بدء التنزيل: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "تحميل",
                                                    tint = currentSecText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Category 1: Downloaded Media Content
                    if (downloadedVideos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = currentSecText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لم تقم بتنزيل أي فيديوهات بعد",
                                    color = currentText,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الفيديوهات وملفات الوسائط التي تحفظها من المواقع ستظهر تلقائياً هنا لتشغيلها بشكل فوري وبكامل دقتها بدون إنترنت.",
                                    color = currentSecText,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(downloadedVideos) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activePlaybackUrl = item.url
                                            activePlaybackTitle = item.fileName
                                        },
                                    colors = CardDefaults.cardColors(containerColor = currentLightElement),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, currentBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(currentAccent.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = currentAccent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.fileName,
                                                color = currentText,
                                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "الحجم: ${(item.totalBytes / (1024 * 1024))} ميجا بايت",
                                                color = currentSecText,
                                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                try {
                                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(android.content.Intent.EXTRA_TEXT, item.url)
                                                    }
                                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "مشاركة رابط الفيديو")
                                                    context.startActivity(shareIntent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "مشاركة",
                                                tint = currentSecText,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Category 2: Play custom Stream Link
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "تشغيل بث أو رابط فيديو مخصص مباشر",
                            color = currentText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "يدعم مشغل الفيديو الخاص بنا مجموعة واسعة من صيغ البث المباشر ومقاطع الفيديو من أي مخدم خارجي.",
                            color = currentSecText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = directUrlInput,
                            onValueChange = { directUrlInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("direct_video_url_input"),
                            label = { Text("رابط الفيديو للتشغيل", color = currentSecText) },
                            placeholder = { Text("https://example.com/stream.mp4", color = currentSecText.copy(alpha = 0.6f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = currentText,
                                unfocusedTextColor = currentSecText,
                                focusedBorderColor = currentAccent,
                                unfocusedBorderColor = currentBorder
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = currentSecText
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (directUrlInput.trim().isNotEmpty()) {
                                    activePlaybackUrl = directUrlInput.trim()
                                    activePlaybackTitle = "بث ميديا مخصص من المستخدم"
                                } else {
                                    android.widget.Toast.makeText(context, "الرجاء إدخال رابط تشغيل ميديا صحيح", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("play_direct_video_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isIncognito) Color.Black else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تشغيل الميديا فوراً", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (activePlaybackUrl != null) {
        ImmersiveVideoPlayerDialog(
            url = activePlaybackUrl!!,
            title = activePlaybackTitle,
            onDismiss = { activePlaybackUrl = null },
            currentAccent = currentAccent,
            currentLightElement = currentLightElement,
            currentText = currentText,
            currentSecText = currentSecText
        )
    }
}

@Composable
fun ImmersiveVideoPlayerDialog(
    url: String,
    title: String?,
    onDismiss: () -> Unit,
    currentAccent: Color,
    currentLightElement: Color,
    currentText: Color,
    currentSecText: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isEmbed = url.contains("youtube.com") || url.contains("youtu.be") || url.contains("vimeo.com")
    var isBackgroundAudioEnabled by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            // Interactive Video Player Holder
            if (isEmbed) {
                // Interactive Embedded Web Frame for YouTube/Vimeo embeds
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                domStorageEnabled = true
                            }
                            webViewClient = android.webkit.WebViewClient()
                            setBackgroundColor(0)
                        }
                    },
                    update = { webView ->
                        webView.loadUrl(url)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16/9f)
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            } else {
                // Standard Native ExoPlayer (Jetpack Media3)
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(url))
                        prepare()
                        playWhenReady = true
                        // Keep playing when app bg if enabled (handled via lifecycle)
                    }
                }
                // Background audio toggle — keep player playing when PiP/background
                DisposableEffect(isBackgroundAudioEnabled) {
                    if (isBackgroundAudioEnabled) {
                        exoPlayer.playWhenReady = true
                    }
                    onDispose {}
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16/9f)
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            // Top Control Overlay Header bar — Milestone 4 PiP + background audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White
                    )
                }

                // PiP + Background audio controls (center)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // PiP button
                    IconButton(
                        onClick = {
                            try {
                                val activity = context as? android.app.Activity
                                if (activity != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    val params = android.app.PictureInPictureParams.Builder()
                                        .setAspectRatio(android.util.Rational(16, 9))
                                        .build()
                                    activity.enterPictureInPictureMode(params)
                                } else {
                                    android.widget.Toast.makeText(context, "وضع الصورة داخل الصورة غير مدعوم", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "فشل PiP: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "صورة داخل صورة",
                            tint = Color.White
                        )
                    }
                    // Background audio toggle
                    IconButton(
                        onClick = { isBackgroundAudioEnabled = !isBackgroundAudioEnabled },
                        modifier = Modifier
                            .background(
                                if (isBackgroundAudioEnabled) currentAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f),
                                CircleShape
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isBackgroundAudioEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "صوت الخلفية",
                            tint = if (isBackgroundAudioEnabled) currentAccent else Color.White
                        )
                    }
                }

                if (!isEmbed) {
                    IconButton(
                        onClick = {
                            try {
                                val uri = android.net.Uri.parse(url)
                                val request = android.app.DownloadManager.Request(uri).apply {
                                    setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
                                    setTitle(title ?: "تنزيل الميديا")
                                    setDescription("تنزيل ملف الفيديو المكتشف")
                                    setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, title ?: "video_${System.currentTimeMillis()}.mp4")
                                    setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                }
                                val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                                downloadManager.enqueue(request)
                                android.widget.Toast.makeText(context, "جاري بدء تنزيل الفيديو المباشر...", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "فشل تنزيل ملف الفيديو: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(currentAccent, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "تنزيل",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom title metadata layout
            if (title != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEmbed) "إطار ميديا مدمج" else "بث مشغل مشفر مباشر",
                        color = currentAccent,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

