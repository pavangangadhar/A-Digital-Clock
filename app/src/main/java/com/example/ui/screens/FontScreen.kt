package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.components.ClockDisplay
import java.time.LocalDateTime

/**
 * FontScreen
 *
 * Educational Screen showcasing 10 distinct clock typography styles.
 *
 * Educational Note (How Fonts are Loaded and Applied):
 * - In Android Compose, typography styles can be composed via FontFamily (Monospace, SansSerif,
 *   Serif, Default) paired with FontWeight (Thin, Light, Bold, Black), LetterSpacing, and
 *   custom Canvas rendering (such as SevenSegmentClockView).
 * - In Flutter, fonts are loaded either through GoogleFonts package, declared in pubspec.yaml assets,
 *   or styled using standard TextStyle(fontFamily, fontWeight, letterSpacing).
 * - Every item in this list renders a live preview of the current time formatted with that specific style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
fun FontScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    onFontSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "10 Clock Font Styles",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("font_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Educational Explanatory Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.06f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Font Info",
                            tint = Color(settings.colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(6.dp))
                        Text(
                            text = "Tap any font below. The clock updates immediately and saves your choice to local storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // The 10 Font Styles List
            items(ClockSettings.ALL_FONTS) { fontName ->
                val isSelected = fontName == settings.fontStyle
                val previewSettings = settings.copy(fontStyle = fontName)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("font_card_$fontName")
                        .clickable { onFontSelected(fontName) }
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = Color(settings.colorHex),
                                    shape = RoundedCornerShape(18.dp)
                                )
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            Color(settings.colorHex).copy(alpha = 0.12f)
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fontName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color(settings.colorHex) else Color.White
                            )

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(settings.colorHex),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (isLightColor(settings.colorHex)) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live preview of the current time in this font
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ClockDisplay(
                                dateTime = dateTime,
                                settings = previewSettings,
                                isFullScreen = false,
                                previewScale = 0.72f
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
