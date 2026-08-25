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

