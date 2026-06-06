package com.example.ui

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
import com.example.data.Bookmark
import com.example.data.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// premium Frosted Glass Light Theme Color Token Palette
val GlassBg = Color(0xFFF3F4F9) // Light blue-gray canvas background
val FrostWhite = Color(0xD8FFFFFF) // 85% opacity White glass material
val MatteWhite = Color(0xF2FFFFFF) // More opaque white for overlays
val LightElement = Color(0xFFEEF0F6) // Internal pills/address bar background
val DarkText = Color(0xFF1B1B1F) // Deep carbon text
val SecondaryText = Color(0xFF74777F) // Medium gray text
val ShadowBorder = Color(0xFFDDE2F0) // Refined delicate border line
val BorderHighlight = Color(0xFFB0C4DE) // Stronger line for active markers
val AccentBlue = Color(0xFF005AC1) // Material Blue primary active indicator

// Tab specific accent system
val TabActiveBg = Color(0xFFD3E3FD) // Light blue color for the selected tab
val TabActiveText = Color(0xFF001D35) // Deep contrast active blue text
val TabActiveBorder = Color(0xFFA8C7FA)
val SecureGreen = Color(0xFF10B981) // Secure Green SSL line

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebBrowserScreen(
    viewModel: WebBrowserViewModel,
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.loadingProgress.collectAsState()
    val isSecure by viewModel.isSecure.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    val isBookmarked by viewModel.isCurrentBookmarked.collectAsState()

    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()

    var showHistoryAndBookmarksSheet by remember { mutableStateOf(false) }
    var showTabsListSheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var addressText by remember { mutableStateOf(urlInput) }
    LaunchedEffect(urlInput) {
        addressText = urlInput
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBg),
        topBar = {
            Column(
                modifier = Modifier.background(FrostWhite)
            ) {
                // Top Tab row switcher with high-fidelity frosted glass feel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.createNewTab() },
                        modifier = Modifier
                            .testTag("add_tab_button")
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, ShadowBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "علامة تبويب جديدة",
                            tint = AccentBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tabs) { tab ->
                            val isActive = tab.id == activeTabId
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(if (isActive) TabActiveBg else Color.White.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) TabActiveBorder else ShadowBorder,
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .clickable { viewModel.switchTab(tab.id) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (tab.url.startsWith("https://")) Icons.Default.Lock else Icons.Default.Public,
                                    contentDescription = null,
                                    tint = if (isActive) AccentBlue else SecondaryText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isActive) TabActiveText else DarkText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 105.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق التبويب",
                                    tint = if (isActive) TabActiveText.copy(alpha = 0.7f) else SecondaryText.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clickable { viewModel.closeTab(tab.id) }
                                )
                            }
                        }
                    }
                }

                // Elegant Glassy Address Bar Layer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = (0.5).dp, color = ShadowBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(LightElement, RoundedCornerShape(24.dp))
                                .border(1.dp, ShadowBorder.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isSecure) "اتصال آمن" else "غير آمن",
                                tint = if (isSecure) SecureGreen else Color.Red,
                                modifier = Modifier.size(18.dp)
                            )

                            // Unified browser URI line
                            TextField(
                                value = addressText,
                                onValueChange = { addressText = it },
                                placeholder = {
                                    Text(
                                        "ابحث أو اكتب عنوان موقع ويب...",
                                        color = SecondaryText,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Start
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("address_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = DarkText,
                                    unfocusedTextColor = DarkText
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        viewModel.handleSearchOrUrl(addressText)
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                maxLines = 1,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 13.5.sp,
                                    textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Bookmarks quick toggle
                        IconButton(
                            onClick = { viewModel.toggleBookmark() },
                            modifier = Modifier
                                .testTag("bookmark_toggle_button")
                                .size(44.dp)
                                .background(LightElement, CircleShape)
                                .border(1.dp, ShadowBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "حفظ في المفضلة",
                                tint = if (isBookmarked) AccentBlue else SecondaryText
                            )
                        }
                    }

                    // Seamless dynamic linear web loader line
                    AnimatedVisibility(
                        visible = isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = AccentBlue,
                            trackColor = LightElement,
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Elegant Navigation Toolbar supporting RTL naturally using weights - Light UI Glass
            Surface(
                color = FrostWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = ShadowBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier
                            .testTag("back_navigation")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع للخلف",
                            tint = if (canGoBack) DarkText else SecondaryText.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier
                            .testTag("forward_navigation")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "التقدم للأمام",
                            tint = if (canGoForward) DarkText else SecondaryText.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(
                        onClick = { if (isLoading) viewModel.stop() else viewModel.reload() },
                        modifier = Modifier
                            .testTag("refresh_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (isLoading) "إيقاف التحميل" else "إعادة تحميل الصفحة",
                            tint = DarkText
                        )
                    }

                    IconButton(
                        onClick = { showTabsListSheet = true },
                        modifier = Modifier
                            .testTag("tab_switcher_button")
                            .size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = "قائمة علامات التبويب",
                                tint = DarkText
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 5.dp, y = (-2).dp)
                                    .background(AccentBlue, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tabs.size.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showHistoryAndBookmarksSheet = true },
                        modifier = Modifier
                            .testTag("history_bookmarks_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = "السجل والمفضلة",
                            tint = DarkText
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GlassBg)
        ) {
            BrowserWebView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Modal Bottom sheet for Bookmarks and History showing high-fidelity light panel
    if (showHistoryAndBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistoryAndBookmarksSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MatteWhite,
            tonalElevation = 16.dp,
        ) {
            HistoryAndBookmarksPanel(
                bookmarks = bookmarks,
                history = history,
                onBookmarkSelected = { url ->
                    viewModel.handleSearchOrUrl(url)
                    showHistoryAndBookmarksSheet = false
                },
                onHistorySelected = { url ->
                    viewModel.handleSearchOrUrl(url)
                    showHistoryAndBookmarksSheet = false
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDeleteHistory = { viewModel.deleteHistoryEntry(it) },
                onClearAllHistory = { viewModel.clearHistory() }
            )
        }
    }

    // Modal Bottom sheet for Switching and managing tabs
    if (showTabsListSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTabsListSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MatteWhite,
            tonalElevation = 16.dp,
        ) {
            TabsManagerPanel(
                tabs = tabs,
                activeTabId = activeTabId,
                onTabSelected = { tabId ->
                    viewModel.switchTab(tabId)
                    showTabsListSheet = false
                },
                onCloseTab = { viewModel.closeTab(it) },
                onAddNewTab = {
                    viewModel.createNewTab()
                    showTabsListSheet = false
                }
            )
        }
    }
}

// Subcomponent Layouts for History And Bookmarks light theme
@Composable
fun HistoryAndBookmarksPanel(
    bookmarks: List<Bookmark>,
    history: List<HistoryEntry>,
    onBookmarkSelected: (String) -> Unit,
    onHistorySelected: (String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onDeleteHistory: (HistoryEntry) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 -> Bookmarks, 1 -> History

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .padding(16.dp)
    ) {
        // Tab header switch styled with gorgeous light gray capsule pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightElement, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedTabState = 0 },
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabState == 0) Color.White else Color.Transparent,
                    contentColor = if (selectedTabState == 0) AccentBlue else DarkText
                ),
                elevation = if (selectedTabState == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("المفضلة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = { selectedTabState = 1 },
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabState == 1) Color.White else Color.Transparent,
                    contentColor = if (selectedTabState == 1) AccentBlue else DarkText
                ),
                elevation = if (selectedTabState == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("السجل", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabState == 0) {
            if (bookmarks.isEmpty()) {
                EmptyStateLayout(
                    imageVector = Icons.Default.BookmarkBorder,
                    title = "مفضلتك فارغة حالياً",
                    subtitle = "احفظ مواقع الويب اليومية بالنقر على النجمة في شريط العنوان للاقتناء بها والعودة إليها بسرعة!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bookmarks) { bookmark ->
                        BookmarkRow(
                            bookmark = bookmark,
                            onSelect = { onBookmarkSelected(bookmark.url) },
                            onDelete = { onDeleteBookmark(bookmark) }
                        )
                    }
                }
            }
        } else {
            if (history.isEmpty()) {
                EmptyStateLayout(
                    imageVector = Icons.Default.HistoryToggleOff,
                    title = "سجل تصفحك خالٍ",
                    subtitle = "مواقع الويب والمقالات التي تزورها ستظهر منظمة ومجدولة زمنياً هنا تلقائياً لراحتك!"
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
                            Text("مسح كامل السجل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history) { entry ->
                            HistoryRow(
                                entry = entry,
                                onSelect = { onHistorySelected(entry.url) },
                                onDelete = { onDeleteHistory(entry) }
                            )
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
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, ShadowBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LightElement, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bookmark.url,
                    color = SecondaryText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
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
            .border(1.dp, ShadowBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LightElement, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.url,
                    color = SecondaryText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    color = AccentBlue,
                    fontSize = 9.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, tint = SecondaryText.copy(alpha = 0.6f), contentDescription = "حذف")
            }
        }
    }
}

@Composable
fun TabsManagerPanel(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onTabSelected: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onAddNewTab: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "علامات التبويب المفتوحة (${tabs.size})",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = onAddNewTab,
                colors = ButtonDefaults.buttonColors(containerColor = LightElement, contentColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تبويب جديد", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTabSelected(tab.id) }
                        .border(
                            width = 1.dp,
                            color = if (isActive) AccentBlue else ShadowBorder,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) TabActiveBg else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Web,
                            contentDescription = null,
                            tint = if (isActive) AccentBlue else SecondaryText,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title,
                                color = DarkText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tab.url,
                                color = SecondaryText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = LocalTextStyle.current.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
                            )
                        }

                        IconButton(onClick = { onCloseTab(tab.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = if (isActive) TabActiveText else DarkText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateLayout(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = SecondaryText.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = DarkText,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = SecondaryText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
