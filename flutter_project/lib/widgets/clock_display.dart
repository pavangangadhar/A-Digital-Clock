import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/clock_settings.dart';

/// ClockDisplay Widget
///
/// Educational Note:
/// - Stateless widget taking DateTime and ClockSettings.
/// - Demonstrates date and time formatting using package:intl.
/// - Maps 10 custom typography styles cleanly using standard Flutter TextStyle properties.
class ClockDisplay extends StatelessWidget {
  final DateTime dateTime;
  final ClockSettings settings;
  final bool isFullScreen;
  final double previewScale;

  const ClockDisplay({
    super.key,
    required this.dateTime,
    required this.settings,
    this.isFullScreen = false,
    this.previewScale = 1.0,
  });

  @override
  Widget build(BuildContext context) {
    // 1. Format time based on 12-hour or 24-hour setting and seconds toggle
    final timePattern = StringBuffer();
    if (settings.is24Hour) {
      timePattern.write('HH:mm');
    } else {
      timePattern.write('hh:mm');
    }

    if (settings.showSeconds) {
      timePattern.write(':ss');
    }

    if (!settings.is24Hour) {
      timePattern.write(' a');
    }

    final formattedTime = DateFormat(timePattern.toString()).format(dateTime);

    // 2. Format date (e.g., "Thursday, September 3, 2026")
    final formattedDate = DateFormat('EEEE, MMMM d, yyyy').format(dateTime);

    // 3. Calculate responsive font sizes
    final (timeFontSize, dateFontSize) = _calculateFontSizes();

    // 4. Alignment based on settings
    final mainAlignment = switch (settings.clockPosition) {
      'top' => MainAxisAlignment.start,
      'bottom' => MainAxisAlignment.end,
      _ => MainAxisAlignment.center,
    };

    return Column(
      mainAxisSize: MainAxisSize.min,
      mainAxisAlignment: mainAlignment,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        // Main Clock Digits
        Text(
          formattedTime,
          textAlign: TextAlign.center,
          style: _getClockTextStyle(timeFontSize),
        ),

        // Optional Date Row
        if (settings.showDate) ...[
          SizedBox(height: 10 * previewScale),
          Text(
            formattedDate,
            textAlign: TextAlign.center,
            style: _getDateTextStyle(dateFontSize),
          ),
        ],

        // Optional Custom Note Badge / Pill
        if (settings.showNote && settings.customNote.trim().isNotEmpty) ...[
          SizedBox(height: 12 * previewScale),
          Container(
            padding: EdgeInsets.symmetric(
              horizontal: 14 * previewScale,
              vertical: 6 * previewScale,
            ),
            decoration: BoxDecoration(
              color: Colors.black.withValues(alpha: 0.35),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: settings.clockColor.withValues(alpha: 0.4),
                width: 1.2,
              ),
            ),
            child: Text(
              settings.customNote,
              textAlign: TextAlign.center,
              style: TextStyle(
                color: settings.clockColor,
                fontSize: (dateFontSize * 0.95).clamp(11.0, 20.0),
                fontWeight: FontWeight.w600,
                letterSpacing: 0.5,
              ),
            ),
          ),
        ],
      ],
    );
  }

  (double, double) _calculateFontSizes() {
    double base = switch (settings.clockSize) {
      'small' => isFullScreen ? 52.0 : 38.0,
      'medium' => isFullScreen ? 68.0 : 48.0,
      'large' => isFullScreen ? 84.0 : 58.0,
      'xlarge' => isFullScreen ? 100.0 : 68.0,
      _ => 58.0,
    };
    base *= previewScale;
    final dateSize = (base * 0.28).clamp(12.0, 26.0);
    return (base, dateSize);
  }

  /// Resolves the 10 custom typography styles
  TextStyle _getClockTextStyle(double fontSize) {
    return switch (settings.fontStyle) {
      'Digital' => TextStyle(
          fontFamily: 'Courier',
          fontSize: fontSize,
          fontWeight: FontWeight.bold,
          letterSpacing: 3.0,
          color: settings.clockColor,
        ),
      'Minimal' => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w200,
          letterSpacing: 1.0,
          color: settings.clockColor,
        ),
      'Thin' => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w100,
          letterSpacing: 0.5,
          color: settings.clockColor,
        ),
      'Bold' => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w900,
          letterSpacing: -1.0,
          color: settings.clockColor,
        ),
      'Futuristic' => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w600,
          letterSpacing: 6.0,
          color: settings.clockColor,
        ),
      'Seven Segment' => TextStyle(
          fontFamily: 'monospace',
          fontSize: fontSize,
          fontWeight: FontWeight.bold,
          letterSpacing: 4.0,
          color: settings.clockColor,
        ),
      'Rounded' => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w500,
          letterSpacing: 2.0,
          color: settings.clockColor,
        ),
      'Retro' => TextStyle(
          fontFamily: 'monospace',
          fontSize: fontSize,
          fontWeight: FontWeight.w400,
          letterSpacing: 5.0,
          color: settings.clockColor,
        ),
      'Monospace' => TextStyle(
          fontFamily: 'monospace',
          fontSize: fontSize,
          fontWeight: FontWeight.w500,
          letterSpacing: 1.0,
          color: settings.clockColor,
        ),
      'Serif' => TextStyle(
          fontFamily: 'serif',
          fontSize: fontSize,
          fontWeight: FontWeight.w400,
          letterSpacing: 1.5,
          color: settings.clockColor,
        ),
      _ => TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w500,
          color: settings.clockColor,
        ),
    };
  }

  TextStyle _getDateTextStyle(double fontSize) {
    final color = settings.clockColor.withOpacity(0.85);
    return switch (settings.fontStyle) {
      'Serif' => TextStyle(
          fontFamily: 'serif',
          fontStyle: FontStyle.italic,
          fontSize: fontSize,
          color: color,
        ),
      'Digital' || 'Retro' || 'Monospace' || 'Seven Segment' => TextStyle(
          fontFamily: 'monospace',
          fontSize: fontSize,
          letterSpacing: 1.5,
          color: color,
        ),
      'Futuristic' => TextStyle(
          fontSize: fontSize,
          letterSpacing: 3.0,
          color: color,
        ),
      _ => TextStyle(
          fontSize: fontSize,
          letterSpacing: 0.5,
          color: color,
        ),
    };
  }
}
