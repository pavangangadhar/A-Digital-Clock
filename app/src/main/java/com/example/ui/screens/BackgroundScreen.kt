package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.components.BackgroundContainer
import com.example.ui.components.ClockDisplay
import com.example.ui.components.PRESET_GRADIENTS
import com.example.ui.components.PRESET_GRADIENT_NAMES
import com.example.ui.components.PRESET_SOLID_COLORS
import java.time.LocalDateTime

/**
 * BackgroundScreen
 *
 * Educational screen offering:
 * 1. Solid color backgrounds (Black, White, Slate, Navy, Forest, Crimson, etc.)
 * 2. Multi-stop Gradient backgrounds (Midnight, Cyber, Sunset, Aurora, etc.)
 * 3. Custom Photo from Gallery with zero-permission Android Photo Picker!
 *
 * Educational Note (How Gallery Image Selection Works):
 * - Android 13+ introduced the Photo Picker (backported to Android 4.4+ via Google Play services).
 * - Instead of requesting broad and dangerous READ_EXTERNAL_STORAGE permissions, the app invokes
 *   ActivityResultContracts.PickVisualMedia().
 * - The system UI manages file access and yields a scoped content:// URI directly to our callback.
 * - In Flutter, the equivalent behavior is provided by the popular `image_picker` package:
 *   final ImagePicker picker = ImagePicker();
 *   final XFile? image = await picker.pickImage(source: ImageSource.gallery);
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("NewApi")
@Composable
fun BackgroundScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    onSolidSelected: (Long) -> Unit,
    onGradientSelected: (Int) -> Unit,
    onImageSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember {
        mutableIntStateOf(
            when (settings.bgType) {
                ClockSettings.BG_GRADIENT -> 1
                ClockSettings.BG_IMAGE -> 2
                else -> 0
            }
        )
    }

    // Photo Picker Activity Launcher Contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri.toString())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clock Background",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("bg_back_btn")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card with Active Background
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                BackgroundContainer(settings = settings) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ClockDisplay(
                            dateTime = dateTime,
                            settings = settings,
                            isFullScreen = false,
                            previewScale = 0.75f
                        )
                    }
                }
            }

            // Tab navigation: Solid, Gradient, Custom Photo
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.05f),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(settings.colorHex)
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Solid") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Gradients") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Custom Photo") }
                )
            }

            when (selectedTab) {
                // Tab 0: Solid Colors
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Solid Colors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PRESET_SOLID_COLORS.forEach { (color, name) ->
                                val colorLong = (color.value.toLong() shr 32) and 0xFFFFFFFFL
                                val isSelected = settings.bgType == ClockSettings.BG_SOLID &&
                                        (settings.bgSolidHex and 0xFFFFFFFFL) == (color.value.toLong() and 0xFFFFFFFFL)

                                Surface(
                                    onClick = { onSolidSelected(color.value.toLong()) },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .testTag("solid_bg_$name"),
                                    shape = CircleShape,
                                    color = color,
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(3.dp, Color(settings.colorHex))
                                    } else {
                                        androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                    }
                                ) {
                                    if (isSelected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (isLightColor(color.value.toLong())) Color.Black else Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 1: Gradient Presets
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Preset Gradients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        PRESET_GRADIENTS.forEachIndexed { index, gradientColors ->
                            val isSelected = settings.bgType == ClockSettings.BG_GRADIENT && settings.bgGradientIndex == index
                            val gradientName = PRESET_GRADIENT_NAMES.getOrElse(index) { "Gradient #$index" }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .testTag("gradient_card_$index")
                                    .clickable { onGradientSelected(index) }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = Color(settings.colorHex),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.horizontalGradient(gradientColors))
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = gradientName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
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
                                }
                            }
                        }
                    }
                }

                // Tab 2: Custom Photo Selection
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Custom Photo from Gallery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color(settings.colorHex),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Privacy-friendly Zero-Permission Photo Picker",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = "This app launches the system Photo Picker directly without requesting any sensitive media storage permissions. Select any personal wallpaper or photo to display behind your digital clock.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("pick_photo_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(settings.colorHex)
                                    )
                                ) {
                                    val contentColor = if (isLightColor(settings.colorHex)) Color.Black else Color.White
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Pick Image",
                                        tint = contentColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Choose Photo from Gallery",
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
