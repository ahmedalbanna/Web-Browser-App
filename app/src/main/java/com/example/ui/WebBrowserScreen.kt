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

// --- Color Tokens for Light Frosted Glass Theme ---
val GlassBg = Color(0xFFF3F4F9) // Light blue-gray canvas background
val FrostWhite = Color(0xD8FFFFFF) // 85% opacity White glass material
val MatteWhite = Color(0xF2FFFFFF) // More opaque white for overlays
val LightElement = Color(0xFFEEF0F6) // Internal pills/address bar background
val DarkText = Color(0xFF1B1B1F) // Deep carbon text
val SecondaryText = Color(0xFF74777F) // Medium gray text
val ShadowBorder = Color(0xFFDDE2F0) // Refined delicate border line
val BorderHighlight = Color(0xFFB0C4DE) // Stronger line for active markers
val AccentBlue = Color(0xFF005AC1) // Material Blue primary active indicator

val TabActiveBg = Color(0xFFD3E3FD) // Light blue color for the selected tab
val TabActiveText = Color(0xFF001D35) // Deep contrast active blue text
val TabActiveBorder = Color(0xFFA8C7FA)
val SecureGreen = Color(0xFF10B981) // Secure Green SSL line

// --- Color Tokens for Incognito Stealth Glass Theme (Private) ---
val IncognitoBg = Color(0xFF0D0E12) // Mysterious deep slate black background
val IncognitoFrost = Color(0xD81A1C24) // 85% opacity dark obsidian glass
val IncognitoMatte = Color(0xF214161F) // Deep rich dark sheet background
val IncognitoLightElement = Color(0xFF232631) // Deep selection capsule or input field
val IncognitoText = Color(0xFFE3E4E9) // Cool white accent text
val IncognitoSecText = Color(0xFF8A8F9E) // Soft muted slate description text
val IncognitoBorder = Color(0xFF282D3D) // Hairline borders for stealth looks
val IncognitoAccent = Color(0xFFFF9E2C) // Glowing active amber spy color

val IncognitoTabActiveBg = Color(0xFF323646) // Stealth selected tab pill
val IncognitoTabActiveText = Color(0xFFFFFFFF) // Contrasting white tab label
val IncognitoTabActiveBorder = Color(0xFF42485E)

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
    val isIncognito by viewModel.isIncognito.collectAsState()

    // VPN connection state & configuration
    val isVpnConnected by viewModel.isVpnConnected.collectAsState()
    val selectedVpnServer by viewModel.selectedVpnServer.collectAsState()
    val simulatedPing by viewModel.simulatedPing.collectAsState()
    val simulatedDataUsage by viewModel.simulatedDataUsage.collectAsState()

    var showHistoryAndBookmarksSheet by remember { mutableStateOf(false) }
    var showTabsListSheet by remember { mutableStateOf(false) }
    var showVpnSheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var addressText by remember { mutableStateOf(urlInput) }
    LaunchedEffect(urlInput) {
        addressText = urlInput
    }

    // Dynamic Style selection based on Incognito state
    val currentBg = if (isIncognito) IncognitoBg else GlassBg
    val currentFrost = if (isIncognito) IncognitoFrost else FrostWhite
    val currentMatte = if (isIncognito) IncognitoMatte else MatteWhite
    val currentLightElement = if (isIncognito) IncognitoLightElement else LightElement
    val currentText = if (isIncognito) IncognitoText else DarkText
    val currentSecText = if (isIncognito) IncognitoSecText else SecondaryText
    val currentBorder = if (isIncognito) IncognitoBorder else ShadowBorder
    val currentAccent = if (isIncognito) IncognitoAccent else AccentBlue

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(currentBg),
        topBar = {
            Column(
                modifier = Modifier.background(currentFrost)
            ) {
                // Top Tab row switcher with high-fidelity frosted glass feel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // New Tab Action Button
                    IconButton(
                        onClick = { viewModel.createNewTab() },
                        modifier = Modifier
                            .testTag("add_tab_button")
                            .size(38.dp)
                            .background(if (isIncognito) currentLightElement else Color.White, CircleShape)
                            .border(1.dp, currentBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "علامة تبويب جديدة",
                            tint = currentAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Premium Stealth Incognito Switcher
                    IconButton(
                        onClick = { viewModel.toggleIncognito() },
                        modifier = Modifier
                            .testTag("incognito_toggle_button")
                            .size(38.dp)
                            .background(
                                color = if (isIncognito) currentAccent.copy(alpha = 0.2f) else currentLightElement,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isIncognito) currentAccent else currentBorder,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "تبديل الوضع المتخفي",
                            tint = if (isIncognito) currentAccent else currentSecText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tabs) { tab ->
                            val isActive = tab.id == activeTabId
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        if (isActive) {
                                            if (isIncognito) IncognitoTabActiveBg else TabActiveBg
                                        } else {
                                            if (isIncognito) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) {
                                            if (isIncognito) IncognitoTabActiveBorder else TabActiveBorder
                                        } else {
                                            currentBorder
                                        },
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .clickable { viewModel.switchTab(tab.id) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (tab.url.startsWith("https://")) Icons.Default.Lock else Icons.Default.Public,
                                    contentDescription = null,
                                    tint = if (isActive) currentAccent else currentSecText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isActive) {
                                        if (isIncognito) IncognitoTabActiveText else TabActiveText
                                    } else {
                                        currentText
                                    },
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
                                    tint = if (isActive) {
                                        if (isIncognito) IncognitoTabActiveText.copy(alpha = 0.7f) else TabActiveText.copy(alpha = 0.7f)
                                    } else {
                                        currentSecText.copy(alpha = 0.6f)
                                    },
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
                        .border(width = (0.5).dp, color = currentBorder)
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
                                .background(currentLightElement, RoundedCornerShape(24.dp))
                                .border(1.dp, currentBorder.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
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
                                        color = currentSecText,
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
                                    focusedTextColor = currentText,
                                    unfocusedTextColor = currentText
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

                        // VPN configuration button
                        IconButton(
                            onClick = { showVpnSheet = true },
                            modifier = Modifier
                                .testTag("vpn_toggle_button")
                                .size(44.dp)
                                .background(
                                    color = if (isVpnConnected) {
                                        if (isIncognito) IncognitoAccent.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.1f)
                                    } else currentLightElement,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isVpnConnected) currentAccent else currentBorder,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isVpnConnected) Icons.Default.Security else Icons.Outlined.Security,
                                contentDescription = "تكوين شبكة VPN الافتراضية",
                                tint = if (isVpnConnected) currentAccent else currentSecText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Bookmarks quick toggle
                        IconButton(
                            onClick = { viewModel.toggleBookmark() },
                            modifier = Modifier
                                .testTag("bookmark_toggle_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "حفظ في المفضلة",
                                tint = if (isBookmarked) currentAccent else currentSecText
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
                            color = currentAccent,
                            trackColor = currentLightElement,
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Elegant Navigation Toolbar supporting RTL naturally using weights - Light UI Glass
            Surface(
                color = currentFrost,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = currentBorder)
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
                            tint = if (canGoBack) currentText else currentSecText.copy(alpha = 0.4f)
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
                            tint = if (canGoForward) currentText else currentSecText.copy(alpha = 0.4f)
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
                            tint = currentText
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
                                tint = currentText
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 5.dp, y = (-2).dp)
                                    .background(currentAccent, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tabs.size.toString(),
                                    color = if (isIncognito) Color.Black else Color.White,
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
                            tint = currentText
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
                .background(currentBg)
        ) {
            BrowserWebView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Floating Stealth Incognito Active Badge
            if (isIncognito) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(currentAccent, RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "وضع متخفٍ نشط",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Dynamic Floating VPN Active Badge
            if (isVpnConnected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clickable { showVpnSheet = true }
                ) {
                    Row(
                        modifier = Modifier
                            .background(if (isIncognito) IncognitoTabActiveBg else TabActiveBg, RoundedCornerShape(20.dp))
                            .border(1.dp, if (isIncognito) IncognitoTabActiveBorder else TabActiveBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تشفير نشط: ${selectedVpnServer.flag} ${selectedVpnServer.name.substringBefore(" ")}",
                            color = if (isIncognito) Color.White else TabActiveText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom sheet for Bookmarks and History showing high-fidelity light/dark panel
    if (showHistoryAndBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistoryAndBookmarksSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            HistoryAndBookmarksPanel(
                bookmarks = bookmarks,
                history = history,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
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
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            TabsManagerPanel(
                tabs = tabs,
                activeTabId = activeTabId,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
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

    // Modal Bottom sheet for VPN Shield configuration
    if (showVpnSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVpnSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            VpnSettingsPanel(
                isConnected = isVpnConnected,
                selectedServer = selectedVpnServer,
                servers = vpnServersList,
                ping = simulatedPing,
                dataUsage = simulatedDataUsage,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onToggleVpn = { viewModel.toggleVpn() },
                onSelectServer = { viewModel.setVpnServer(it) }
            )
        }
    }
}

// Subcomponent Layouts for History And Bookmarks light/dark theme
@Composable
fun HistoryAndBookmarksPanel(
    bookmarks: List<Bookmark>,
    history: List<HistoryEntry>,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onBookmarkSelected: (String) -> Unit,
    onHistorySelected: (String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onDeleteHistory: (HistoryEntry) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 -> Bookmarks, 1 -> History
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab header switch styled with gorgeous capsule pill buttons
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
                    containerColor = if (selectedTabState == 1) {
                        if (isIncognito) IncognitoTabActiveBg else Color.White
                    } else Color.Transparent,
                    contentColor = if (selectedTabState == 1) currentAccent else currentText
                ),
                elevation = if (selectedTabState == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("السجل", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        text = if (selectedTabState == 0) "ابحث في مفضلتك..." else "ابحث في السجل اليومي...",
                        color = currentSecText,
                        fontSize = 12.sp
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

        if (selectedTabState == 0) {
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
        } else {
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
                            Text("مسح كامل السجل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bookmark.url,
                    color = currentSecText,
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
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.url,
                    color = currentSecText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    color = currentAccent,
                    fontSize = 9.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, tint = currentSecText.copy(alpha = 0.6f), contentDescription = "حذف")
            }
        }
    }
}

@Composable
fun TabsManagerPanel(
    tabs: List<BrowserTab>,
    activeTabId: String,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
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
                color = currentText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = onAddNewTab,
                colors = ButtonDefaults.buttonColors(containerColor = currentLightElement, contentColor = currentAccent),
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
                            color = if (isActive) currentAccent else currentBorder,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            if (isIncognito) IncognitoTabActiveBg else TabActiveBg
                        } else {
                            if (isIncognito) currentLightElement.copy(alpha = 0.5f) else Color.White
                        }
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
                            tint = if (isActive) currentAccent else currentSecText,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title,
                                color = currentText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tab.url,
                                color = currentSecText,
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
                                tint = if (isActive) {
                                    if (isIncognito) IncognitoTabActiveText else TabActiveText
                                } else {
                                    currentText
                                }
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
    subtitle: String,
    currentText: Color,
    currentSecText: Color
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
            tint = currentSecText.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = currentText,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = currentSecText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun VpnSettingsPanel(
    isConnected: Boolean,
    selectedServer: com.example.ui.VpnServer,
    servers: List<com.example.ui.VpnServer>,
    ping: Int,
    dataUsage: Float,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onToggleVpn: () -> Unit,
    onSelectServer: (com.example.ui.VpnServer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Upper Title & Active Tunnel Shield Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (isConnected) currentAccent else currentSecText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "جسر التشفير الآمن (VPN)",
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            // Connection Status Glow Pill
            Box(
                modifier = Modifier
                    .background(
                        color = if (isConnected) currentAccent.copy(alpha = 0.15f) else currentLightElement,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isConnected) currentAccent else currentBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isConnected) "نشط ومؤمن" else "غير متصل",
                    color = if (isConnected) currentAccent else currentSecText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Premium Frosted Connection Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, currentBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) currentAccent.copy(alpha = 0.05f) else currentLightElement.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Clickable Power Switch
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = if (isConnected) currentAccent else currentLightElement,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isConnected) Color.White.copy(alpha = 0.6f) else currentBorder,
                            shape = CircleShape
                        )
                        .clickable { onToggleVpn() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "تفعيل / إلغاء تفعيل",
                        tint = if (isConnected) (if (isIncognito) Color.Black else Color.White) else currentSecText,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isConnected) "انقر للقطع ووقف التشفير" else "انقر للتشغيل وبدء التصفح المشفر",
                    color = currentSecText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry Statistics Grid
        Text(
            text = "إحصائيات الاتصال الفعلي",
            color = currentText,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat Card 1: Virtual IP
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("عنوان الـ IP", color = currentSecText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) selectedServer.ipAddress else "---.---.---.---",
                        color = currentText,
                        fontSize = 12.6.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
                    )
                }
            }

            // Stat Card 2: Ping / Latency
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("سرعة الاستجابة", color = currentSecText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) "$ping ms" else "--- ms",
                        color = if (isConnected) {
                            if (ping < 35) SecureGreen else if (ping < 90) currentAccent else Color.Yellow
                        } else currentSecText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stat Card 3: Tunneled Data
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, currentBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("بيانات مشفرة", color = currentSecText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) String.format(Locale.US, "%.3f MB", dataUsage) else "0.000 MB",
                        color = currentText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // High Speed Node selection list header
        Text(
            text = "اختر خادم التشفير الجغرافي",
            color = currentText,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers) { server ->
                val isSelected = server.name == selectedServer.name
                val borderHighlight = if (isSelected) currentAccent else currentBorder
                val backgroundShade = if (isSelected) currentAccent.copy(alpha = 0.08f) else currentLightElement.copy(alpha = 0.3f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundShade)
                        .border(1.dp, borderHighlight, RoundedCornerShape(12.dp))
                        .clickable { onSelectServer(server) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.flag,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = server.name,
                            color = currentText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "منفذ: ${server.ipAddress}:${server.port}",
                            color = currentSecText,
                            fontSize = 11.sp,
                            style = androidx.compose.ui.text.TextStyle(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = currentSecText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = server.speed,
                            color = currentAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
