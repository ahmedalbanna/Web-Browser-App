package com.example.ui.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.WebBrowserViewModel

/**
 * Milestone 5.4 — Adaptive layout for large-screen & foldables
 * TODO v1.4: Wire WindowSizeClass + FoldingFeature
 *
 * Compact  → phone (current WebBrowserScreen)
 * Medium   → rail + two-pane (tabs + WebView)
 * Expanded → permanent drawer + draggable divider
 *
 * Stub — logs WindowSizeClass, delegates to phone for now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveBrowserScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: WebBrowserViewModel,
    modifier: Modifier = Modifier
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone — current
            com.example.ui.WebBrowserScreen(viewModel = viewModel, modifier = modifier)
        }
        WindowWidthSizeClass.Medium -> {
            // Tablet — rail + WebView (stub: still phone, but with rail placeholder)
            Row(modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // TODO: rail items (tabs, history, downloads)
                }
                Box(Modifier.weight(1f)) {
                    com.example.ui.WebBrowserScreen(viewModel = viewModel)
                }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Desktop — permanent drawer (stub)
            Row(modifier.fillMaxSize()) {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(Modifier.width(280.dp)) {
                            // TODO: drawer: bookmarks/history
                        }
                    }
                ) {
                    Box(Modifier.weight(1f)) {
                        com.example.ui.WebBrowserScreen(viewModel = viewModel)
                    }
                }
            }
        }
        else -> com.example.ui.WebBrowserScreen(viewModel = viewModel, modifier = modifier)
    }
}

// TODO v1.4: FoldingFeature support
// val foldingFeature = WindowInfoTracker.getOrCreate(context).windowLayoutInfo(...).displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
// if (foldingFeature?.isSeparating == true) { TwoPane(...) }
