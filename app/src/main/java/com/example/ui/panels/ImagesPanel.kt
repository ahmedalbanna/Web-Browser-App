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
fun ImagesPanel(
    viewModel: WebBrowserViewModel,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Page Extracted, 1 = Downloaded
    val extractedImages by viewModel.extractedImages.collectAsState()
    val isExtracting by viewModel.isExtractingImages.collectAsState()
    val allDownloads by viewModel.allDownloads.collectAsState()

    var viewerImageUri by remember { mutableStateOf<String?>(null) }
    var viewerImageTitle by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val downloadedImages = remember(allDownloads) {
        allDownloads.filter { item ->
            item.status == "COMPLETED" && (
                item.mimeType.startsWith("image/") ||
                item.fileName.endsWith(".jpg", ignoreCase = true) ||
                item.fileName.endsWith(".jpeg", ignoreCase = true) ||
                item.fileName.endsWith(".png", ignoreCase = true) ||
                item.fileName.endsWith(".gif", ignoreCase = true) ||
                item.fileName.endsWith(".webp", ignoreCase = true) ||
                item.fileName.endsWith(".svg", ignoreCase = true)
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
                imageVector = Icons.Default.Collections,
                contentDescription = null,
                tint = currentAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "مستعرض ومعرض الصور",
                    color = currentText,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "عرض وتنزيل وحفظ جميع صور الصفحة والميديا المُنزلة",
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
                    .testTag("tab_extracted_images"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) currentAccent else Color.Transparent,
                    contentColor = if (activeTab == 0) (if (isIncognito) Color.Black else Color.White) else currentSecText
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("صور الصفحة الحالية", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            // Tab 2: Downloaded
            Button(
                onClick = { activeTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_downloaded_images"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) currentAccent else Color.Transparent,
                    contentColor = if (activeTab == 1) (if (isIncognito) Color.Black else Color.White) else currentSecText
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("الصور المُنزلّة (${downloadedImages.size})", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content Dynamic Switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (activeTab == 0) {
                // Category 0: Page Extracted Content
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تم العثور على (${extractedImages.size}) صورة بالصفحة- RLT",
                            color = currentText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Scan Button
                        Button(
                            onClick = { viewModel.triggerImageExtraction() },
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("scan_page_images_button"),
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
                                Text("استخراج الصور", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                                    text = "جاري مسح صفحة الويب واستخراج الصور والميديا...",
                                    color = currentSecText,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    } else if (extractedImages.isEmpty()) {
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
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = currentSecText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لا توجد صور مستخرجة حالياً",
                                    color = currentText,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "اضغط على زر الاستخراج لمسح علامة التبويب الحالية واسترجاع كافة الصور المعروضة بداخلها بضغطة واحدة وعرضها بوضوح.",
                                    color = currentSecText,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    } else {
                        // Extracted Grid using fully-qualified calls to avoid any imports issue
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(100.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(extractedImages) { imgUrl ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable {
                                            viewerImageUri = imgUrl
                                            viewerImageTitle = "صورة مستخرجة من صفحة الويب"
                                        },
                                    colors = CardDefaults.cardColors(containerColor = currentLightElement),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, currentBorder)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        coil.compose.AsyncImage(
                                            model = imgUrl,
                                            contentDescription = "صورة موقع",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Category 1: Downloaded Local Content
                if (downloadedImages.isEmpty()) {
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
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = currentSecText.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لم تقم بتنزيل أي صور بعد",
                                color = currentText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الصور التي تقوم بتنزيلها عبر المتصفح ستظهر تلقائياً هنا في معرض آمن لسهولة الوصول والمشاهدة والمشاركة في أي وقت.",
                                color = currentSecText,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    // Downloaded Grid
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(100.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(downloadedImages) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        viewerImageUri = item.url
                                        viewerImageTitle = item.fileName
                                    },
                                colors = CardDefaults.cardColors(containerColor = currentLightElement),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, currentBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    coil.compose.AsyncImage(
                                        model = item.url,
                                        contentDescription = item.fileName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    // Image type label or name
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = item.fileName,
                                            color = Color.White,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue (Immersive Full Screen Image Viewer)
    if (viewerImageUri != null) {
        ImmersiveImageViewerDialog(
            url = viewerImageUri!!,
            title = viewerImageTitle,
            onDismiss = { viewerImageUri = null },
            currentAccent = currentAccent,
            currentLightElement = currentLightElement,
            currentText = currentText,
            currentSecText = currentSecText
        )
    }
}

@Composable
fun ImmersiveImageViewerDialog(
    url: String,
    title: String?,
    onDismiss: () -> Unit,
    currentAccent: Color,
    currentLightElement: Color,
    currentText: Color,
    currentSecText: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current

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
            // Immersive Image loading
            coil.compose.AsyncImage(
                model = url,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            // Header layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
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

                // Options action row (Save / Download)
                IconButton(
                    onClick = {
                        try {
                            val uri = android.net.Uri.parse(url)
                            android.widget.Toast.makeText(context, "جاري بدء تنزيل الصورة...", android.widget.Toast.LENGTH_SHORT).show()
                            val request = android.app.DownloadManager.Request(uri).apply {
                                setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
                                setAllowedOverRoaming(false)
                                setTitle(title ?: "صورة_مستعرض")
                                setDescription("جاري تنزيل الصورة المحددة بنجاح")
                                setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, title ?: "image_${System.currentTimeMillis()}.jpg")
                            }
                            val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                            downloadManager.enqueue(request)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "فشل الحفظ المباشر: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .background(currentAccent, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "حفظ وتنزيل",
                        tint = Color.White
                    )
                }
            }

            // Bottom bar naming layout
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
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "رابط الصورة: $url",
                        color = Color.White.copy(alpha = 0.5f),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

