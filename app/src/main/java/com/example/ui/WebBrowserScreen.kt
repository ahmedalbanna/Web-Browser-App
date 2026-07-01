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
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onAddNewTab,
                colors = ButtonDefaults.buttonColors(containerColor = currentLightElement, contentColor = currentAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تبويب جديد", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
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
                            if (isIncognito) IncognitoTabActiveBg else MaterialTheme.colorScheme.primaryContainer
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
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tab.url,
                                color = currentSecText,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        IconButton(onClick = { onCloseTab(tab.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = if (isActive) {
                                    if (isIncognito) IncognitoTabActiveText else MaterialTheme.colorScheme.onPrimaryContainer
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
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = currentSecText,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
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
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
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
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
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
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
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
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
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
                    Text("عنوان الـ IP", color = currentSecText, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) selectedServer.ipAddress else "---.---.---.---",
                        color = currentText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                        fontWeight = FontWeight.Bold,
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
                    Text("سرعة الاستجابة", color = currentSecText, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) "$ping ms" else "--- ms",
                        color = if (isConnected) {
                            if (ping < 35) SecureGreen else if (ping < 90) currentAccent else Color.Yellow
                        } else currentSecText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
                    Text("بيانات مشفرة", color = currentSecText, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) String.format(Locale.US, "%.3f MB", dataUsage) else "0.000 MB",
                        color = currentText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
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
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = server.name,
                            color = currentText,
                            fontWeight = FontWeight.Bold,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "منفذ: ${server.ipAddress}:${server.port}",
                            color = currentSecText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
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
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdBlockerSettingsPanel(
    isEnabled: Boolean,
    blockedCount: Int,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onToggleAdBlocker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Upper Title & Shield Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isEnabled) currentAccent else currentSecText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "درع حظر الإعلانات والتعقب",
                    color = currentText,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }

            // Connection Status Glow Pill
            Box(
                modifier = Modifier
                    .background(
                        color = if (isEnabled) currentAccent.copy(alpha = 0.15f) else currentLightElement,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isEnabled) currentAccent else currentBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isEnabled) "الدرع نشط" else "الدرع متوقف",
                    color = if (isEnabled) currentAccent else currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, currentBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) currentAccent.copy(alpha = 0.05f) else currentLightElement.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Power button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = if (isEnabled) currentAccent else currentLightElement,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isEnabled) Color.White.copy(alpha = 0.6f) else currentBorder,
                            shape = CircleShape
                        )
                        .clickable { onToggleAdBlocker() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "تبديل حالة الدرع",
                        tint = if (isEnabled) (if (isIncognito) Color.Black else Color.White) else currentSecText,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isEnabled) "اضغط هنا لتعطيل مانع الإعلانات" else "اضغط هنا لتفعيل مانع الإعلانات الذكي",
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real-time blocked count counter
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, currentBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الإعلانات المحجوبة على الصفحة الحالية",
                        color = currentText,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "يقوم النظام بتصفية الموارد لتسريع التصفح",
                        color = currentSecText,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (blockedCount > 0) Color.Red.copy(alpha = 0.15f) else currentLightElement,
                            CircleShape
                        )
                        .size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = blockedCount.toString(),
                        color = if (blockedCount > 0) Color.Red else currentSecText,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ad Blocker Advantages Checklist
        Text(
            text = "مزايا درع التصفية المتقدم",
            color = currentText,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val advantages = listOf(
                "منع تعقب الإعلانات والأنشطة الإعلانية لخصوصية أكبر",
                "حظر النوافذ المنبثقة وإطارات الفيديو المزعجة ومحتوى السبام",
                "تصفح فائق السرعة بفضل منع تنزيل ملفات الإعلانات الضخمة",
                "توفير هائل في استهلاك باقة الإنترنت وتحسين الأداء وعمر البطارية"
            )

            advantages.forEach { advantage ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isEnabled) currentAccent else currentSecText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = advantage,
                        color = currentText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

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
                                    android.widget.Toast.makeText(context, "تم فتح الملف بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
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

@Composable
fun TranslationPanel(
    viewModel: WebBrowserViewModel,
    isIncognito: Boolean,
    currentLightElement: Color,
    currentAccent: Color,
    currentText: Color,
    currentSecText: Color,
    currentBorder: Color,
    onDismiss: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var sourceLang by remember { mutableStateOf("Auto") }
    var targetLang by remember { mutableStateOf("Arabic") }

    val isTranslating by viewModel.isTranslating.collectAsState()
    val translatedResult by viewModel.translatedResult.collectAsState()
    val translationError by viewModel.translationError.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle bar in bottom sheet
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(currentSecText.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = currentAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "مساعد الترجمة الذكي",
                    color = currentText,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ترجمة المواقع والنصوص الفورية بدعم من الذكاء الاصطناعي",
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Page Translation Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = currentLightElement),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ترجمة صفحة الويب الحالية للعربية",
                        color = currentText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "سيتم تحويل الموقع المعروض حالياً بالكامل ليعمل باللغة العربية مع دعم تدوير النصوص التفاعلية RTL تلقائياً وبسهولة.",
                    color = currentSecText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.translateActiveWebPage()
                        onDismiss()
                        android.widget.Toast.makeText(context, "جاري بدء ترجمة موقع الويب...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("translate_page_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GTranslate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ترجمة الموقع الحالي كاملاً",
                        color = if (isIncognito) Color.Black else Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        HorizontalDivider(color = currentBorder, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Text Translation Box
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "الترجمة الفورية للنصوص",
                color = currentText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Selection Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val config1 = "كشف تلقائي ➔ العربية"
                val config2 = "الإنجليزية ➔ العربية"
                val config3 = "العربية ➔ الإنجليزية"

                // Chip 1
                val isSelected1 = sourceLang == "Auto" && targetLang == "Arabic"
                FilterChip(
                    selected = isSelected1,
                    onClick = {
                        sourceLang = "Auto"
                        targetLang = "Arabic"
                    },
                    label = { Text(config1, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentAccent.copy(alpha = 0.15f),
                        selectedLabelColor = currentAccent
                    )
                )

                // Chip 2
                val isSelected2 = sourceLang == "English" && targetLang == "Arabic"
                FilterChip(
                    selected = isSelected2,
                    onClick = {
                        sourceLang = "English"
                        targetLang = "Arabic"
                    },
                    label = { Text(config2, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentAccent.copy(alpha = 0.15f),
                        selectedLabelColor = currentAccent
                    )
                )

                // Chip 3
                val isSelected3 = sourceLang == "Arabic" && targetLang == "English"
                FilterChip(
                    selected = isSelected3,
                    onClick = {
                        sourceLang = "Arabic"
                        targetLang = "English"
                    },
                    label = { Text(config3, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentAccent.copy(alpha = 0.15f),
                        selectedLabelColor = currentAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-line Input Box
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("translate_text_input"),
                placeholder = {
                    Text(
                        text = "اكتب أو الصق النص هنا للترجمة...",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = currentSecText
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color = currentText
                ),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = currentBorder,
                    focusedLabelColor = currentAccent,
                    cursorColor = currentAccent
                ),
                trailingIcon = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (rawText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    rawText = ""
                                    viewModel.clearTranslation()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = currentSecText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        IconButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text ?: ""
                                if (clipText.isNotEmpty()) {
                                    rawText = clipText
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "لصق الحافظة",
                                tint = currentAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translate Text Action Button
            Button(
                onClick = {
                    viewModel.translateText(rawText, sourceLang, targetLang)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("translate_text_submit_button"),
                enabled = rawText.isNotBlank() && !isTranslating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    disabledContainerColor = currentLightElement
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = if (isIncognito) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جاري الترجمة بذكاء Gemini...",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ترجمة النص الآن ✨",
                        color = if (isIncognito) Color.Black else Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Results UI Display
            if (isTranslating) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = currentLightElement.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "جاري التواصل مع خادم الترجمة فائق السرعة...",
                            color = currentSecText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (translatedResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isIncognito) currentLightElement else Color(0xFFEBF3FC)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isIncognito) currentBorder else currentAccent.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "النص المترجم:",
                                color = currentAccent,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Quick Copy Button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(translatedResult))
                                    android.widget.Toast.makeText(context, "تم نسخ الترجمة للحافظة 📋", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ النص",
                                    tint = currentAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Output text supporting vertical alignment
                        Text(
                            text = translatedResult,
                            color = currentText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            lineHeight = 19.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (translationError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8B4B4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "خطأ",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translationError!!,
                            color = Color(0xFFC81E1E),
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
                    }
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

            // Top Control Overlay Header bar
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

