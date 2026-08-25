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

