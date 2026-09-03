import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';

/// FullscreenScreen
///
/// Educational Note:
/// 1. KEEP SCREEN AWAKE:
///    - In Flutter, `wakelock_plus` prevents the device from auto-sleeping or dimming.
///    - WakelockPlus.enable() is called in initState().
///    - WakelockPlus.disable() is called in dispose() so the device returns to normal power-saving!
/// 2. HIDING SYSTEM UI:
///    - SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky) hides system navigation & status bars.
///    - SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge) restores them when exiting.
/// 3. EXIT INTERACTION:
///    - Double-tap gesture anywhere on the screen exits full screen mode.
///    - An unobtrusive top-right 'X' button also offers touch accessibility.
class FullscreenScreen extends StatefulWidget {
  final ClockSettings settings;

  const FullscreenScreen({
    super.key,
    required this.settings,
  });

  @override
  State<FullscreenScreen> createState() => _FullscreenScreenState();
}

class _FullscreenScreenState extends State<FullscreenScreen> {
  late DateTime _currentTime;
  Timer? _timer;
  bool _showHint = true;

  @override
  void initState() {
    super.initState();
    _currentTime = DateTime.now();

    // 1. Keep the device awake only while full-screen clock is running
    WakelockPlus.enable();

    // 2. Hide system bars (immersive mode)
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);

    // 3. Start 1-second clock ticker
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() {
          _currentTime = DateTime.now();
        });
      }
    });

    // 4. Auto-fade the exit instruction hint after 3.5 seconds
    Future.delayed(const Duration(milliseconds: 3500), () {
      if (mounted) {
        setState(() {
          _showHint = false;
        });
      }
    });
  }

  @override
  void dispose() {
    _timer?.cancel();

    // Return device to normal sleep behavior immediately
    WakelockPlus.disable();

    // Restore system UI bars
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GestureDetector(
        behavior: HitTestBehavior.opaque,
        // Double tap anywhere to exit full-screen mode
        onDoubleTap: () {
          Navigator.pop(context);
        },
        child: Container(
          width: double.infinity,
          height: double.infinity,
          decoration: BoxDecoration(
            color: widget.settings.bgType == 'solid'
                ? widget.settings.bgSolidColor
                : null,
            gradient: widget.settings.bgType == 'gradient'
                ? LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black,
                      widget.settings.clockColor.withOpacity(0.2),
                      Colors.black,
                    ],
                  )
                : null,
            image: widget.settings.bgType == 'image' && widget.settings.customImagePath != null
                ? DecorationImage(
                    image: FileImage(File(widget.settings.customImagePath!)),
                    fit: BoxFit.cover,
                  )
                : null,
          ),
          child: Stack(
            children: [
              // Center Live Clock
              Center(
                child: ClockDisplay(
                  dateTime: _currentTime,
                  settings: widget.settings,
                  isFullScreen: true,
                  previewScale: 1.0,
                ),
              ),

              // Unobtrusive Top-Right Exit Button
              Positioned(
                top: 24,
                right: 24,
                child: SafeArea(
                  child: IconButton(
                    icon: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.4),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.close, color: Colors.white70, size: 20),
                    ),
                    onPressed: () {
                      Navigator.pop(context);
                    },
                  ),
                ),
              ),

              // Bottom Exit Hint
              if (_showHint)
                Positioned(
                  bottom: 40,
                  left: 0,
                  right: 0,
                  child: Center(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.55),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: const Text(
                        'Double-tap anywhere to exit',
                        style: TextStyle(color: Colors.white70, fontSize: 12),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
