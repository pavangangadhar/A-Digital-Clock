import 'package:flutter/material.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';

/// FontScreen
///
/// Educational Note:
/// - Explains how fonts are loaded and applied in Flutter:
///   1. In Flutter, fonts can come from standard device fonts ('monospace', 'serif', 'sans-serif').
///   2. Custom local TTF/OTF files can be bundled in pubspec.yaml under `flutter: fonts:`.
///   3. Online Google Fonts can be fetched on the fly via the `google_fonts` package.
///   4. Each card below re-renders the current live time formatted in that specific style.
class FontScreen extends StatelessWidget {
  final DateTime currentTime;
  final ClockSettings settings;
  final Function(String) onSelectFont;

  const FontScreen({
    super.key,
    required this.currentTime,
    required this.settings,
    required this.onSelectFont,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1117),
      appBar: AppBar(
        title: const Text('10 Clock Fonts', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF0D1117),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Educational Banner
          Container(
            padding: const EdgeInsets.all(14.0),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.06),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Row(
              children: [
                Icon(Icons.info_outline, color: settings.clockColor, size: 22),
                const SizedBox(width: 10),
                const Expanded(
                  child: Text(
                    'Tap any font below to update the clock immediately. Your choice is saved locally.',
                    style: TextStyle(color: Colors.white70, fontSize: 12),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),

          // 10 Font Cards
          ...ClockSettings.allFonts.map((fontName) {
            final isSelected = fontName == settings.fontStyle;
            final previewSettings = settings.copyWith(fontStyle: fontName);

            return Padding(
              padding: const EdgeInsets.only(bottom: 12.0),
              child: InkWell(
                onTap: () {
                  onSelectFont(fontName);
                },
                borderRadius: BorderRadius.circular(18),
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: isSelected
                        ? settings.clockColor.withOpacity(0.12)
                        : Colors.white.withOpacity(0.04),
                    borderRadius: BorderRadius.circular(18),
                    border: isSelected
                        ? Border.all(color: settings.clockColor, width: 2)
                        : null,
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            fontName,
                            style: TextStyle(
                              color: isSelected ? settings.clockColor : Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          if (isSelected)
                            CircleAvatar(
                              radius: 12,
                              backgroundColor: settings.clockColor,
                              child: const Icon(Icons.check, size: 16, color: Colors.black),
                            ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      Center(
                        child: ClockDisplay(
                          dateTime: currentTime,
                          settings: previewSettings,
                          isFullScreen: false,
                          previewScale: 0.75,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}
