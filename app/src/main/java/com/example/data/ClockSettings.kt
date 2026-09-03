package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ClockSettings
 *
 * This entity model represents the user's saved clock preferences stored locally in the Room database.
 * Each property has beginner-friendly default values.
 *
 * Educational Note:
 * - @Entity tells Room (SQLite abstraction) to create a database table named "clock_settings".
 * - @PrimaryKey(autoGenerate = false) ensures we only store a single configuration row (id = 1).
 *   Whenever settings change, we update this exact row.
 */
@Entity(tableName = "clock_settings")
data class ClockSettings(
    @PrimaryKey
    val id: Int = 1,

    // Time Formatting
    val is24Hour: Boolean = false,      // false = 12-hour format with AM/PM, true = 24-hour format
    val showSeconds: Boolean = true,    // Toggle displaying :SS
    val showDate: Boolean = true,       // Toggle displaying the date line

    // Custom Note
    val customNote: String = "",        // Custom message or note displayed with clock
    val showNote: Boolean = true,       // Toggle displaying the custom note

    // Visual Customization
    val fontStyle: String = FONT_DIGITAL, // 1 of 10 supported font styles
    val colorHex: Long = 0xFF00FFCC,       // Neon Cyan default (ARGB as Long)

    // Background Customization
    val bgType: String = BG_SOLID,      // "solid", "gradient", or "image"
    val bgSolidHex: Long = 0xFF0D1117,  // Dark cyber canvas default
    val bgGradientIndex: Int = 0,       // Preset gradient index (0 to 6)
    val customImageUri: String? = null, // URI string if user selected a gallery picture

    // Layout
    val clockSize: String = SIZE_LARGE,       // "small", "medium", "large", "xlarge"
    val clockPosition: String = POS_CENTER    // "center", "top", "bottom"
) {
    companion object {
        // 10 Clock Font Styles
        const val FONT_DIGITAL = "Digital"
        const val FONT_MINIMAL = "Minimal"
        const val FONT_THIN = "Thin"
        const val FONT_BOLD = "Bold"
        const val FONT_FUTURISTIC = "Futuristic"
        const val FONT_SEVEN_SEGMENT = "Seven Segment"
        const val FONT_ROUNDED = "Rounded"
        const val FONT_RETRO = "Retro"
        const val FONT_MONOSPACE = "Monospace"
        const val FONT_SERIF = "Serif"

        val ALL_FONTS = listOf(
            FONT_DIGITAL,
            FONT_MINIMAL,
            FONT_THIN,
            FONT_BOLD,
            FONT_FUTURISTIC,
            FONT_SEVEN_SEGMENT,
            FONT_ROUNDED,
            FONT_RETRO,
            FONT_MONOSPACE,
            FONT_SERIF
        )

        // Background Types
        const val BG_SOLID = "solid"
        const val BG_GRADIENT = "gradient"
        const val BG_IMAGE = "image"

        // Clock Sizes
        const val SIZE_SMALL = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE = "large"
        const val SIZE_XLARGE = "xlarge"

        // Clock Positions
        const val POS_TOP = "top"
        const val POS_CENTER = "center"
        const val POS_BOTTOM = "bottom"

        // Default instance
        val DEFAULT = ClockSettings()
    }
}
