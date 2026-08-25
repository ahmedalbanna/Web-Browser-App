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

import com.example.ui.panels.*

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
    val downloads by viewModel.allDownloads.collectAsState()
    val savedPages by viewModel.allSavedPages.collectAsState()
    val isIncognito by viewModel.isIncognito.collectAsState()

    // VPN connection state & configuration
    val isVpnConnected by viewModel.isVpnConnected.collectAsState()
    val selectedVpnServer by viewModel.selectedVpnServer.collectAsState()
    val simulatedPing by viewModel.simulatedPing.collectAsState()
    val simulatedDataUsage by viewModel.simulatedDataUsage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val blockedAdsCount by viewModel.blockedAdsCount.collectAsState()
    val errorMessage by viewModel.errorMessages.collectAsState()

    var showHistoryAndBookmarksSheet by remember { mutableStateOf(false) }
    var showTabsListSheet by remember { mutableStateOf(false) }
    var showVpnSheet by remember { mutableStateOf(false) }
    var showAdBlockerSheet by remember { mutableStateOf(false) }
    var showDownloadsSheet by remember { mutableStateOf(false) }
    var showTranslationSheet by remember { mutableStateOf(false) }
    var showImagesSheet by remember { mutableStateOf(false) }
    var showVideoPlayerSheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dynamic Style selection based on Incognito state
    val currentBg = if (isIncognito) IncognitoBg else MaterialTheme.colorScheme.background
    val currentFrost = if (isIncognito) IncognitoFrost else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val currentMatte = if (isIncognito) IncognitoMatte else MaterialTheme.colorScheme.surface
    val currentLightElement = if (isIncognito) IncognitoLightElement else MaterialTheme.colorScheme.surfaceVariant
    val currentText = if (isIncognito) IncognitoText else MaterialTheme.colorScheme.onBackground
    val currentSecText = if (isIncognito) IncognitoSecText else MaterialTheme.colorScheme.onSurfaceVariant
    val currentBorder = if (isIncognito) IncognitoBorder else MaterialTheme.colorScheme.outlineVariant
    val currentAccent = if (isIncognito) IncognitoAccent else MaterialTheme.colorScheme.primary

    val currentTabActiveBg = if (isIncognito) IncognitoTabActiveBg else MaterialTheme.colorScheme.primaryContainer
    val currentTabActiveText = if (isIncognito) IncognitoTabActiveText else MaterialTheme.colorScheme.onPrimaryContainer
    val currentTabActiveBorder = if (isIncognito) IncognitoTabActiveBorder else MaterialTheme.colorScheme.primary

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(currentBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                            if (isIncognito) IncognitoTabActiveBg else currentTabActiveBg
                                        } else {
                                            if (isIncognito) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) {
                                            if (isIncognito) IncognitoTabActiveBorder else currentTabActiveBorder
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
                                        if (isIncognito) IncognitoTabActiveText else currentTabActiveText
                                    } else {
                                        currentText
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
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
                                        if (isIncognito) IncognitoTabActiveText.copy(alpha = 0.7f) else currentTabActiveText.copy(alpha = 0.7f)
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
                                value = urlInput,
                                onValueChange = { viewModel.setUrlInput(it) },
                                placeholder = {
                                    Text(
                                        "ابحث أو اكتب عنوان موقع ويب...",
                                        color = currentSecText,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
                                        viewModel.handleSearchOrUrl(urlInput)
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
                                        if (isIncognito) IncognitoAccent.copy(alpha = 0.2f) else currentAccent.copy(alpha = 0.1f)
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

                        // Ad Blocker quick toggle button with count badge
                        IconButton(
                            onClick = { showAdBlockerSheet = true },
                            modifier = Modifier
                                .testTag("ad_blocker_toggle_button")
                                .size(44.dp)
                                .background(
                                    color = if (isAdBlockerEnabled) {
                                        if (isIncognito) IncognitoAccent.copy(alpha = 0.2f) else currentAccent.copy(alpha = 0.1f)
                                    } else currentLightElement,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isAdBlockerEnabled) currentAccent else currentBorder,
                                    shape = CircleShape
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAdBlockerEnabled) Icons.Default.Block else Icons.Default.Block,
                                    contentDescription = "تبديل مانع الإعلانات",
                                    tint = if (isAdBlockerEnabled) currentAccent else currentSecText,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (isAdBlockerEnabled && blockedAdsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-10).dp)
                                            .background(Color.Red, CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = blockedAdsCount.toString(),
                                            color = Color.White,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Download Manager quick toggle button
                        IconButton(
                            onClick = { showDownloadsSheet = true },
                            modifier = Modifier
                                .testTag("downloads_toggle_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "مدير التحميلات",
                                    tint = currentSecText
                                )
                                val downloadingCount = downloads.count { it.status == "DOWNLOADING" }
                                if (downloadingCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-8).dp)
                                            .background(currentAccent, CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = downloadingCount.toString(),
                                            color = if (isIncognito) Color.Black else Color.White,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
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

                        Spacer(modifier = Modifier.width(6.dp))

                        // Save page offline button (حفظ الصفحة دون إنترنت)
                        IconButton(
                            onClick = { viewModel.triggerSavePage() },
                            modifier = Modifier
                                .testTag("save_offline_page_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "حفظ الصفحة للقراءة دون إنترنت",
                                tint = currentSecText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Translate quick toggle button
                        IconButton(
                            onClick = { showTranslationSheet = true },
                            modifier = Modifier
                                .testTag("translation_toggle_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "المترجم الذكي والترجمة الفورية",
                                tint = currentSecText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Images visualizer toggle button (عرض الصور)
                        IconButton(
                            onClick = { showImagesSheet = true },
                            modifier = Modifier
                                .testTag("images_viewer_toggle_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "مستعرض ومعرض الصور الذكي",
                                tint = currentSecText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Video Player toggle button (مشغل الفيديو)
                        IconButton(
                            onClick = { showVideoPlayerSheet = true },
                            modifier = Modifier
                                .testTag("video_player_toggle_button")
                                .size(44.dp)
                                .background(currentLightElement, CircleShape)
                                .border(1.dp, currentBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "مشغل الفيديو والميديا الذكي",
                                tint = currentSecText
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
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
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
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
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
                            .background(if (isIncognito) IncognitoTabActiveBg else currentTabActiveBg, RoundedCornerShape(20.dp))
                            .border(1.dp, if (isIncognito) IncognitoTabActiveBorder else currentTabActiveBorder, RoundedCornerShape(20.dp))
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
                            color = if (isIncognito) Color.White else currentTabActiveText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
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
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            HistoryAndBookmarksPanel(
                bookmarks = bookmarks,
                history = history,
                savedPages = savedPages,
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
                onSavedPageSelected = { filePath ->
                    viewModel.handleSearchOrUrl("file://$filePath")
                    showHistoryAndBookmarksSheet = false
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDeleteHistory = { viewModel.deleteHistoryEntry(it) },
                onDeleteSavedPage = { viewModel.deleteSavedPage(it) },
                onClearAllHistory = { viewModel.clearHistory() },
                onClearAllSavedPages = { viewModel.clearSavedPages() }
            )
        }
    }

    // Modal Bottom sheet for Switching and managing tabs
    if (showTabsListSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTabsListSheet = false },
            sheetState = rememberModalBottomSheetState(),
            dragHandle = { BottomSheetDefaults.DragHandle() },
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
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            val availableServers by viewModel.availableVpnServers.collectAsState()
            VpnSettingsPanel(
                isConnected = isVpnConnected,
                selectedServer = selectedVpnServer,
                servers = availableServers,
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

    // Modal Bottom sheet for Ad Blocker configuration
    if (showAdBlockerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAdBlockerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            AdBlockerSettingsPanel(
                isEnabled = isAdBlockerEnabled,
                blockedCount = blockedAdsCount,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onToggleAdBlocker = { viewModel.toggleAdBlocker() }
            )
        }
    }

    // Modal Bottom sheet for Download Manager configuration
    if (showDownloadsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDownloadsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            DownloadsManagerPanel(
                downloads = downloads,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onStartManualDownload = { url -> viewModel.startDownload(url) },
                onPauseDownload = { id -> viewModel.pauseDownload(id) },
                onResumeDownload = { id -> viewModel.resumeDownload(id) },
                onCancelDownload = { id -> viewModel.cancelDownload(id) },
                onDeleteDownload = { item -> viewModel.deleteDownload(item) },
                onClearAllDownloads = { viewModel.clearAllDownloads() }
            )
        }
    }

    // Modal Bottom sheet for Translation configuration
    if (showTranslationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTranslationSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            TranslationPanel(
                viewModel = viewModel,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onDismiss = { showTranslationSheet = false }
            )
        }
    }

    // Modal Bottom sheet for Images gallery & extractor (عرض الصور)
    if (showImagesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImagesSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            ImagesPanel(
                viewModel = viewModel,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onDismiss = { showImagesSheet = false }
            )
        }
    }

    // Modal Bottom sheet for Video player & extractor (مشغل الفيديو)
    if (showVideoPlayerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVideoPlayerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = currentMatte,
            tonalElevation = 16.dp,
        ) {
            VideoPanel(
                viewModel = viewModel,
                isIncognito = isIncognito,
                currentLightElement = currentLightElement,
                currentAccent = currentAccent,
                currentText = currentText,
                currentSecText = currentSecText,
                currentBorder = currentBorder,
                onDismiss = { showVideoPlayerSheet = false }
            )
        }
    }
}

// Subcomponent Layouts for History And Bookmarks light/dark theme
