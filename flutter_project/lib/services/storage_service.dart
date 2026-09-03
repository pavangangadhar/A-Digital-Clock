import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/clock_settings.dart';

/// Service responsible for saving and restoring user settings locally.
///
/// Educational Note:
/// - In Flutter, SharedPreferences provides simple key-value local storage on both
///   Android (NSUserDefaults on iOS, SharedPreferences XML on Android).
/// - No remote server or complex database setup is required.
/// - Whenever a user changes a setting, we write it asynchronously with setString / setBool / setInt.
/// - When the app launches, we load these values in main() before displaying the UI.
class StorageService {
  static const _keyIs24Hour = 'clock_is_24_hour';
  static const _keyShowSeconds = 'clock_show_seconds';
  static const _keyShowDate = 'clock_show_date';
  static const _keyFontStyle = 'clock_font_style';
  static const _keyClockColor = 'clock_color_value';
  static const _keyBgType = 'clock_bg_type';
  static const _keyBgSolidColor = 'clock_bg_solid_value';
  static const _keyBgGradientIndex = 'clock_bg_gradient_index';
  static const _keyCustomImagePath = 'clock_custom_image_path';
  static const _keyClockSize = 'clock_size';
  static const _keyClockPosition = 'clock_position';

  /// Saves the complete ClockSettings object to local storage.
  static Future<void> saveSettings(ClockSettings settings) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyIs24Hour, settings.is24Hour);
    await prefs.setBool(_keyShowSeconds, settings.showSeconds);
    await prefs.setBool(_keyShowDate, settings.showDate);
    await prefs.setString(_keyFontStyle, settings.fontStyle);
    await prefs.setInt(_keyClockColor, settings.clockColor.value);
    await prefs.setString(_keyBgType, settings.bgType);
    await prefs.setInt(_keyBgSolidColor, settings.bgSolidColor.value);
    await prefs.setInt(_keyBgGradientIndex, settings.bgGradientIndex);
    if (settings.customImagePath != null) {
      await prefs.setString(_keyCustomImagePath, settings.customImagePath!);
    } else {
      await prefs.remove(_keyCustomImagePath);
    }
    await prefs.setString(_keyClockSize, settings.clockSize);
    await prefs.setString(_keyClockPosition, settings.clockPosition);
  }

  /// Loads saved settings or returns default ClockSettings if none found.
  static Future<ClockSettings> loadSettings() async {
    final prefs = await SharedPreferences.getInstance();

    final is24Hour = prefs.getBool(_keyIs24Hour) ?? false;
    final showSeconds = prefs.getBool(_keyShowSeconds) ?? true;
    final showDate = prefs.getBool(_keyShowDate) ?? true;
    final fontStyle = prefs.getString(_keyFontStyle) ?? 'Digital';
    final colorVal = prefs.getInt(_keyClockColor) ?? 0xFF00E5FF;
    final bgType = prefs.getString(_keyBgType) ?? 'solid';
    final bgSolidVal = prefs.getInt(_keyBgSolidColor) ?? 0xFF0D1117;
    final bgGradientIdx = prefs.getInt(_keyBgGradientIndex) ?? 0;
    final customImg = prefs.getString(_keyCustomImagePath);
    final clockSize = prefs.getString(_keyClockSize) ?? 'large';
    final clockPosition = prefs.getString(_keyClockPosition) ?? 'center';

    return ClockSettings(
      is24Hour: is24Hour,
      showSeconds: showSeconds,
      showDate: showDate,
      fontStyle: fontStyle,
      clockColor: Color(colorVal),
      bgType: bgType,
      bgSolidColor: Color(bgSolidVal),
      bgGradientIndex: bgGradientIdx,
      customImagePath: customImg,
      clockSize: clockSize,
      clockPosition: clockPosition,
    );
  }
}
