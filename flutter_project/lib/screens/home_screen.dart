import 'package:flutter/material.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';
import 'fullscreen_screen.dart';
import 'font_screen.dart';
import 'color_screen.dart';
import 'background_screen.dart';
import 'settings_screen.dart';

/// HomeScreen
///
/// Educational Note:
/// - Centers the real-time clock preview.
/// - Hosts the prominent "FULL SCREEN CLOCK" action button.
/// - Provides quick navigation to Font, Color, Background, and Settings screens.
class HomeScreen extends StatelessWidget {
  final DateTime currentTime;
  final ClockSettings settings;
  final Function(ClockSettings) onUpdateSettings;

  const HomeScreen({
    super.key,
    required this.currentTime,
    required this.settings,
    required this.onUpdateSettings,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Digital Clock',
                        style: TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                      Text(
                        'Flutter Learning Project',
                        style: TextStyle(
                          fontSize: 13,
                          color: Colors.white60,
                        ),
                      ),
                    ],
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.white12,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      settings.fontStyle,
                      style: TextStyle(
                        color: settings.clockColor,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),

              // Central Clock Display Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(vertical: 40.0, horizontal: 16.0),
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.35),
                  borderRadius: BorderRadius.circular(24.0),
                ),
                child: ClockDisplay(
                  dateTime: currentTime,
                  settings: settings,
                  isFullScreen: false,
                ),
              ),

              // Action Buttons
              Column(
                children: [
                  // Prominent FULL SCREEN CLOCK Button
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: settings.clockColor,
                        foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        elevation: 4,
                      ),
                      icon: const Icon(Icons.fullscreen, size: 26),
                      label: const Text(
                        'FULL SCREEN CLOCK',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w900,
                          letterSpacing: 1.0,
                        ),
                      ),
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => FullscreenScreen(
                              settings: settings,
                            ),
                          ),
                        );
                      },
                    ),
                  ),

                  const SizedBox(height: 14),

                  // 4 Quick-Action Navigation Buttons
                  Row(
                    children: [
                      _QuickNavButton(
                        icon: Icons.text_fields,
                        label: 'Font',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => FontScreen(
                                currentTime: currentTime,
                                settings: settings,
                                onSelectFont: (font) {
                                  onUpdateSettings(settings.copyWith(fontStyle: font));
                                },
                              ),
                            ),
                          );
                        },
                      ),
                      const SizedBox(width: 8),
                      _QuickNavButton(
                        icon: Icons.color_lens,
                        label: 'Color',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => ColorScreen(
                                currentTime: currentTime,
                                settings: settings,
                                onSelectColor: (color) {
                                  onUpdateSettings(settings.copyWith(clockColor: color));
                                },
                              ),
                            ),
                          );
                        },
                      ),
                      const SizedBox(width: 8),
                      _QuickNavButton(
                        icon: Icons.image,
                        label: 'Background',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => BackgroundScreen(
                                currentTime: currentTime,
                                settings: settings,
                                onUpdateSettings: onUpdateSettings,
                              ),
                            ),
                          );
                        },
                      ),
                      const SizedBox(width: 8),
                      _QuickNavButton(
                        icon: Icons.settings,
                        label: 'Settings',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => SettingsScreen(
                                currentTime: currentTime,
                                settings: settings,
                                onUpdateSettings: onUpdateSettings,
                              ),
                            ),
                          );
                        },
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickNavButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _QuickNavButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          height: 66,
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.08),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, color: Colors.white, size: 22),
              const SizedBox(height: 4),
              Text(
                label,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
