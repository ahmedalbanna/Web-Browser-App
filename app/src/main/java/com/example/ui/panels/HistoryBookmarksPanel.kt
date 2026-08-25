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
fun HistoryAndBookmarksPanel(
    bookmarks: List<Bookmark>,
    history: List<HistoryEntry>,
    savedPages: List<com.example.data.SavedPage>,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onBookmarkSelected: (String) -> Unit,
    onHistorySelected: (String) -> Unit,
    onSavedPageSelected: (String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onDeleteHistory: (HistoryEntry) -> Unit,
    onDeleteSavedPage: (com.example.data.SavedPage) -> Unit,
    onClearAllHistory: () -> Unit,
    onClearAllSavedPages: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 -> Bookmarks, 1 -> History, 2 -> Saved Pages
    var searchQuery by remember { mutableStateOf("") }

    // Live Instant filtering based on local query
    val filteredBookmarks = remember(bookmarks, searchQuery) {
        if (searchQuery.isBlank()) bookmarks else {
            bookmarks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredHistory = remember(history, searchQuery) {
        if (searchQuery.isBlank()) history else {
            history.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredSavedPages = remember(savedPages, searchQuery) {
        if (searchQuery.isBlank()) savedPages else {
            savedPages.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab header switch styled with gorgeous capsule pill buttons (3 sections!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentLightElement, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedTabState = 0 },
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabState == 0) {
                        if (isIncognito) IncognitoTabActiveBg else Color.White
                    } else Color.Transparent,
                    contentColor = if (selectedTabState == 0) currentAccent else currentText
                ),
                elevation = if (selectedTabState == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("المفضلة", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { selectedTabState = 1 },
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabState == 1) {
                        if (isIncognito) IncognitoTabActiveBg else Color.White
                    } else Color.Transparent,
                    contentColor = if (selectedTabState == 1) currentAccent else currentText
                ),
                elevation = if (selectedTabState == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("السجل", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { selectedTabState = 2 },
                modifier = Modifier
                    .weight(1.1f) // Slightly more space for text
                    .padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabState == 2) {
                        if (isIncognito) IncognitoTabActiveBg else Color.White
                    } else Color.Transparent,
                    contentColor = if (selectedTabState == 2) currentAccent else currentText
                ),
                elevation = if (selectedTabState == 2) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("المحفوظة أوفلاين", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // High-Fidelity Local Filter Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(currentLightElement, RoundedCornerShape(22.dp))
                .border(1.dp, currentBorder, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = currentSecText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = when (selectedTabState) {
                            0 -> "ابحث في مفضلتك..."
                            1 -> "ابحث في السجل اليومي..."
                            else -> "ابحث في الصفحات المحفوظة..."
                        },
                        color = currentSecText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = currentText,
                    unfocusedTextColor = currentText
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "مسح",
                        tint = currentSecText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabState) {
            0 -> {
                if (filteredBookmarks.isEmpty()) {
                    EmptyStateLayout(
                        imageVector = Icons.Default.BookmarkBorder,
                        title = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة" else "مفضلتك فارغة حالياً",
                        subtitle = if (searchQuery.isNotEmpty()) "تحقق من الكلمات المدخلة وحاول مجدداً للبحث في المفضلة." else "احفظ مواقع الويب اليومية بالنقر على النجمة في شريط العنوان للاقتناء بها والعودة إليها بسرعة!",
                        currentText = currentText,
                        currentSecText = currentSecText
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBookmarks) { bookmark ->
                            BookmarkRow(
                                bookmark = bookmark,
                                currentLightElement = currentLightElement,
                                currentAccent = currentAccent,
                                currentText = currentText,
                                currentSecText = currentSecText,
                                currentBorder = currentBorder,
                                onSelect = { onBookmarkSelected(bookmark.url) },
                                onDelete = { onDeleteBookmark(bookmark) }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (filteredHistory.isEmpty()) {
                    EmptyStateLayout(
                        imageVector = Icons.Default.HistoryToggleOff,
                        title = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة" else "سجل تصفحك خالٍ",
                        subtitle = if (searchQuery.isNotEmpty()) "تحقق من صحة المدخلات أو جرب البحث بكلمات أبسط للوصول للسجل." else "مواقع الويب والمقالات التي تزورها ستظهر منظمة ومجدولة زمنياً هنا تلقائياً لراحتك!",
                        currentText = currentText,
                        currentSecText = currentSecText
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onClearAllHistory,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مسح كامل السجل", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredHistory) { entry ->
                                HistoryRow(
                                    entry = entry,
                                    currentLightElement = currentLightElement,
                                    currentAccent = currentAccent,
                                    currentText = currentText,
                                    currentSecText = currentSecText,
                                    currentBorder = currentBorder,
                                    onSelect = { onHistorySelected(entry.url) },
                                    onDelete = { onDeleteHistory(entry) }
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                if (filteredSavedPages.isEmpty()) {
                    EmptyStateLayout(
                        imageVector = Icons.Default.Archive,
                        title = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة" else "لا توجد صفحات محفوظة",
                        subtitle = if (searchQuery.isNotEmpty()) "تحقق من صحة الكلمات المفتاحية في الصفحات المحفوظة." else "اضغط على زر الأرشفة الدائري في شريط الأدوات لحفظ صفحات الويب بالكامل وقراءتها في أي وقت بدون اتصال بالإنترنت!",
                        currentText = currentText,
                        currentSecText = currentSecText
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onClearAllSavedPages,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حذف كافة الصفحات", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredSavedPages) { page ->
                                SavedPageRow(
                                    page = page,
                                    currentLightElement = currentLightElement,
                                    currentAccent = currentAccent,
                                    currentText = currentText,
                                    currentSecText = currentSecText,
                                    currentBorder = currentBorder,
                                    onSelect = { onSavedPageSelected(page.filePath) },
                                    onDelete = { onDeleteSavedPage(page) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkRow(
    bookmark: Bookmark,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (currentLightElement == IncognitoLightElement) currentLightElement.copy(alpha = 0.5f) else Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(currentLightElement, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = currentAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bookmark.url,
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, tint = Color.Red.copy(alpha = 0.8f), contentDescription = "حذف")
            }
        }
    }
}

@Composable
fun HistoryRow(
    entry: HistoryEntry,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (currentLightElement == IncognitoLightElement) currentLightElement.copy(alpha = 0.5f) else Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(currentLightElement, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = currentSecText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.url,
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    color = currentAccent,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, tint = currentSecText.copy(alpha = 0.6f), contentDescription = "حذف")
            }
        }
    }
}

@Composable
fun SavedPageRow(
    page: com.example.data.SavedPage,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(page.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(page.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (currentLightElement == IncognitoLightElement) currentLightElement.copy(alpha = 0.5f) else Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(currentLightElement, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = currentAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = page.title,
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = page.url,
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "محفوظة دون اتصال",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateString,
                        color = currentSecText.copy(alpha = 0.6f),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, tint = Color.Red.copy(alpha = 0.8f), contentDescription = "حذف")
            }
        }
    }
}

