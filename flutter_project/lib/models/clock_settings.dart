import 'package:flutter/material.dart';

/// Model representing all customizable clock preferences.
///
/// Educational Note:
/// - In Flutter/Dart, immutable data classes with copyWith() are the gold standard.
/// - When an attribute changes, we emit a new ClockSettings instance via copyWith(),
///   which triggers reactive widget rebuilds cleanly without mutation side-effects.
class ClockSettings {
  final bool is24Hour;
  final bool showSeconds;
  final bool showDate;
  final String fontStyle;
  final Color clockColor;
  final String bgType; // 'solid', 'gradient', or 'image'
  final Color bgSolidColor;
  final int bgGradientIndex;
  final String? customImagePath;
  final String clockSize; // 'small', 'medium', 'large', 'xlarge'
  final String clockPosition; // 'center', 'top', 'bottom'
  final String customNote;
  final bool showNote;

  const ClockSettings({
    this.is24Hour = false,
    this.showSeconds = true,
    this.showDate = true,
    this.fontStyle = 'Digital',
    this.clockColor = const Color(0xFF00E5FF),
    this.bgType = 'solid',
    this.bgSolidColor = const Color(0xFF0D1117),
    this.bgGradientIndex = 0,
    this.customImagePath,
    this.clockSize = 'large',
    this.clockPosition = 'center',
    this.customNote = 'Stay Focused 🎯',
    this.showNote = true,
  });

  static const List<String> allFonts = [
    'Digital',
    'Minimal',
    'Thin',
    'Bold',
    'Futuristic',
    'Seven Segment',
    'Rounded',
    'Retro',
    'Monospace',
    'Serif',
  ];

  ClockSettings copyWith({
    bool? is24Hour,
    bool? showSeconds,
    bool? showDate,
    String? fontStyle,
    Color? clockColor,
    String? bgType,
    Color? bgSolidColor,
    int? bgGradientIndex,
    String? customImagePath,
    String? clockSize,
    String? clockPosition,
    String? customNote,
    bool? showNote,
  }) {
    return ClockSettings(
      is24Hour: is24Hour ?? this.is24Hour,
      showSeconds: showSeconds ?? this.showSeconds,
      showDate: showDate ?? this.showDate,
      fontStyle: fontStyle ?? this.fontStyle,
      clockColor: clockColor ?? this.clockColor,
      bgType: bgType ?? this.bgType,
      bgSolidColor: bgSolidColor ?? this.bgSolidColor,
      bgGradientIndex: bgGradientIndex ?? this.bgGradientIndex,
      customImagePath: customImagePath ?? this.customImagePath,
      clockSize: clockSize ?? this.clockSize,
      clockPosition: clockPosition ?? this.clockPosition,
      customNote: customNote ?? this.customNote,
      showNote: showNote ?? this.showNote,
    );
  }
}
