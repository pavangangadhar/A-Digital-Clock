import 'package:flutter/material.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';
import '../widgets/setting_tile.dart';

/// SettingsScreen
///
/// Educational Note:
/// - Controls functional time parameters and geometry layout.
/// - Demonstrates state propagation: calling onUpdateSettings(newSettings)
///   immediately updates the parent state, which triggers a rebuild across all widgets.
class SettingsScreen extends StatelessWidget {
  final DateTime currentTime;
  final ClockSettings settings;
  final Function(ClockSettings) onUpdateSettings;

  const SettingsScreen({
    super.key,
    required this.currentTime,
    required this.settings,
    required this.onUpdateSettings,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1117),
      appBar: AppBar(
        title: const Text('Time & Clock Settings', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF0D1117),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Live Clock Preview
          Container(
            height: 150,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.black.withOpacity(0.4),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Center(
              child: ClockDisplay(
                dateTime: currentTime,
                settings: settings,
                isFullScreen: false,
                previewScale: 0.72,
              ),
            ),
          ),
          const SizedBox(height: 16),

          // 1. Time Format Setting
          SettingCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Time Format',
                  style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: _ChoiceChip(
                        label: '12-Hour (AM/PM)',
                        isSelected: !settings.is24Hour,
                        accentColor: settings.clockColor,
                        onTap: () {
                          onUpdateSettings(settings.copyWith(is24Hour: false));
                        },
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: _ChoiceChip(
                        label: '24-Hour',
                        isSelected: settings.is24Hour,
                        accentColor: settings.clockColor,
                        onTap: () {
                          onUpdateSettings(settings.copyWith(is24Hour: true));
                        },
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // 2. Seconds and Date Toggles
          SettingCard(
            child: Column(
              children: [
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Show Seconds', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                  subtitle: const Text('Display :SS on clock', style: TextStyle(color: Colors.white60, fontSize: 12)),
                  value: settings.showSeconds,
                  activeColor: settings.clockColor,
                  onChanged: (val) {
                    onUpdateSettings(settings.copyWith(showSeconds: val));
                  },
                ),
                const Divider(color: Colors.white10),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Show Date', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                  subtitle: const Text('Display Day, Month, Date, Year', style: TextStyle(color: Colors.white60, fontSize: 12)),
                  value: settings.showDate,
                  activeColor: settings.clockColor,
                  onChanged: (val) {
                    onUpdateSettings(settings.copyWith(showDate: val));
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // 3. Custom Note Setting
          SettingCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Custom Note',
                          style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        SizedBox(height: 2),
                        Text(
                          'Show a personal note with the clock',
                          style: TextStyle(color: Colors.white54, fontSize: 13),
                        ),
                      ],
                    ),
                    Switch(
                      value: settings.showNote,
                      activeColor: settings.clockColor,
                      onChanged: (val) {
                        onUpdateSettings(settings.copyWith(showNote: val));
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextFormField(
                  initialValue: settings.customNote,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Enter custom note...',
                    hintStyle: const TextStyle(color: Colors.white38),
                    prefixIcon: Icon(Icons.note_alt_outlined, color: settings.clockColor),
                    filled: true,
                    fillColor: Colors.black26,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide(color: settings.clockColor.withOpacity(0.3)),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide(color: settings.clockColor),
                    ),
                  ),
                  onChanged: (val) {
                    onUpdateSettings(settings.copyWith(customNote: val));
                  },
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 6,
                  runSpacing: 6,
                  children: [
                    'Focus Time 🎯',
                    'Stay Hydrated 💧',
                    'Study Session 📚',
                    'Be Present ✨',
                  ].map((preset) {
                    final isSelected = settings.customNote == preset;
                    return ActionChip(
                      label: Text(
                        preset,
                        style: TextStyle(
                          fontSize: 12,
                          color: isSelected ? Colors.black : Colors.white,
                          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                        ),
                      ),
                      backgroundColor: isSelected ? settings.clockColor : Colors.white.withOpacity(0.1),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      onPressed: () {
                        onUpdateSettings(settings.copyWith(customNote: preset));
                      },
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // 4. Clock Size
          SettingCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Clock Size',
                  style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    for (final (key, label) in [
                      ('small', 'Small'),
                      ('medium', 'Med'),
                      ('large', 'Large'),
                      ('xlarge', 'XL'),
                    ]) ...[
                      Expanded(
                        child: _ChoiceChip(
                          label: label,
                          isSelected: settings.clockSize == key,
                          accentColor: settings.clockColor,
                          onTap: () {
                            onUpdateSettings(settings.copyWith(clockSize: key));
                          },
                        ),
                      ),
                      if (key != 'xlarge') const SizedBox(width: 6),
                    ],
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // 4. Clock Position
          SettingCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Clock Position',
                  style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    for (final (key, label) in [
                      ('top', 'Top'),
                      ('center', 'Center'),
                      ('bottom', 'Bottom'),
                    ]) ...[
                      Expanded(
                        child: _ChoiceChip(
                          label: label,
                          isSelected: settings.clockPosition == key,
                          accentColor: settings.clockColor,
                          onTap: () {
                            onUpdateSettings(settings.copyWith(clockPosition: key));
                          },
                        ),
                      ),
                      if (key != 'bottom') const SizedBox(width: 6),
                    ],
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Reset to Defaults
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: Colors.redAccent,
              side: const BorderSide(color: Colors.redAccent),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(14),
              ),
              padding: const EdgeInsets.symmetric(vertical: 14),
            ),
            icon: const Icon(Icons.restart_alt),
            label: const Text('Reset All Settings to Defaults'),
            onPressed: () {
              onUpdateSettings(const ClockSettings());
            },
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}

class _ChoiceChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final Color accentColor;
  final VoidCallback onTap;

  const _ChoiceChip({
    required this.label,
    required this.isSelected,
    required this.accentColor,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 42,
        decoration: BoxDecoration(
          color: isSelected ? accentColor : Colors.white10,
          borderRadius: BorderRadius.circular(12),
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.black : Colors.white,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
            fontSize: 12,
          ),
        ),
      ),
    );
  }
}
