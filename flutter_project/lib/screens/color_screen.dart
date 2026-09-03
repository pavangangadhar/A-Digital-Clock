import 'package:flutter/material.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';

/// ColorScreen
///
/// Educational Note:
/// - Provides both preset swatches and custom HSV color slider controls.
/// - In Flutter, Color(0xAARRGGBB) stores color as an unsigned 32-bit integer.
/// - HSVColor.fromAHSV() allows intuitive Hue (0-360), Saturation (0-1), and Value (0-1) adjustments.
class ColorScreen extends StatefulWidget {
  final DateTime currentTime;
  final ClockSettings settings;
  final Function(Color) onSelectColor;

  const ColorScreen({
    super.key,
    required this.currentTime,
    required this.settings,
    required this.onSelectColor,
  });

  @override
  State<ColorScreen> createState() => _ColorScreenState();
}

class _ColorScreenState extends State<ColorScreen> {
  static const List<Map<String, dynamic>> presetColors = [
    {'name': 'White', 'color': Color(0xFFFFFFFF)},
    {'name': 'Dark / Black', 'color': Color(0xFF333333)},
    {'name': 'Red', 'color': Color(0xFFFF3B30)},
    {'name': 'Blue', 'color': Color(0xFF007AFF)},
    {'name': 'Green', 'color': Color(0xFF34C759)},
    {'name': 'Yellow', 'color': Color(0xFFFFCC00)},
    {'name': 'Purple', 'color': Color(0xFFAF52DE)},
    {'name': 'Orange', 'color': Color(0xFFFF9500)},
    {'name': 'Cyan', 'color': Color(0xFF00E5FF)},
    {'name': 'Neon Green', 'color': Color(0xFF39FF14)},
    {'name': 'Hot Pink', 'color': Color(0xFFFF2D55)},
    {'name': 'Amber', 'color': Color(0xFFFFB300)},
  ];

  double _hue = 180.0;
  double _saturation = 1.0;
  double _value = 1.0;

  @override
  void initState() {
    super.initState();
    final hsv = HSVColor.fromColor(widget.settings.clockColor);
    _hue = hsv.hue;
    _saturation = hsv.saturation;
    _value = hsv.value;
  }

  void _updateCustomColor() {
    final color = HSVColor.fromAHSV(1.0, _hue, _saturation, _value).toColor();
    widget.onSelectColor(color);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1117),
      appBar: AppBar(
        title: const Text('Clock Color', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF0D1117),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Live Clock Preview
          Container(
            padding: const EdgeInsets.all(24.0),
            decoration: BoxDecoration(
              color: Colors.black.withOpacity(0.4),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Center(
              child: ClockDisplay(
                dateTime: widget.currentTime,
                settings: widget.settings,
                isFullScreen: false,
                previewScale: 0.85,
              ),
            ),
          ),
          const SizedBox(height: 20),

          const Text(
            'Preset Colors',
            style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          // Preset Swatches Grid
          Wrap(
            spacing: 12,
            runSpacing: 12,
            children: presetColors.map((item) {
              final color = item['color'] as Color;
              final isSelected = widget.settings.clockColor.value == color.value;

              return GestureDetector(
                onTap: () {
                  widget.onSelectColor(color);
                },
                child: Container(
                  width: 50,
                  height: 50,
                  decoration: BoxDecoration(
                    color: color,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: isSelected ? Colors.white : Colors.white24,
                      width: isSelected ? 3 : 1,
                    ),
                  ),
                  child: isSelected
                      ? const Icon(Icons.check, color: Colors.black, size: 24)
                      : null,
                ),
              );
            }).toList(),
          ),

          const SizedBox(height: 24),

          // Custom HSV Picker Section
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.05),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Custom Color Picker (HSV)',
                      style: TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.bold),
                    ),
                    Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        color: widget.settings.clockColor,
                        shape: BoxShape.circle,
                        border: Border.all(color: Colors.white),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),

                Text('Hue: ${_hue.toInt()}°', style: const TextStyle(color: Colors.white70, fontSize: 12)),
                Slider(
                  value: _hue,
                  min: 0,
                  max: 360,
                  activeColor: widget.settings.clockColor,
                  onChanged: (val) {
                    setState(() => _hue = val);
                    _updateCustomColor();
                  },
                ),

                Text('Saturation: ${(_saturation * 100).toInt()}%', style: const TextStyle(color: Colors.white70, fontSize: 12)),
                Slider(
                  value: _saturation,
                  min: 0,
                  max: 1,
                  activeColor: widget.settings.clockColor,
                  onChanged: (val) {
                    setState(() => _saturation = val);
                    _updateCustomColor();
                  },
                ),

                Text('Brightness: ${(_value * 100).toInt()}%', style: const TextStyle(color: Colors.white70, fontSize: 12)),
                Slider(
                  value: _value,
                  min: 0.2,
                  max: 1,
                  activeColor: widget.settings.clockColor,
                  onChanged: (val) {
                    setState(() => _value = val);
                    _updateCustomColor();
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
