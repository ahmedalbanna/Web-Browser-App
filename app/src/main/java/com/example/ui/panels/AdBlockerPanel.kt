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

