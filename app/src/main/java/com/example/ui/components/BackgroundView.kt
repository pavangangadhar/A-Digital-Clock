package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.ClockSettings

/**
 * Preset Gradients for Backgrounds
 */
val PRESET_GRADIENTS = listOf(
    // 0: Midnight Horizon (Deep navy to cyber dark)
    listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF0A0E17)),
    // 1: Neon Cyber (Deep purple to electric blue)
    listOf(Color(0xFF1F0038), Color(0xFF0C1033), Color(0xFF002233)),
    // 2: Sunset Glow (Warm crimson to dusky amber)
    listOf(Color(0xFF2B0938), Color(0xFF5A1827), Color(0xFF1A0B10)),
    // 3: Emerald Deep (Dark spruce to obsidian green)
    listOf(Color(0xFF061A14), Color(0xFF0D3B2E), Color(0xFF04120D)),
    // 4: Royal Velvet (Deep violet to midnight black)
    listOf(Color(0xFF20072B), Color(0xFF38104A), Color(0xFF0E0414)),
    // 5: Aurora Borealis (Teal green and mystic night)
    listOf(Color(0xFF051C2C), Color(0xFF0B3C49), Color(0xFF031926)),
    // 6: Deep Space (Charcoal to onyx black)
    listOf(Color(0xFF121417), Color(0xFF1E2228), Color(0xFF0B0D0F))
)

val PRESET_GRADIENT_NAMES = listOf(
    "Midnight Horizon",
    "Neon Cyber",
    "Sunset Glow",
    "Emerald Deep",
    "Royal Velvet",
    "Aurora Borealis",
    "Deep Space"
)

val PRESET_SOLID_COLORS = listOf(
    Color(0xFF0D1117) to "Obsidian Black",
    Color(0xFF000000) to "Pure Black",
    Color(0xFF1A1E24) to "Dark Charcoal",
    Color(0xFF0F172A) to "Slate Navy",
    Color(0xFF1E1B4B) to "Midnight Indigo",
    Color(0xFF062B21) to "Forest Green",
    Color(0xFF2E081F) to "Deep Crimson",
    Color(0xFF374151) to "Steel Gray",
    Color(0xFFFFFFFF) to "Pure White"
)

/**
 * BackgroundContainer
 *
 * Renders the chosen background (Solid color, Gradient brush, or Gallery Photo)
 * behind the clock contents.
 *
 * Educational Note:
 * - Using Box as a background wrapper lets child Composables render seamlessly on top.
 * - Coil's AsyncImage efficiently loads images from local Android content:// URIs with caching.
 */
@Composable
fun BackgroundContainer(
    settings: ClockSettings,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                when (settings.bgType) {
                    ClockSettings.BG_SOLID -> {
                        Modifier.background(Color(settings.bgSolidHex))
                    }
                    ClockSettings.BG_GRADIENT -> {
                        val gradientColors = PRESET_GRADIENTS.getOrElse(settings.bgGradientIndex) {
                            PRESET_GRADIENTS[0]
                        }
                        Modifier.background(
                            Brush.verticalGradient(colors = gradientColors)
                        )
                    }
                    else -> Modifier.background(Color(0xFF0D1117))
                }
            )
    ) {
        // If Custom Image is selected, render image under content
        if (settings.bgType == ClockSettings.BG_IMAGE && settings.customImageUri != null) {
            AsyncImage(
                model = settings.customImageUri,
                contentDescription = "Custom Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Subtle dark overlay to maintain clock legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        content()
    }
}
