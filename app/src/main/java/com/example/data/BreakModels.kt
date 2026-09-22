package com.example.data

/**
 * State representing an active reading break in full-screen mode.
 */
data class ReadingBreakState(
    val isBreakActive: Boolean = false,
    val breakTotalSeconds: Int = 300,
    val breakRemainingSeconds: Int = 300,
    val isCompleted: Boolean = false,
    val selectedDurationMinutes: Int = 5
) {
    val progress: Float
        get() = if (breakTotalSeconds > 0) {
            ((breakTotalSeconds - breakRemainingSeconds).toFloat() / breakTotalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val formattedRemaining: String
        get() {
            val minutes = breakRemainingSeconds / 60
            val seconds = breakRemainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

/**
 * Intelligent real-time break suggestion computed from reading duration.
 */
data class BreakSuggestion(
    val durationMinutes: Int,
    val title: String,
    val reason: String,
    val tip: String,
    val badgeLabel: String
)

/**
 * Standard break duration options in minutes.
 */
val BREAK_PRESET_MINUTES = listOf(5, 10, 15)

/**
 * Curated list of eye-rest and relaxation tips shown during reading breaks.
 */
val BREAK_HEALTH_TIPS = listOf(
    "👁️ 20-20-20 Rule: Look at an object 20 feet away for 20 seconds to reset your focal length.",
    "💧 Stay hydrated: Sip a glass of water to refresh your brain and maintain alertness.",
    "🙆‍♂️ Shoulder & Neck Roll: Gently roll your shoulders back 5 times and ease neck tension.",
    "👀 Palming Technique: Rub your palms together until warm, then cup them gently over closed eyes.",
    "🚶‍♂️ Take a Walk: Take a quick 1-minute walk around the room to boost blood circulation.",
    "🧘 Deep Breathing: Inhale slowly through your nose for 4 seconds, hold for 4, exhale for 6.",
    "✨ Blink Exercise: Slowly blink 10 times to re-moisturize your cornea and reduce dryness."
)

/**
 * Computes an intelligent, real-time break suggestion based on elapsed reading seconds.
 *
 * - Less than 25 minutes: 5-minute micro eye-rest break.
 * - 25 to 50 minutes: 10-minute stretch & hydrate break.
 * - 50+ minutes: 15-minute restorative deep break.
 */
fun computeRealTimeBreakSuggestion(elapsedSeconds: Long): BreakSuggestion {
    val elapsedMinutes = (elapsedSeconds / 60).toInt()

    return when {
        elapsedMinutes < 25 -> {
            BreakSuggestion(
                durationMinutes = 5,
                title = "5-Min Quick Eye Rest",
                reason = if (elapsedMinutes >= 15) {
                    "You've been reading for $elapsedMinutes min. A quick micro-break keeps eye strain away!"
                } else {
                    "Short focus interval! A 5-minute eye rest keeps your vision sharp."
                },
                tip = "Practice the 20-20-20 rule: look 20 feet away to relax eye muscles.",
                badgeLabel = "Suggested: 5m"
            )
        }
        elapsedMinutes < 50 -> {
            BreakSuggestion(
                durationMinutes = 10,
                title = "10-Min Stretch & Hydrate",
                reason = "Solid focus! You've been reading for $elapsedMinutes min. Time for a refreshing break.",
                tip = "Stand up, drink some water, roll your shoulders, and rest your eyes.",
                badgeLabel = "Suggested: 10m"
            )
        }
        else -> {
            BreakSuggestion(
                durationMinutes = 15,
                title = "15-Min Deep Recharge",
                reason = "Incredible deep reading session ($elapsedMinutes min)! A thorough break restores cognitive retention.",
                tip = "Step away from reading, take a short stroll, grab a healthy snack, and recharge.",
                badgeLabel = "Suggested: 15m"
            )
        }
    }
}
